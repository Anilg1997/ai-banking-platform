package com.banking.card.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "card_applications", indexes = {
    @Index(name = "idx_card_app_user_id", columnList = "userId"),
    @Index(name = "idx_card_app_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplicationStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Card.CardType cardType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Card.CardNetwork cardNetwork;

    @Column(precision = 19, scale = 2)
    private BigDecimal requestedCreditLimit;

    @Column(precision = 19, scale = 2)
    private BigDecimal annualIncome;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private EmploymentType employmentType;

    @Column(length = 100)
    private String employerName;

    @Column(length = 100)
    private String designation;

    @Column(precision = 19, scale = 2)
    private BigDecimal monthlyIncome;

    @Column(length = 255)
    private String addressLine1;

    @Column(length = 255)
    private String addressLine2;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(length = 10)
    private String pincode;

    @Column(length = 255)
    private String aadharNumber;

    @Column(length = 50)
    private String panNumber;

    private Integer creditScore;

    @Column(length = 50)
    private String reviewedBy;

    @Column(length = 500)
    private String reviewNotes;

    @Column(length = 500)
    private String rejectionReason;

    private LocalDateTime submittedAt;

    private LocalDateTime reviewedAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum ApplicationStatus {
        DRAFT, SUBMITTED, UNDER_REVIEW, APPROVED, REJECTED, CANCELLED
    }

    public enum EmploymentType {
        SALARIED, SELF_EMPLOYED, BUSINESS, STUDENT, RETIRED
    }
}
