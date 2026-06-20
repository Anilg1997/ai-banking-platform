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
@Table(name = "card_statements", indexes = {
    @Index(name = "idx_stmt_card_id", columnList = "cardId"),
    @Index(name = "idx_stmt_user_id", columnList = "userId"),
    @Index(name = "idx_stmt_date", columnList = "statementDate")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardStatement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String cardId;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private LocalDate statementDate;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Column(nullable = false)
    private LocalDate periodStart;

    @Column(nullable = false)
    private LocalDate periodEnd;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal openingBalance;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal closingBalance;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal minimumPayment;

    @Column(precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal totalCharges = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal totalPayments = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal totalCashAdvances = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal totalInterest = BigDecimal.ZERO;

    @Column(precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal totalFees = BigDecimal.ZERO;

    @Column
    @Builder.Default
    private Long rewardPointsEarned = 0L;

    @Column
    @Builder.Default
    private Long rewardPointsUsed = 0L;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus paymentStatus;

    @Column(precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal paidAmount = BigDecimal.ZERO;

    private LocalDateTime paymentDate;

    @Column(nullable = false)
    private LocalDateTime generatedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (generatedAt == null) {
            generatedAt = LocalDateTime.now();
        }
    }

    public enum PaymentStatus {
        UNPAID, PARTIALLY_PAID, PAID, OVERDUE
    }
}
