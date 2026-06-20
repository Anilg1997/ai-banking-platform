package com.banking.card.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cards", indexes = {
    @Index(name = "idx_card_user_id", columnList = "userId"),
    @Index(name = "idx_card_number", columnList = "cardNumber", unique = true),
    @Index(name = "idx_card_status", columnList = "status"),
    @Index(name = "idx_card_expiry", columnList = "expiryDate")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false, unique = true, length = 16)
    private String cardNumber;

    @Column(nullable = false)
    private String cardHolderName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CardType cardType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CardNetwork cardNetwork;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private CardStatus status;

    @Column(precision = 19, scale = 2)
    private BigDecimal creditLimit;

    @Column(precision = 19, scale = 2)
    private BigDecimal availableCredit;

    @Column(precision = 19, scale = 2)
    private BigDecimal usedCredit;

    @Column(precision = 19, scale = 2)
    private BigDecimal cashLimit;

    @Column(precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal interestRate = new BigDecimal("24.99");

    @Column
    @Builder.Default
    private Integer billingCycle = 1;

    @Column(precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal minimumPaymentPercent = new BigDecimal("5.0");

    @Column(precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal currentBalance = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal outstandingAmount = BigDecimal.ZERO;

    @Column
    @Builder.Default
    private Long rewardPoints = 0L;

    @Column(precision = 19, scale = 2)
    private BigDecimal annualFee;

    @Column(precision = 19, scale = 2)
    private BigDecimal joiningFee;

    @Column(precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal foreignTransactionFee = new BigDecimal("3.0");

    @Column(nullable = false)
    private LocalDate expiryDate;

    @Column(length = 4)
    private String cvv;

    @Column
    @Builder.Default
    private Boolean pinSet = false;

    @Column(nullable = false, updatable = false)
    private LocalDate issuanceDate;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (issuanceDate == null) {
            issuanceDate = LocalDate.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum CardType {
        CREDIT, DEBIT, PREPAID
    }

    public enum CardNetwork {
        VISA, MASTERCARD, AMEX, RUPAY
    }

    public enum CardStatus {
        PENDING, ACTIVE, FROZEN, CANCELLED, EXPIRED, LOST, STOLEN
    }
}
