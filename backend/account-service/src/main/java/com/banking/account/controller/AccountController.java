package com.banking.account.controller;

import com.banking.account.dto.AccountRequest;
import com.banking.account.dto.AccountResponse;
import com.banking.account.dto.AccountSummary;
import com.banking.account.model.Account;
import com.banking.account.model.Account;
import com.banking.account.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody AccountRequest request) {
        AccountResponse response = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable String id) {
        AccountResponse response = accountService.getAccount(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/number/{accountNumber}")
    public ResponseEntity<AccountResponse> getAccountByNumber(@PathVariable String accountNumber) {
        AccountResponse response = accountService.getAccountByNumber(accountNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AccountSummary>> getUserAccounts(@PathVariable String userId) {
        List<AccountSummary> accounts = accountService.getUserAccounts(userId);
        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/user/{userId}/active")
    public ResponseEntity<List<AccountSummary>> getUserActiveAccounts(@PathVariable String userId) {
        List<AccountSummary> accounts = accountService.getUserActiveAccounts(userId);
        return ResponseEntity.ok(accounts);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AccountResponse> updateAccount(
            @PathVariable String id,
            @Valid @RequestBody AccountRequest request) {
        AccountResponse response = accountService.updateAccount(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<AccountResponse> updateAccountStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> request) {
        Account.AccountStatus status = Account.AccountStatus.valueOf(request.get("status"));
        AccountResponse response = accountService.updateAccountStatus(id, status);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable String id) {
        accountService.deleteAccount(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}/balance")
    public ResponseEntity<Map<String, Object>> getTotalBalance(@PathVariable String userId) {
        BigDecimal totalBalance = accountService.getTotalBalance(userId);
        long accountCount = accountService.getAccountCount(userId);
        return ResponseEntity.ok(Map.of(
                "totalBalance", totalBalance,
                "accountCount", accountCount,
                "userId", userId
        ));
    }

    @PatchMapping("/{id}/debit")
    public ResponseEntity<AccountResponse> debitAccount(
            @PathVariable String id,
            @RequestBody Map<String, BigDecimal> request) {
        Account account = accountService.findAccountById(id);
        account.debit(request.get("amount"));
        account = accountService.saveAccount(account);
        return ResponseEntity.ok(AccountResponse.fromAccount(account));
    }

    @PatchMapping("/{id}/credit")
    public ResponseEntity<AccountResponse> creditAccount(
            @PathVariable String id,
            @RequestBody Map<String, BigDecimal> request) {
        Account account = accountService.findAccountById(id);
        account.credit(request.get("amount"));
        account = accountService.saveAccount(account);
        return ResponseEntity.ok(AccountResponse.fromAccount(account));
    }
}
