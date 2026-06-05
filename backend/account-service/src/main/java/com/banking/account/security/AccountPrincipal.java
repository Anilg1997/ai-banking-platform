package com.banking.account.security;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AccountPrincipal {
    private String userId;
    private String username;
    private List<String> roles;
}
