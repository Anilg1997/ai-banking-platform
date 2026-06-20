package com.banking.card.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "card_transactions", indexes = {
    @Index(name = "idx_card_txn_card_id", columnList = "cardId"),
    @Index(name = "idx_card_txn_user_id", columnList = "userId"),
    @Index(name = "idx_card_txn_ref", columnList = "transactionRef", unique = true),
    @Index(name = "idx_card_txn_created", columnList = "createdAt"),
    @Index(name = "idx_card_txn_status", columnList = "status"),
    @Index(name = "idx_card_txn_category", columnList = "merchantCategory")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String cardId;

    @Column(nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private TransactionStatus status;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(length = 3)
    @Builder.Default
    private String currency = "USD";

    @Column(length = 100)
    private String merchantName;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private MerchantCategory merchantCategory;

    @Column(length = 50)
    private String merchantId;

    @Column(length = 200)
    private String description;

    @Column(nullable = false, unique = true, length = 30)
    private String transactionRef;

    @Column(precision = 19, scale = 2)
    private BigDecimal billingAmount;

    @Column(length = 3)
    private String foreignCurrency;

    @Column(precision = 19, scale = 6)
    private BigDecimal exchangeRate;

    @Column
    @Builder.Default
    private Long rewardPointsEarned = 0L;

    @Column
    @Builder.Default
    private Boolean installmentPlan = false;

    @Column
    private Integer installmentMonths;

    @Column(precision = 19, scale = 2)
    private BigDecimal monthlyInstallment;

    @Column(precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal feeAmount = BigDecimal.ZERO;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime completedAt;

    @Column(length = 500)
    private String failureReason;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum TransactionType {
        PURCHASE, PAYMENT, REFUND, CASH_ADVANCE, FEE, INTEREST,
        ANNUAL_FEE, LATE_FEE, FOREIGN_TRANSACTION_FEE
    }

    public enum TransactionStatus {
        PENDING, PROCESSING, COMPLETED, FAILED, DISPUTED, REFUNDED
    }

    public enum MerchantCategory {
        RESTAURANT, GROCERY, TRAVEL, SHOPPING, ENTERTAINMENT, BILLS, HEALTHCARE, EDUCATION, OTHER
    }
}
