package com.banking.account.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;

@Document(collection = "accounts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    private String id;

    @Indexed(unique = true)
    private String accountNumber;

    @Indexed
    private String userId;

    private String accountName;

    private AccountType accountType;

    private AccountStatus status;

    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal availableBalance = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal holdsBalance = BigDecimal.ZERO;

    private Currency currency;

    @Builder.Default
    private BigDecimal interestRate = BigDecimal.ZERO;

    private String branchCode;

    private String branchName;

    @Builder.Default
    private boolean allowOverdraft = false;

    @Builder.Default
    private BigDecimal overdraftLimit = BigDecimal.ZERO;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime closedAt;

    public enum AccountType {
        CHECKING,
        SAVINGS,
        BUSINESS,
        CREDIT,
        INVESTMENT,
        FIXED_DEPOSIT
    }

    public enum AccountStatus {
        PENDING,
        ACTIVE,
        FROZEN,
        CLOSED,
        SUSPENDED
    }

    public void credit(BigDecimal amount) {
        this.balance = this.balance.add(amount);
        this.availableBalance = this.availableBalance.add(amount);
    }

    public void debit(BigDecimal amount) {
        BigDecimal newBalance = this.balance.subtract(amount);
        BigDecimal newAvailable = this.availableBalance.subtract(amount);

        if (!allowOverdraft && newAvailable.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Insufficient funds");
        }
        if (allowOverdraft && newAvailable.compareTo(overdraftLimit.negate()) < 0) {
            throw new IllegalStateException("Overdraft limit exceeded");
        }

        this.balance = newBalance;
        this.availableBalance = newAvailable;
    }

    public void placeHold(BigDecimal amount) {
        if (availableBalance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient available balance for hold");
        }
        this.holdsBalance = this.holdsBalance.add(amount);
        this.availableBalance = this.availableBalance.subtract(amount);
    }

    public void releaseHold(BigDecimal amount) {
        this.holdsBalance = this.holdsBalance.subtract(amount);
        this.availableBalance = this.availableBalance.add(amount);
    }
}
