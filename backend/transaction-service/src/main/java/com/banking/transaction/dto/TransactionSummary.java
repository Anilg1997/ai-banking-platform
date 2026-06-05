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
public class TransactionSummary {
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
    private String description;
    private String category;
    private LocalDateTime createdAt;

    public static TransactionSummary fromTransaction(Transaction txn) {
        return TransactionSummary.builder()
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
                .description(txn.getDescription())
                .category(txn.getCategory())
                .createdAt(txn.getCreatedAt())
                .build();
    }
}
