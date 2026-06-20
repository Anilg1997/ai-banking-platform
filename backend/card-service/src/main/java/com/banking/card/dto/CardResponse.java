package com.banking.card.dto;

import com.banking.card.model.Card;
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
public class CardResponse {
    private String id;
    private String userId;
    private String cardNumber;
    private String cardHolderName;
    private String cardType;
    private String cardNetwork;
    private String status;
    private BigDecimal creditLimit;
    private BigDecimal availableCredit;
    private BigDecimal usedCredit;
    private BigDecimal cashLimit;
    private BigDecimal interestRate;
    private Integer billingCycle;
    private BigDecimal minimumPaymentPercent;
    private BigDecimal currentBalance;
    private BigDecimal outstandingAmount;
    private Long rewardPoints;
    private BigDecimal annualFee;
    private BigDecimal joiningFee;
    private BigDecimal foreignTransactionFee;
    private LocalDate expiryDate;
    private String cvv;
    private Boolean pinSet;
    private LocalDate issuanceDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CardResponse fromCard(Card card) {
        return CardResponse.builder()
                .id(card.getId())
                .userId(card.getUserId())
                .cardNumber(card.getCardNumber())
                .cardHolderName(card.getCardHolderName())
                .cardType(card.getCardType().name())
                .cardNetwork(card.getCardNetwork().name())
                .status(card.getStatus().name())
                .creditLimit(card.getCreditLimit())
                .availableCredit(card.getAvailableCredit())
                .usedCredit(card.getUsedCredit())
                .cashLimit(card.getCashLimit())
                .interestRate(card.getInterestRate())
                .billingCycle(card.getBillingCycle())
                .minimumPaymentPercent(card.getMinimumPaymentPercent())
                .currentBalance(card.getCurrentBalance())
                .outstandingAmount(card.getOutstandingAmount())
                .rewardPoints(card.getRewardPoints())
                .annualFee(card.getAnnualFee())
                .joiningFee(card.getJoiningFee())
                .foreignTransactionFee(card.getForeignTransactionFee())
                .expiryDate(card.getExpiryDate())
                .cvv(card.getCvv())
                .pinSet(card.getPinSet())
                .issuanceDate(card.getIssuanceDate())
                .createdAt(card.getCreatedAt())
                .updatedAt(card.getUpdatedAt())
                .build();
    }
}
