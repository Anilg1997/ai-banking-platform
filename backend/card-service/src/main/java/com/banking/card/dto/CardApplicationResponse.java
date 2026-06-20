package com.banking.card.dto;

import com.banking.card.model.CardApplication;
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
public class CardApplicationResponse {
    private String id;
    private String userId;
    private String status;
    private String cardType;
    private String cardNetwork;
    private BigDecimal requestedCreditLimit;
    private BigDecimal annualIncome;
    private String employmentType;
    private String employerName;
    private String designation;
    private BigDecimal monthlyIncome;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String pincode;
    private Integer creditScore;
    private String reviewedBy;
    private String reviewNotes;
    private String rejectionReason;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CardApplicationResponse fromApplication(CardApplication app) {
        return CardApplicationResponse.builder()
                .id(app.getId())
                .userId(app.getUserId())
                .status(app.getStatus().name())
                .cardType(app.getCardType().name())
                .cardNetwork(app.getCardNetwork().name())
                .requestedCreditLimit(app.getRequestedCreditLimit())
                .annualIncome(app.getAnnualIncome())
                .employmentType(app.getEmploymentType() != null ? app.getEmploymentType().name() : null)
                .employerName(app.getEmployerName())
                .designation(app.getDesignation())
                .monthlyIncome(app.getMonthlyIncome())
                .addressLine1(app.getAddressLine1())
                .addressLine2(app.getAddressLine2())
                .city(app.getCity())
                .state(app.getState())
                .pincode(app.getPincode())
                .creditScore(app.getCreditScore())
                .reviewedBy(app.getReviewedBy())
                .reviewNotes(app.getReviewNotes())
                .rejectionReason(app.getRejectionReason())
                .submittedAt(app.getSubmittedAt())
                .reviewedAt(app.getReviewedAt())
                .createdAt(app.getCreatedAt())
                .updatedAt(app.getUpdatedAt())
                .build();
    }
}
