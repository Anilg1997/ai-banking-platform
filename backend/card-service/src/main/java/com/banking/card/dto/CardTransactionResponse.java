package com.banking.card.dto;

import com.banking.card.model.CardTransaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardTransactionResponse {
    private String id;
    private String cardId;
    private String userId;
    private String type;
    private String status;
    private BigDecimal amount;
    private String currency;
    private String merchantName;
    private String merchantCategory;
    private String merchantId;
    private String description;
    private String transactionRef;
    private BigDecimal billingAmount;
    private String foreignCurrency;
    private BigDecimal exchangeRate;
    private Long rewardPointsEarned;
    private Boolean installmentPlan;
    private Integer installmentMonths;
    private BigDecimal monthlyInstallment;
    private BigDecimal feeAmount;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private String failureReason;

    public static CardTransactionResponse fromTransaction(CardTransaction txn) {
        return CardTransactionResponse.builder()
                .id(txn.getId())
                .cardId(txn.getCardId())
                .userId(txn.getUserId())
                .type(txn.getType().name())
                .status(txn.getStatus().name())
                .amount(txn.getAmount())
                .currency(txn.getCurrency())
                .merchantName(txn.getMerchantName())
                .merchantCategory(txn.getMerchantCategory() != null ? txn.getMerchantCategory().name() : null)
                .merchantId(txn.getMerchantId())
                .description(txn.getDescription())
                .transactionRef(txn.getTransactionRef())
                .billingAmount(txn.getBillingAmount())
                .foreignCurrency(txn.getForeignCurrency())
                .exchangeRate(txn.getExchangeRate())
                .rewardPointsEarned(txn.getRewardPointsEarned())
                .installmentPlan(txn.getInstallmentPlan())
                .installmentMonths(txn.getInstallmentMonths())
                .monthlyInstallment(txn.getMonthlyInstallment())
                .feeAmount(txn.getFeeAmount())
                .createdAt(txn.getCreatedAt())
                .completedAt(txn.getCompletedAt())
                .failureReason(txn.getFailureReason())
                .build();
    }
}
