package com.banking.transaction.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String TOPIC_TRANSACTION_CREATED = "transaction.created";
    public static final String TOPIC_TRANSACTION_COMPLETED = "transaction.completed";
    public static final String TOPIC_TRANSACTION_FAILED = "transaction.failed";
    public static final String TOPIC_ACCOUNT_BALANCE_UPDATED = "account.balance.updated";

    @Bean
    public NewTopic transactionCreatedTopic() {
        return TopicBuilder.name(TOPIC_TRANSACTION_CREATED)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic transactionCompletedTopic() {
        return TopicBuilder.name(TOPIC_TRANSACTION_COMPLETED)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic transactionFailedTopic() {
        return TopicBuilder.name(TOPIC_TRANSACTION_FAILED)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic accountBalanceUpdatedTopic() {
        return TopicBuilder.name(TOPIC_ACCOUNT_BALANCE_UPDATED)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
