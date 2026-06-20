package com.banking.card.dto;

import com.banking.card.model.CardStatement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardStatementResponse {
    private String id;
    private String cardId;
    private String userId;
    private LocalDate statementDate;
    private LocalDate dueDate;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private BigDecimal openingBalance;
    private BigDecimal closingBalance;
    private BigDecimal minimumPayment;
    private BigDecimal totalCharges;
    private BigDecimal totalPayments;
    private BigDecimal totalCashAdvances;
    private BigDecimal totalInterest;
    private BigDecimal totalFees;
    private Long rewardPointsEarned;
    private Long rewardPointsUsed;
    private String paymentStatus;
    private BigDecimal paidAmount;
    private LocalDateTime paymentDate;
    private LocalDateTime generatedAt;
    private LocalDateTime createdAt;

    public static CardStatementResponse fromStatement(CardStatement stmt) {
        return CardStatementResponse.builder()
                .id(stmt.getId())
                .cardId(stmt.getCardId())
                .userId(stmt.getUserId())
                .statementDate(stmt.getStatementDate())
                .dueDate(stmt.getDueDate())
                .periodStart(stmt.getPeriodStart())
                .periodEnd(stmt.getPeriodEnd())
                .openingBalance(stmt.getOpeningBalance())
                .closingBalance(stmt.getClosingBalance())
                .minimumPayment(stmt.getMinimumPayment())
                .totalCharges(stmt.getTotalCharges())
                .totalPayments(stmt.getTotalPayments())
                .totalCashAdvances(stmt.getTotalCashAdvances())
                .totalInterest(stmt.getTotalInterest())
                .totalFees(stmt.getTotalFees())
                .rewardPointsEarned(stmt.getRewardPointsEarned())
                .rewardPointsUsed(stmt.getRewardPointsUsed())
                .paymentStatus(stmt.getPaymentStatus().name())
                .paidAmount(stmt.getPaidAmount())
                .paymentDate(stmt.getPaymentDate())
                .generatedAt(stmt.getGeneratedAt())
                .createdAt(stmt.getCreatedAt())
                .build();
    }
}
