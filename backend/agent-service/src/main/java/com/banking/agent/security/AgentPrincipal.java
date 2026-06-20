package com.banking.agent.security;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AgentPrincipal {
    private String userId;
    private String username;
    private List<String> roles;
}
