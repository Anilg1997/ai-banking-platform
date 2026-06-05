package com.banking.account.dto;

import com.banking.account.model.Account;
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
public class AccountRequest {

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "Account name is required")
    @Size(min = 3, max = 100, message = "Account name must be between 3 and 100 characters")
    private String accountName;

    @NotNull(message = "Account type is required")
    private Account.AccountType accountType;

    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a 3-letter ISO code (e.g., USD, EUR)")
    private String currency;

    @Builder.Default
    private BigDecimal initialDeposit = BigDecimal.ZERO;

    @Builder.Default
    private boolean allowOverdraft = false;

    private BigDecimal overdraftLimit;

    private String branchCode;

    private String branchName;
}
