package com.banking.transaction.service;

import com.banking.transaction.dto.TransactionResponse;
import com.banking.transaction.dto.TransactionSummary;
import com.banking.transaction.dto.TransferRequest;
import com.banking.transaction.kafka.TransactionEventProducer;
import com.banking.transaction.model.Transaction;
import com.banking.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionEventProducer eventProducer;
    private final WebClient.Builder webClientBuilder;
    private final Random random = new Random();

    @Value("${app.services.account-service.url:http://localhost:8083}")
    private String accountServiceUrl;

    @Transactional
    public TransactionResponse transfer(TransferRequest request, String ipAddress, String userAgent) {
        request.sanitize();
        log.info("Initiating transfer: from={} to={} amount={} {}",
                request.getFromAccountId(), request.getToAccountId(), request.getAmount(), request.getCurrency());

        // Create transaction record
        String txnRef = generateTransactionRef();
        Transaction transaction = Transaction.builder()
                .transactionRef(txnRef)
                .type(Transaction.TransactionType.TRANSFER)
                .status(Transaction.TransactionStatus.PROCESSING)
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .fromAccountId(request.getFromAccountId())
                .toAccountId(request.getToAccountId())
                .description(request.getDescription())
                .category(request.getCategory())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .feeAmount(BigDecimal.ZERO)
                .build();

        transaction = transactionRepository.save(transaction);

        // Publish transaction.created event
        eventProducer.publishTransactionCreated(transaction);

        try {
            // Fetch source account details via account-service
            Map fromAccount = fetchAccountDetails(request.getFromAccountId());
            transaction.setFromAccountNumber((String) fromAccount.get("accountNumber"));
            transaction.setFromAccountName((String) fromAccount.get("accountName"));
            transaction.setFromUserId((String) fromAccount.get("userId"));

            // Fetch destination account details
            Map toAccount = fetchAccountDetails(request.getToAccountId());
            transaction.setToAccountNumber((String) toAccount.get("accountNumber"));
            transaction.setToAccountName((String) toAccount.get("accountName"));
            transaction.setToUserId((String) toAccount.get("userId"));

            // Debit source account
            debitAccount(request.getFromAccountId(), request.getAmount());

            // Credit destination account
            creditAccount(request.getToAccountId(), request.getAmount());

            // Mark transaction as completed
            transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
            transaction.setCompletedAt(LocalDateTime.now());
            transaction = transactionRepository.save(transaction);

            // Publish transaction.completed event
            eventProducer.publishTransactionCompleted(transaction);

            log.info("Transfer completed: ref={}", txnRef);

        } catch (Exception e) {
            log.error("Transfer failed: ref={} error={}", txnRef, e.getMessage());

            transaction.setStatus(Transaction.TransactionStatus.FAILED);
            transaction.setFailureReason(e.getMessage());
            transaction = transactionRepository.save(transaction);

            // Publish transaction.failed event
            eventProducer.publishTransactionFailed(transaction, e.getMessage());

            throw new RuntimeException("Transfer failed: " + e.getMessage(), e);
        }

        return TransactionResponse.fromTransaction(transaction);
    }

    public TransactionResponse getTransaction(String id) {
        Transaction txn = transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + id));
        return TransactionResponse.fromTransaction(txn);
    }

    public TransactionResponse getByReference(String ref) {
        Transaction txn = transactionRepository.findByTransactionRef(ref)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + ref));
        return TransactionResponse.fromTransaction(txn);
    }

    public List<TransactionSummary> getUserTransactions(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Transaction> txns = transactionRepository.findByUserInvolved(userId, pageable);
        return txns.stream()
                .map(TransactionSummary::fromTransaction)
                .collect(Collectors.toList());
    }

    public List<TransactionSummary> getUserTransactionsAll(String userId) {
        return transactionRepository.findByUserInvolved(userId).stream()
                .map(TransactionSummary::fromTransaction)
                .collect(Collectors.toList());
    }

    public List<TransactionSummary> getAccountTransactions(String accountId) {
        return transactionRepository.findByAccountInvolved(accountId).stream()
                .map(TransactionSummary::fromTransaction)
                .collect(Collectors.toList());
    }

    public List<TransactionSummary> getRecentTransactions(String userId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return transactionRepository.findRecentByUser(userId, pageable).stream()
                .map(TransactionSummary::fromTransaction)
                .collect(Collectors.toList());
    }

    public List<TransactionSummary> getDateRangeTransactions(String userId, LocalDateTime start, LocalDateTime end) {
        return transactionRepository.findByUserAndDateRange(userId, start, end).stream()
                .map(TransactionSummary::fromTransaction)
                .collect(Collectors.toList());
    }

    public long getTransactionCount(String userId) {
        return transactionRepository.countByFromUserIdOrToUserId(userId, userId);
    }

    @Transactional
    public TransactionResponse reverseTransaction(String id) {
        Transaction original = transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + id));

        if (original.getStatus() != Transaction.TransactionStatus.COMPLETED) {
            throw new IllegalStateException("Only completed transactions can be reversed");
        }

        // Create reversal
        String reverseRef = "REV-" + original.getTransactionRef();
        Transaction reversal = Transaction.builder()
                .transactionRef(reverseRef)
                .type(Transaction.TransactionType.REFUND)
                .status(Transaction.TransactionStatus.PROCESSING)
                .amount(original.getAmount())
                .currency(original.getCurrency())
                .fromAccountId(original.getToAccountId())
                .toAccountId(original.getFromAccountId())
                .fromUserId(original.getToUserId())
                .toUserId(original.getFromUserId())
                .fromAccountNumber(original.getToAccountNumber())
                .toAccountNumber(original.getFromAccountNumber())
                .description("Reversal: " + original.getTransactionRef())
                .build();

        reversal = transactionRepository.save(reversal);

        try {
            // Reverse the money
            debitAccount(original.getToAccountId(), original.getAmount());
            creditAccount(original.getFromAccountId(), original.getAmount());

            reversal.setStatus(Transaction.TransactionStatus.COMPLETED);
            reversal.setCompletedAt(LocalDateTime.now());
            transactionRepository.save(reversal);

            // Mark original as reversed
            original.setStatus(Transaction.TransactionStatus.REVERSED);
            transactionRepository.save(original);

            eventProducer.publishTransactionCompleted(reversal);
            log.info("Transaction reversed: {}", id);

        } catch (Exception e) {
            reversal.setStatus(Transaction.TransactionStatus.FAILED);
            reversal.setFailureReason(e.getMessage());
            transactionRepository.save(reversal);
            throw new RuntimeException("Reversal failed: " + e.getMessage());
        }

        return TransactionResponse.fromTransaction(reversal);
    }

    // ============== Helper Methods ==============

    private Map fetchAccountDetails(String accountId) {
        WebClient client = webClientBuilder.baseUrl(accountServiceUrl).build();
        return client.get()
                .uri("/api/accounts/" + accountId)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    private void debitAccount(String accountId, BigDecimal amount) {
        WebClient client = webClientBuilder.baseUrl(accountServiceUrl).build();
        client.patch()
                .uri("/api/accounts/" + accountId + "/debit")
                .bodyValue(Map.of("amount", amount))
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    private void creditAccount(String accountId, BigDecimal amount) {
        WebClient client = webClientBuilder.baseUrl(accountServiceUrl).build();
        client.patch()
                .uri("/api/accounts/" + accountId + "/credit")
                .bodyValue(Map.of("amount", amount))
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    private String generateTransactionRef() {
        String prefix = "TXN";
        String ts = String.valueOf(System.currentTimeMillis() % 10000000);
        String rnd = String.format("%04d", random.nextInt(10000));
        return prefix + ts + rnd;
    }
}
