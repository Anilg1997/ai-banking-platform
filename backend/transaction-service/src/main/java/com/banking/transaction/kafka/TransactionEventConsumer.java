package com.banking.transaction.kafka;

import com.banking.transaction.dto.TransactionSummary;
import com.banking.transaction.service.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventConsumer {

    private final SseService sseService;

    @KafkaListener(topics = KafkaConfig.TOPIC_TRANSACTION_CREATED, groupId = "transaction-group")
    public void consumeTransactionCreated(TransactionSummary summary) {
        log.info("Received transaction.created event: ref={} amount={} {}", 
                summary.getTransactionRef(), summary.getAmount(), summary.getCurrency());
    }

    @KafkaListener(topics = KafkaConfig.TOPIC_TRANSACTION_COMPLETED, groupId = "transaction-group")
    public void consumeTransactionCompleted(TransactionSummary summary) {
        log.info("Received transaction.completed event: ref={}", summary.getTransactionRef());

        // Push to SSE clients for real-time updates
        sseService.broadcast(summary);
    }

    @KafkaListener(topics = KafkaConfig.TOPIC_TRANSACTION_FAILED, groupId = "transaction-group")
    public void consumeTransactionFailed(Object event) {
        log.warn("Received transaction.failed event: {}", event);
    }

    @KafkaListener(topics = KafkaConfig.TOPIC_ACCOUNT_BALANCE_UPDATED, groupId = "transaction-group")
    public void consumeBalanceUpdated(Object event) {
        log.debug("Received account.balance.updated event: {}", event);
    }
}
