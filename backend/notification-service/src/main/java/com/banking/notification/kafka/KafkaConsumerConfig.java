package com.banking.notification.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConsumerConfig {

    public static final String TOPIC_TRANSACTION_COMPLETED = "transaction.completed";
    public static final String TOPIC_TRANSACTION_FAILED = "transaction.failed";
    public static final String TOPIC_TRANSACTION_CREATED = "transaction.created";

    @Bean
    public NewTopic notificationTransactionCompleted() {
        return new NewTopic(TOPIC_TRANSACTION_COMPLETED, 3, (short) 1);
    }

    @Bean
    public NewTopic notificationTransactionFailed() {
        return new NewTopic(TOPIC_TRANSACTION_FAILED, 3, (short) 1);
    }
}
