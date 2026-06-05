package com.banking.account.dto;

import com.banking.account.model.Account;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountSummary {
    private String id;
    private String accountNumber;
    private String accountName;
    private String accountType;
    private String status;
    private BigDecimal balance;
    private BigDecimal availableBalance;
    private String currency;
    private boolean allowOverdraft;

    public static AccountSummary fromAccount(Account account) {
        return AccountSummary.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .accountName(account.getAccountName())
                .accountType(account.getAccountType().name())
                .status(account.getStatus().name())
                .balance(account.getBalance())
                .availableBalance(account.getAvailableBalance())
                .currency(account.getCurrency().getCurrencyCode())
                .allowOverdraft(account.isAllowOverdraft())
                .build();
    }
}
