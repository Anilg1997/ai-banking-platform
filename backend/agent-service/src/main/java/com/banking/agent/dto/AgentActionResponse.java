package com.banking.agent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentActionResponse {

    private String actionType;
    private String status;
    private String toolName;
    private String input;
    private String output;
    private Long duration;
}
