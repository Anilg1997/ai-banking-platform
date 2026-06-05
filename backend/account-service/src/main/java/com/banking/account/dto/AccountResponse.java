package com.banking.account.dto;

import com.banking.account.model.Account;
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
public class AccountResponse {
    private String id;
    private String accountNumber;
    private String userId;
    private String accountName;
    private String accountType;
    private String status;
    private BigDecimal balance;
    private BigDecimal availableBalance;
    private BigDecimal holdsBalance;
    private String currency;
    private BigDecimal interestRate;
    private String branchCode;
    private String branchName;
    private boolean allowOverdraft;
    private BigDecimal overdraftLimit;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AccountResponse fromAccount(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .userId(account.getUserId())
                .accountName(account.getAccountName())
                .accountType(account.getAccountType().name())
                .status(account.getStatus().name())
                .balance(account.getBalance())
                .availableBalance(account.getAvailableBalance())
                .holdsBalance(account.getHoldsBalance())
                .currency(account.getCurrency().getCurrencyCode())
                .interestRate(account.getInterestRate())
                .branchCode(account.getBranchCode())
                .branchName(account.getBranchName())
                .allowOverdraft(account.isAllowOverdraft())
                .overdraftLimit(account.getOverdraftLimit())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}
