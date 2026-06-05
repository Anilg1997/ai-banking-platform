package com.banking.account.service;

import com.banking.account.dto.AccountRequest;
import com.banking.account.dto.AccountResponse;
import com.banking.account.dto.AccountSummary;
import com.banking.account.model.Account;
import com.banking.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Currency;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;
    private final Random random = new Random();

    public AccountResponse createAccount(AccountRequest request) {
        Currency currency;
        try {
            currency = Currency.getInstance(request.getCurrency().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid currency code: " + request.getCurrency());
        }

        Account account = Account.builder()
                .accountNumber(generateAccountNumber())
                .userId(request.getUserId())
                .accountName(request.getAccountName())
                .accountType(request.getAccountType())
                .status(Account.AccountStatus.PENDING)
                .balance(request.getInitialDeposit())
                .availableBalance(request.getInitialDeposit())
                .holdsBalance(BigDecimal.ZERO)
                .currency(currency)
                .interestRate(getDefaultInterestRate(request.getAccountType()))
                .branchCode(request.getBranchCode() != null ? request.getBranchCode() : "BR001")
                .branchName(request.getBranchName() != null ? request.getBranchName() : "Main Branch")
                .allowOverdraft(request.isAllowOverdraft())
                .overdraftLimit(request.getOverdraftLimit() != null ? request.getOverdraftLimit() : BigDecimal.ZERO)
                .build();

        account = accountRepository.save(account);
        log.info("Account created: {} for user {}", account.getAccountNumber(), request.getUserId());

        return AccountResponse.fromAccount(account);
    }

    public AccountResponse getAccount(String id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found with id: " + id));
        return AccountResponse.fromAccount(account);
    }

    public AccountResponse getAccountByNumber(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountNumber));
        return AccountResponse.fromAccount(account);
    }

    public List<AccountSummary> getUserAccounts(String userId) {
        List<Account> accounts = accountRepository.findByUserId(userId);
        return accounts.stream()
                .map(AccountSummary::fromAccount)
                .collect(Collectors.toList());
    }

    public List<AccountSummary> getUserActiveAccounts(String userId) {
        List<Account> accounts = accountRepository.findByUserIdAndStatus(userId, Account.AccountStatus.ACTIVE);
        return accounts.stream()
                .map(AccountSummary::fromAccount)
                .collect(Collectors.toList());
    }

    public AccountResponse updateAccount(String id, AccountRequest request) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found with id: " + id));

        if (request.getAccountName() != null) {
            account.setAccountName(request.getAccountName());
        }
        if (request.getBranchCode() != null) {
            account.setBranchCode(request.getBranchCode());
        }
        if (request.getBranchName() != null) {
            account.setBranchName(request.getBranchName());
        }

        account = accountRepository.save(account);
        log.info("Account updated: {}", account.getAccountNumber());

        return AccountResponse.fromAccount(account);
    }

    public AccountResponse updateAccountStatus(String id, Account.AccountStatus status) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found with id: " + id));

        if (status == Account.AccountStatus.CLOSED) {
            account.setClosedAt(LocalDateTime.now());
        }
        account.setStatus(status);
        account = accountRepository.save(account);

        log.info("Account {} status updated to: {}", account.getAccountNumber(), status);
        return AccountResponse.fromAccount(account);
    }

    public void deleteAccount(String id) {
        if (!accountRepository.existsById(id)) {
            throw new IllegalArgumentException("Account not found with id: " + id);
        }
        accountRepository.deleteById(id);
        log.info("Account deleted: {}", id);
    }

    public BigDecimal getTotalBalance(String userId) {
        List<Account> accounts = accountRepository.findByUserIdAndStatus(userId, Account.AccountStatus.ACTIVE);
        return accounts.stream()
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public long getAccountCount(String userId) {
        return accountRepository.countByUserId(userId);
    }

    public Account findAccountById(String id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found with id: " + id));
    }

    public Account saveAccount(Account account) {
        return accountRepository.save(account);
    }

    private String generateAccountNumber() {
        String prefix = "NOVA";
        String timestamp = String.valueOf(System.currentTimeMillis() % 1000000);
        String randomNum = String.format("%04d", random.nextInt(10000));
        String number = prefix + timestamp + randomNum;
        // Ensure uniqueness
        while (accountRepository.existsByAccountNumber(number)) {
            randomNum = String.format("%04d", random.nextInt(10000));
            number = prefix + timestamp + randomNum;
        }
        return number;
    }

    private BigDecimal getDefaultInterestRate(Account.AccountType type) {
        return switch (type) {
            case SAVINGS -> BigDecimal.valueOf(4.5);
            case FIXED_DEPOSIT -> BigDecimal.valueOf(7.0);
            case CHECKING -> BigDecimal.valueOf(0.5);
            case BUSINESS -> BigDecimal.valueOf(2.0);
            case INVESTMENT -> BigDecimal.valueOf(6.0);
            case CREDIT -> BigDecimal.ZERO;
        };
    }
}
