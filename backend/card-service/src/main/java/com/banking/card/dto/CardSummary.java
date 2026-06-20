package com.banking.card.dto;

import com.banking.card.model.Card;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardSummary {
    private String id;
    private String cardNumber;
    private String cardHolderName;
    private String cardType;
    private String cardNetwork;
    private String status;
    private BigDecimal availableCredit;
    private BigDecimal currentBalance;
    private LocalDate expiryDate;
    private Long rewardPoints;

    public static CardSummary fromCard(Card card) {
        return CardSummary.builder()
                .id(card.getId())
                .cardNumber(maskCardNumber(card.getCardNumber()))
                .cardHolderName(card.getCardHolderName())
                .cardType(card.getCardType().name())
                .cardNetwork(card.getCardNetwork().name())
                .status(card.getStatus().name())
                .availableCredit(card.getAvailableCredit())
                .currentBalance(card.getCurrentBalance())
                .expiryDate(card.getExpiryDate())
                .rewardPoints(card.getRewardPoints())
                .build();
    }

    private static String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 8) {
            return cardNumber;
        }
        return "XXXX-XXXX-XXXX-" + cardNumber.substring(cardNumber.length() - 4);
    }
}
