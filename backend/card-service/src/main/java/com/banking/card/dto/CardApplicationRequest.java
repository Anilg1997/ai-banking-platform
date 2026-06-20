package com.banking.card.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardApplicationRequest {

    @NotBlank(message = "Card type is required")
    private String cardType;

    @NotBlank(message = "Card network is required")
    private String cardNetwork;

    @NotNull(message = "Requested credit limit is required")
    @DecimalMin(value = "1000", message = "Minimum credit limit is 1000")
    @DecimalMax(value = "10000000", message = "Maximum credit limit is 10,000,000")
    private BigDecimal requestedCreditLimit;

    @NotNull(message = "Annual income is required")
    @DecimalMin(value = "0", message = "Annual income must be positive")
    private BigDecimal annualIncome;

    @NotBlank(message = "Employment type is required")
    private String employmentType;

    @Size(max = 100, message = "Employer name must not exceed 100 characters")
    private String employerName;

    @Size(max = 100, message = "Designation must not exceed 100 characters")
    private String designation;

    private BigDecimal monthlyIncome;

    @NotBlank(message = "Address line 1 is required")
    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String addressLine1;

    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String addressLine2;

    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @NotBlank(message = "State is required")
    @Size(max = 100, message = "State must not exceed 100 characters")
    private String state;

    @NotBlank(message = "Pincode is required")
    @Size(min = 5, max = 10, message = "Pincode must be between 5 and 10 characters")
    private String pincode;

    @NotBlank(message = "Aadhar number is required")
    private String aadharNumber;

    @NotBlank(message = "PAN number is required")
    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$", message = "Invalid PAN number format")
    private String panNumber;
}
