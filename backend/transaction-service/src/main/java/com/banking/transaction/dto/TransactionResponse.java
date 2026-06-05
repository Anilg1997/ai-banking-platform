package com.banking.transaction.dto;

import com.banking.transaction.model.Transaction;
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
public class TransactionResponse {
    private String id;
    private String transactionRef;
    private String type;
    private String status;
    private BigDecimal amount;
    private String currency;
    private String fromAccountId;
    private String fromUserId;
    private String fromAccountNumber;
    private String fromAccountName;
    private String toAccountId;
    private String toUserId;
    private String toAccountNumber;
    private String toAccountName;
    private String toEmail;
    private String description;
    private String category;
    private BigDecimal feeAmount;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public static TransactionResponse fromTransaction(Transaction txn) {
        return TransactionResponse.builder()
                .id(txn.getId())
                .transactionRef(txn.getTransactionRef())
                .type(txn.getType().name())
                .status(txn.getStatus().name())
                .amount(txn.getAmount())
                .currency(txn.getCurrency())
                .fromAccountId(txn.getFromAccountId())
                .fromUserId(txn.getFromUserId())
                .fromAccountNumber(txn.getFromAccountNumber())
                .fromAccountName(txn.getFromAccountName())
                .toAccountId(txn.getToAccountId())
                .toUserId(txn.getToUserId())
                .toAccountNumber(txn.getToAccountNumber())
                .toAccountName(txn.getToAccountName())
                .toEmail(txn.getToEmail())
                .description(txn.getDescription())
                .category(txn.getCategory())
                .feeAmount(txn.getFeeAmount())
                .failureReason(txn.getFailureReason())
                .createdAt(txn.getCreatedAt())
                .completedAt(txn.getCompletedAt())
                .build();
    }
}
