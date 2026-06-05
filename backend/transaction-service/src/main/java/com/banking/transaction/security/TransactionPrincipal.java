package com.banking.transaction.security;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class TransactionPrincipal {
    private String userId;
    private String username;
    private List<String> roles;
}
