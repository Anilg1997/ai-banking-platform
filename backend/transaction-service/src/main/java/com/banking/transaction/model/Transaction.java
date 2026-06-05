package com.banking.transaction.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_txn_user_id", columnList = "fromUserId"),
    @Index(name = "idx_txn_account", columnList = "fromAccountId"),
    @Index(name = "idx_txn_ref", columnList = "transactionRef", unique = true),
    @Index(name = "idx_txn_created", columnList = "createdAt"),
    @Index(name = "idx_txn_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true, length = 30)
    private String transactionRef;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(length = 3, nullable = false)
    private String currency;

    @Column(length = 50)
    private String fromAccountId;

    @Column(length = 50)
    private String fromUserId;

    @Column(length = 100)
    private String fromAccountNumber;

    @Column(length = 100)
    private String fromAccountName;

    @Column(length = 50)
    private String toAccountId;

    @Column(length = 50)
    private String toUserId;

    @Column(length = 100)
    private String toAccountNumber;

    @Column(length = 100)
    private String toAccountName;

    @Column(length = 50)
    private String toEmail;

    @Column(length = 200)
    private String description;

    @Column(length = 50)
    private String category;

    @Column(precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal feeAmount = BigDecimal.ZERO;

    @Column(precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal exchangeRate = BigDecimal.ONE;

    @Column(length = 500)
    private String failureReason;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime completedAt;

    @Column(length = 45)
    private String ipAddress;

    @Column(length = 255)
    private String userAgent;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = TransactionStatus.PENDING;
        }
    }

    public enum TransactionType {
        TRANSFER,
        DEPOSIT,
        WITHDRAWAL,
        PAYMENT,
        REFUND,
        FEE,
        INTEREST,
        EXCHANGE
    }

    public enum TransactionStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED,
        REVERSED,
        CANCELLED
    }
}
