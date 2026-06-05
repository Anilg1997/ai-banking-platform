package com.banking.transaction.kafka;

import com.banking.transaction.dto.TransactionSummary;
import com.banking.transaction.model.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishTransactionCreated(Transaction transaction) {
        TransactionSummary summary = TransactionSummary.fromTransaction(transaction);
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(KafkaConfig.TOPIC_TRANSACTION_CREATED, transaction.getId(), summary);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish transaction.created event: {}", ex.getMessage());
            } else {
                log.debug("Published transaction.created: {} to partition {}",
                        transaction.getTransactionRef(), result.getRecordMetadata().partition());
            }
        });
    }

    public void publishTransactionCompleted(Transaction transaction) {
        TransactionSummary summary = TransactionSummary.fromTransaction(transaction);
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(KafkaConfig.TOPIC_TRANSACTION_COMPLETED, transaction.getId(), summary);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish transaction.completed event: {}", ex.getMessage());
            }
        });

        // Also publish balance update event
        kafkaTemplate.send(KafkaConfig.TOPIC_ACCOUNT_BALANCE_UPDATED, transaction.getFromAccountId(),
                Map.of("accountId", transaction.getFromAccountId(), "type", "DEBIT"));
        kafkaTemplate.send(KafkaConfig.TOPIC_ACCOUNT_BALANCE_UPDATED, transaction.getToAccountId(),
                Map.of("accountId", transaction.getToAccountId(), "type", "CREDIT"));
    }

    public void publishTransactionFailed(Transaction transaction, String reason) {
        Map<String, Object> event = Map.of(
                "transactionId", transaction.getId(),
                "transactionRef", transaction.getTransactionRef(),
                "reason", reason,
                "amount", transaction.getAmount(),
                "currency", transaction.getCurrency()
        );

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(KafkaConfig.TOPIC_TRANSACTION_FAILED, transaction.getId(), event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish transaction.failed event: {}", ex.getMessage());
            }
        });
    }
}
