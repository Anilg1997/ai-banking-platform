package com.banking.notification.kafka;

import com.banking.notification.model.Notification;
import com.banking.notification.repository.NotificationRepository;
import com.banking.notification.service.SseNotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventConsumer {

    private final NotificationRepository notificationRepository;
    private final SseNotificationService sseService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaConsumerConfig.TOPIC_TRANSACTION_COMPLETED, groupId = "notification-group")
    public void handleTransactionCompleted(Object event) {
        log.info("Received transaction.completed event: {}", event);
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) event;

            String txnRef = getString(data, "transactionRef");
            String fromUserId = getString(data, "fromUserId");
            String toUserId = getString(data, "toUserId");
            Number amount = getNumber(data, "amount");
            String currency = getString(data, "currency");

            // Create notification for sender
            if (fromUserId != null) {
                Notification senderNotif = Notification.builder()
                        .userId(fromUserId)
                        .type(Notification.NotificationType.TRANSACTION_COMPLETED)
                        .title("Transfer Sent")
                        .message(String.format("Sent %s %.2f — reference: %s", currency, amount.doubleValue(), txnRef))
                        .referenceId(txnRef)
                        .referenceType("TRANSACTION")
                        .build();
                notificationRepository.save(senderNotif);
                sseService.sendNotification(fromUserId, senderNotif);
            }

            // Create notification for receiver
            if (toUserId != null) {
                Notification receiverNotif = Notification.builder()
                        .userId(toUserId)
                        .type(Notification.NotificationType.DEPOSIT_RECEIVED)
                        .title("Money Received")
                        .message(String.format("Received %s %.2f — reference: %s", currency, amount.doubleValue(), txnRef))
                        .referenceId(txnRef)
                        .referenceType("TRANSACTION")
                        .build();
                notificationRepository.save(receiverNotif);
                sseService.sendNotification(toUserId, receiverNotif);
            }

            log.info("Notifications created for transaction: {}", txnRef);

        } catch (Exception e) {
            log.error("Failed to process transaction.completed event: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = KafkaConsumerConfig.TOPIC_TRANSACTION_FAILED, groupId = "notification-group")
    public void handleTransactionFailed(Object event) {
        log.warn("Received transaction.failed event: {}", event);
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) event;

            String txnRef = getString(data, "transactionRef");
            String reason = getString(data, "reason");
            Number amount = getNumber(data, "amount");
            String currency = getString(data, "currency");

            String userId = getString(data, "userId");
            if (userId == null && data.containsKey("transactionId")) {
                userId = getString(data, "transactionId");
            }

            if (userId != null) {
                Notification notif = Notification.builder()
                        .userId(userId)
                        .type(Notification.NotificationType.TRANSACTION_FAILED)
                        .title("Transaction Failed")
                        .message(String.format("Transfer of %s %.2f failed: %s", currency, amount != null ? amount.doubleValue() : 0, reason != null ? reason : "Unknown error"))
                        .referenceId(txnRef)
                        .referenceType("TRANSACTION")
                        .build();
                notificationRepository.save(notif);
                sseService.sendNotification(userId, notif);
            }

        } catch (Exception e) {
            log.error("Failed to process transaction.failed event: {}", e.getMessage());
        }
    }

    private String getString(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : null;
    }

    private Number getNumber(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val instanceof Number ? (Number) val : null;
    }
}
