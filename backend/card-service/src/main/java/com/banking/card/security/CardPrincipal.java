package com.banking.card.security;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.security.Principal;
import java.util.List;

@Data
@AllArgsConstructor
public class CardPrincipal implements Principal {
    private String userId;
    private String username;
    private List<String> roles;

    @Override
    public String getName() {
        return userId;
    }
}
