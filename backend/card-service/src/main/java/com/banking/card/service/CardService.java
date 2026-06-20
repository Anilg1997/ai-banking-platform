package com.banking.card.service;

import com.banking.card.dto.*;
import com.banking.card.model.Card;
import com.banking.card.model.CardApplication;
import com.banking.card.model.CardStatement;
import com.banking.card.model.CardTransaction;
import com.banking.card.repository.CardApplicationRepository;
import com.banking.card.repository.CardRepository;
import com.banking.card.repository.CardStatementRepository;
import com.banking.card.repository.CardTransactionRepository;
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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardService {

    private final CardRepository cardRepository;
    private final CardTransactionRepository transactionRepository;
    private final CardStatementRepository statementRepository;
    private final CardApplicationRepository applicationRepository;
    private final CardStatementService statementService;
    private final WebClient.Builder webClientBuilder;
    private final Random random = new Random();

    @Value("${app.services.account-service.url:http://localhost:8083}")
    private String accountServiceUrl;

    @Transactional
    public CardApplicationResponse applyForCard(CardApplicationRequest request, String userId) {
        CardApplication application = CardApplication.builder()
                .userId(userId)
                .status(CardApplication.ApplicationStatus.SUBMITTED)
                .cardType(Card.CardType.valueOf(request.getCardType().toUpperCase()))
                .cardNetwork(Card.CardNetwork.valueOf(request.getCardNetwork().toUpperCase()))
                .requestedCreditLimit(request.getRequestedCreditLimit())
                .annualIncome(request.getAnnualIncome())
                .employmentType(CardApplication.EmploymentType.valueOf(request.getEmploymentType().toUpperCase()))
                .employerName(request.getEmployerName())
                .designation(request.getDesignation())
                .monthlyIncome(request.getMonthlyIncome())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .aadharNumber(request.getAadharNumber())
                .panNumber(request.getPanNumber())
                .submittedAt(LocalDateTime.now())
                .build();

        CardApplication saved = applicationRepository.save(application);
        log.info("Card application submitted: id={}, userId={}, type={}", saved.getId(), userId, request.getCardType());
        return CardApplicationResponse.fromApplication(saved);
    }

    public List<CardSummary> getUserCards(String userId) {
        return cardRepository.findByUserId(userId).stream()
                .map(CardSummary::fromCard)
                .collect(Collectors.toList());
    }

    public CardResponse getCard(String cardId, String userId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Card not found: " + cardId));
        if (!card.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Card does not belong to user");
        }
        return CardResponse.fromCard(card);
    }

    public List<CardTransactionResponse> getCardTransactions(String cardId, String userId, int page, int size) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Card not found: " + cardId));
        if (!card.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Card does not belong to user");
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<CardTransaction> txns = transactionRepository.findByCardId(cardId, pageable);
        return txns.stream()
                .map(CardTransactionResponse::fromTransaction)
                .collect(Collectors.toList());
    }

    public CardStatementResponse getLatestStatement(String cardId, String userId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Card not found: " + cardId));
        if (!card.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Card does not belong to user");
        }
        CardStatement stmt = statementService.getLatestStatement(cardId);
        if (stmt == null) {
            throw new IllegalArgumentException("No statement found for card: " + cardId);
        }
        return CardStatementResponse.fromStatement(stmt);
    }

    public List<CardStatementResponse> getAllStatements(String cardId, String userId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Card not found: " + cardId));
        if (!card.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Card does not belong to user");
        }
        return statementService.getAllStatements(cardId).stream()
                .map(CardStatementResponse::fromStatement)
                .collect(Collectors.toList());
    }

    public Map<String, Object> getAvailableCredit(String cardId, String userId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Card not found: " + cardId));
        if (!card.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Card does not belong to user");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("cardId", card.getId());
        result.put("creditLimit", card.getCreditLimit());
        result.put("availableCredit", card.getAvailableCredit());
        result.put("usedCredit", card.getUsedCredit());
        result.put("cashLimit", card.getCashLimit());
        return result;
    }

    @Transactional
    public CardResponse activateCard(String cardId, String userId, ActivateCardRequest request) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Card not found: " + cardId));
        if (!card.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Card does not belong to user");
        }
        if (card.getStatus() != Card.CardStatus.PENDING) {
            throw new IllegalStateException("Card is not in PENDING status");
        }
        if (!card.getCvv().equals(request.getCvv())) {
            throw new IllegalArgumentException("Invalid CVV");
        }
        LocalDate expiry = LocalDate.parse(request.getExpiryDate(), DateTimeFormatter.ofPattern("MM/yy"));
        if (!card.getExpiryDate().equals(expiry)) {
            throw new IllegalArgumentException("Invalid expiry date");
        }
        if (expiry.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Card has expired");
        }

        card.setStatus(Card.CardStatus.ACTIVE);
        card = cardRepository.save(card);
        log.info("Card activated: id={}", cardId);
        return CardResponse.fromCard(card);
    }

    @Transactional
    public CardResponse freezeCard(String cardId, String userId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Card not found: " + cardId));
        if (!card.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Card does not belong to user");
        }
        if (card.getStatus() != Card.CardStatus.ACTIVE) {
            throw new IllegalStateException("Only active cards can be frozen");
        }
        card.setStatus(Card.CardStatus.FROZEN);
        card = cardRepository.save(card);
        log.info("Card frozen: id={}", cardId);
        return CardResponse.fromCard(card);
    }

    @Transactional
    public CardResponse unfreezeCard(String cardId, String userId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Card not found: " + cardId));
        if (!card.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Card does not belong to user");
        }
        if (card.getStatus() != Card.CardStatus.FROZEN) {
            throw new IllegalStateException("Card is not frozen");
        }
        card.setStatus(Card.CardStatus.ACTIVE);
        card = cardRepository.save(card);
        log.info("Card unfrozen: id={}", cardId);
        return CardResponse.fromCard(card);
    }

    @Transactional
    public CardResponse reportLostCard(String cardId, String userId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Card not found: " + cardId));
        if (!card.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Card does not belong to user");
        }
        card.setStatus(Card.CardStatus.LOST);
        card = cardRepository.save(card);
        log.info("Card reported lost/stolen: id={}", cardId);
        return CardResponse.fromCard(card);
    }

    @Transactional
    public CardTransactionResponse makePayment(String cardId, String userId, CardPaymentRequest request) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Card not found: " + cardId));
        if (!card.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Card does not belong to user");
        }
        if (card.getStatus() != Card.CardStatus.ACTIVE && card.getStatus() != Card.CardStatus.FROZEN) {
            throw new IllegalStateException("Card is not active");
        }

        String txnRef = generateTransactionRef();

        try {
            WebClient client = webClientBuilder.baseUrl(accountServiceUrl).build();
            client.patch()
                    .uri("/api/accounts/" + request.getFromAccountId() + "/debit")
                    .bodyValue(Map.of("amount", request.getAmount()))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (Exception e) {
            log.error("Payment failed: failed to debit account {}: {}", request.getFromAccountId(), e.getMessage());
            throw new RuntimeException("Payment failed: unable to debit account: " + e.getMessage());
        }

        CardTransaction payment = CardTransaction.builder()
                .cardId(cardId)
                .userId(userId)
                .type(CardTransaction.TransactionType.PAYMENT)
                .status(CardTransaction.TransactionStatus.COMPLETED)
                .amount(request.getAmount())
                .currency("USD")
                .merchantName("NovaBank Payment")
                .description("Card payment from account " + request.getFromAccountId())
                .transactionRef(txnRef)
                .completedAt(LocalDateTime.now())
                .build();

        payment = transactionRepository.save(payment);

        BigDecimal newBalance = card.getCurrentBalance().subtract(request.getAmount());
        BigDecimal newUsedCredit = card.getUsedCredit().subtract(request.getAmount());
        BigDecimal newAvailableCredit = card.getAvailableCredit().add(request.getAmount());

        card.setCurrentBalance(newBalance.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : newBalance);
        card.setUsedCredit(newUsedCredit.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : newUsedCredit);
        card.setAvailableCredit(newAvailableCredit.compareTo(card.getCreditLimit()) > 0 ? card.getCreditLimit() : newAvailableCredit);
        cardRepository.save(card);

        CardStatement latestStmt = statementService.getLatestStatement(cardId);
        if (latestStmt != null) {
            statementService.payStatement(latestStmt, request.getAmount());
        }

        log.info("Card payment completed: card={}, amount={}, ref={}", cardId, request.getAmount(), txnRef);
        return CardTransactionResponse.fromTransaction(payment);
    }

    public List<CardTransactionResponse> getTransactionsByCategory(String cardId, String userId, String category, int page, int size) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Card not found: " + cardId));
        if (!card.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Card does not belong to user");
        }
        CardTransaction.MerchantCategory cat;
        try {
            cat = CardTransaction.MerchantCategory.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid category: " + category);
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<CardTransaction> txns = transactionRepository.findByCardIdAndMerchantCategory(cardId, cat, pageable);
        return txns.stream()
                .map(CardTransactionResponse::fromTransaction)
                .collect(Collectors.toList());
    }

    public CardTransactionResponse getTransactionByRef(String ref) {
        CardTransaction txn = transactionRepository.findByTransactionRef(ref)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + ref));
        return CardTransactionResponse.fromTransaction(txn);
    }

    @Transactional
    public CardTransactionResponse disputeTransaction(String transactionId, String userId, DisputeRequest request) {
        CardTransaction txn = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));
        if (!txn.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Transaction does not belong to user");
        }
        if (txn.getStatus() != CardTransaction.TransactionStatus.COMPLETED) {
            throw new IllegalStateException("Only completed transactions can be disputed");
        }
        txn.setStatus(CardTransaction.TransactionStatus.DISPUTED);
        txn.setDescription(txn.getDescription() + " [DISPUTED: " + request.getReason() + " - " + request.getDescription() + "]");
        txn = transactionRepository.save(txn);
        log.info("Transaction disputed: id={}, reason={}", transactionId, request.getReason());
        return CardTransactionResponse.fromTransaction(txn);
    }

    public Map<String, Object> getRewardsSummary(String userId) {
        List<Card> cards = cardRepository.findByUserId(userId);
        long totalPoints = cards.stream().mapToLong(Card::getRewardPoints).sum();
        long activeCards = cards.stream().filter(c -> c.getStatus() == Card.CardStatus.ACTIVE).count();

        BigDecimal avgRewardRate = new BigDecimal("1.0");
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("userId", userId);
        summary.put("totalRewardPoints", totalPoints);
        summary.put("activeCards", activeCards);
        summary.put("totalCards", cards.size());
        summary.put("rewardRate", avgRewardRate + " pts per $100 (2x on Travel & Dining)");
        return summary;
    }

    public CardApplicationResponse getApplicationStatus(String applicationId, String userId) {
        CardApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + applicationId));
        if (!app.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Application does not belong to user");
        }
        return CardApplicationResponse.fromApplication(app);
    }

    // ============== Admin Methods ==============

    public List<CardResponse> getAllCards(int page, int size, String statusFilter) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Card> cards;
        if (statusFilter != null && !statusFilter.isEmpty()) {
            Card.CardStatus status = Card.CardStatus.valueOf(statusFilter.toUpperCase());
            cards = cardRepository.findAll(Pageable.unpaged());
            return cardRepository.findByStatus(status).stream()
                    .map(CardResponse::fromCard)
                    .collect(Collectors.toList());
        }
        cards = cardRepository.findAll(pageable);
        return cards.stream()
                .map(CardResponse::fromCard)
                .collect(Collectors.toList());
    }

    public CardResponse getCardById(String cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Card not found: " + cardId));
        return CardResponse.fromCard(card);
    }

    @Transactional
    public CardResponse updateCardStatus(String cardId, String newStatus) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Card not found: " + cardId));
        Card.CardStatus status;
        try {
            status = Card.CardStatus.valueOf(newStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + newStatus);
        }
        card.setStatus(status);
        card = cardRepository.save(card);
        log.info("Card status updated: id={}, status={}", cardId, newStatus);
        return CardResponse.fromCard(card);
    }

    @Transactional
    public CardResponse updateCreditLimit(String cardId, BigDecimal newLimit) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Card not found: " + cardId));
        if (newLimit.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Credit limit must be positive");
        }
        BigDecimal diff = newLimit.subtract(card.getCreditLimit());
        card.setCreditLimit(newLimit);
        card.setAvailableCredit(card.getAvailableCredit().add(diff));
        card.setCashLimit(newLimit.multiply(new BigDecimal("0.8")).setScale(2, RoundingMode.HALF_UP));
        card = cardRepository.save(card);
        log.info("Card credit limit updated: id={}, newLimit={}", cardId, newLimit);
        return CardResponse.fromCard(card);
    }

    public List<CardApplicationResponse> getAllApplications(String statusFilter) {
        List<CardApplication> apps;
        if (statusFilter != null && !statusFilter.isEmpty()) {
            CardApplication.ApplicationStatus status;
            try {
                status = CardApplication.ApplicationStatus.valueOf(statusFilter.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid application status: " + statusFilter);
            }
            apps = applicationRepository.findByStatusOrderBySubmittedAtDesc(status);
        } else {
            apps = applicationRepository.findAll();
        }
        return apps.stream()
                .map(CardApplicationResponse::fromApplication)
                .collect(Collectors.toList());
    }

    public CardApplicationResponse getApplicationById(String applicationId) {
        CardApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + applicationId));
        return CardApplicationResponse.fromApplication(app);
    }

    @Transactional
    public CardResponse approveApplication(String applicationId, String reviewedBy) {
        CardApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + applicationId));
        if (app.getStatus() != CardApplication.ApplicationStatus.SUBMITTED
                && app.getStatus() != CardApplication.ApplicationStatus.UNDER_REVIEW) {
            throw new IllegalStateException("Application is not in SUBMITTED or UNDER_REVIEW status");
        }

        app.setStatus(CardApplication.ApplicationStatus.APPROVED);
        app.setReviewedBy(reviewedBy);
        app.setReviewedAt(LocalDateTime.now());
        applicationRepository.save(app);

        String cardNumber = generateCardNumber();
        String cvv = String.format("%03d", random.nextInt(1000));
        LocalDate expiryDate = LocalDate.now().plusYears(5).withDayOfMonth(1);

        BigDecimal creditLimit = app.getRequestedCreditLimit();
        if (creditLimit == null || creditLimit.compareTo(BigDecimal.ZERO) <= 0) {
            creditLimit = calculateCreditLimit(app.getAnnualIncome());
        }

        Card card = Card.builder()
                .userId(app.getUserId())
                .cardNumber(cardNumber)
                .cardHolderName(app.getUserId())
                .cardType(app.getCardType())
                .cardNetwork(app.getCardNetwork())
                .status(Card.CardStatus.PENDING)
                .creditLimit(creditLimit)
                .availableCredit(creditLimit)
                .usedCredit(BigDecimal.ZERO)
                .cashLimit(creditLimit.multiply(new BigDecimal("0.8")).setScale(2, RoundingMode.HALF_UP))
                .currentBalance(BigDecimal.ZERO)
                .outstandingAmount(BigDecimal.ZERO)
                .rewardPoints(0L)
                .expiryDate(expiryDate)
                .cvv(cvv)
                .pinSet(false)
                .build();

        card = cardRepository.save(card);
        log.info("Card application approved: appId={}, cardId={}", applicationId, card.getId());
        return CardResponse.fromCard(card);
    }

    @Transactional
    public CardApplicationResponse rejectApplication(String applicationId, String reviewedBy, String reason) {
        CardApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found: " + applicationId));
        if (app.getStatus() != CardApplication.ApplicationStatus.SUBMITTED
                && app.getStatus() != CardApplication.ApplicationStatus.UNDER_REVIEW) {
            throw new IllegalStateException("Application is not in SUBMITTED or UNDER_REVIEW status");
        }

        app.setStatus(CardApplication.ApplicationStatus.REJECTED);
        app.setReviewedBy(reviewedBy);
        app.setRejectionReason(reason);
        app.setReviewedAt(LocalDateTime.now());
        app = applicationRepository.save(app);
        log.info("Card application rejected: appId={}, reason={}", applicationId, reason);
        return CardApplicationResponse.fromApplication(app);
    }

    public Map<String, Object> getCardStats() {
        List<Card> allCards = cardRepository.findAll();
        long totalCards = allCards.size();
        long activeCards = allCards.stream().filter(c -> c.getStatus() == Card.CardStatus.ACTIVE).count();
        long frozenCards = allCards.stream().filter(c -> c.getStatus() == Card.CardStatus.FROZEN).count();
        long cancelledCards = allCards.stream().filter(c -> c.getStatus() == Card.CardStatus.CANCELLED).count();
        long pendingCards = allCards.stream().filter(c -> c.getStatus() == Card.CardStatus.PENDING).count();

        BigDecimal totalCreditLimits = allCards.stream()
                .filter(c -> c.getCreditLimit() != null)
                .map(Card::getCreditLimit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalOutstanding = allCards.stream()
                .filter(c -> c.getOutstandingAmount() != null)
                .map(Card::getOutstandingAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalRewardPoints = new BigDecimal(
                allCards.stream().mapToLong(Card::getRewardPoints).sum());

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalCards", totalCards);
        stats.put("activeCards", activeCards);
        stats.put("frozenCards", frozenCards);
        stats.put("cancelledCards", cancelledCards);
        stats.put("pendingCards", pendingCards);
        stats.put("totalCreditLimits", totalCreditLimits);
        stats.put("totalOutstanding", totalOutstanding);
        stats.put("totalRewardPoints", totalRewardPoints);
        return stats;
    }

    public List<CardTransactionResponse> getRecentTransactions(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<CardTransaction> allTxns = transactionRepository.findAll(pageable).getContent();
        return allTxns.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(limit)
                .map(CardTransactionResponse::fromTransaction)
                .collect(Collectors.toList());
    }

    // ============== Helper Methods ==============

    public long calculateRewardPoints(BigDecimal amount, CardTransaction.MerchantCategory category) {
        int pointsPerUnit = 100;
        int multiplier = (category == CardTransaction.MerchantCategory.TRAVEL
                || category == CardTransaction.MerchantCategory.RESTAURANT) ? 2 : 1;
        return amount.divide(new BigDecimal(pointsPerUnit), 0, RoundingMode.FLOOR).longValue() * multiplier;
    }

    public String generateCardNumber() {
        String prefix;
        int netIdx = random.nextInt(4);
        switch (netIdx) {
            case 0: prefix = "4"; break;
            case 1: prefix = "5"; break;
            case 2: prefix = "37"; break;
            case 3: prefix = "6"; break;
            default: prefix = "4";
        }

        StringBuilder sb = new StringBuilder(prefix);
        for (int i = prefix.length(); i < 15; i++) {
            sb.append(random.nextInt(10));
        }

        String partial = sb.toString();
        int checkDigit = luhnCheckDigit(partial);
        sb.append(checkDigit);

        return sb.toString();
    }

    private int luhnCheckDigit(String number) {
        int sum = 0;
        boolean alternate = true;
        for (int i = number.length() - 1; i >= 0; i--) {
            int digit = Integer.parseInt(String.valueOf(number.charAt(i)));
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            alternate = !alternate;
        }
        return (sum * 9) % 10;
    }

    public String generateTransactionRef() {
        String prefix = "CARD";
        String ts = String.valueOf(System.currentTimeMillis() % 10000000);
        String rnd = String.format("%04d", random.nextInt(10000));
        return prefix + ts + rnd;
    }

    private BigDecimal calculateCreditLimit(BigDecimal annualIncome) {
        if (annualIncome == null || annualIncome.compareTo(BigDecimal.ZERO) <= 0) {
            return new BigDecimal("5000");
        }
        BigDecimal limit = annualIncome.multiply(new BigDecimal("0.3"))
                .setScale(2, RoundingMode.HALF_UP);
        if (limit.compareTo(new BigDecimal("50000")) > 0) {
            return new BigDecimal("50000");
        }
        if (limit.compareTo(new BigDecimal("5000")) < 0) {
            return new BigDecimal("5000");
        }
        return limit;
    }
}
