package com.banking.transaction.controller;

import com.banking.transaction.dto.TransactionResponse;
import com.banking.transaction.dto.TransactionSummary;
import com.banking.transaction.dto.TransferRequest;
import com.banking.transaction.service.TransactionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(
            @Valid @RequestBody TransferRequest request,
            HttpServletRequest httpRequest) {
        TransactionResponse response = transactionService.transfer(
                request,
                httpRequest.getRemoteAddr(),
                httpRequest.getHeader("User-Agent")
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransaction(@PathVariable String id) {
        TransactionResponse response = transactionService.getTransaction(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ref/{reference}")
    public ResponseEntity<TransactionResponse> getByReference(@PathVariable String reference) {
        TransactionResponse response = transactionService.getByReference(reference);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TransactionSummary>> getUserTransactions(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        List<TransactionSummary> transactions = transactionService.getUserTransactions(userId, page, size);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/user/{userId}/recent")
    public ResponseEntity<List<TransactionSummary>> getRecentTransactions(
            @PathVariable String userId,
            @RequestParam(defaultValue = "10") int limit) {
        List<TransactionSummary> transactions = transactionService.getRecentTransactions(userId, limit);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/user/{userId}/range")
    public ResponseEntity<List<TransactionSummary>> getTransactionsByDateRange(
            @PathVariable String userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        List<TransactionSummary> transactions = transactionService.getDateRangeTransactions(userId, start, end);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<TransactionSummary>> getAccountTransactions(@PathVariable String accountId) {
        List<TransactionSummary> transactions = transactionService.getAccountTransactions(accountId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/user/{userId}/count")
    public ResponseEntity<Map<String, Object>> getTransactionCount(@PathVariable String userId) {
        long count = transactionService.getTransactionCount(userId);
        return ResponseEntity.ok(Map.of("count", count, "userId", userId));
    }

    @PostMapping("/{id}/reverse")
    public ResponseEntity<TransactionResponse> reverseTransaction(@PathVariable String id) {
        TransactionResponse response = transactionService.reverseTransaction(id);
        return ResponseEntity.ok(response);
    }
}
