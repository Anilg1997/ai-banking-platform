package com.banking.agent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentRequest {

    @NotBlank(message = "Message is required")
    @Size(min = 1, max = 2000, message = "Message must be between 1 and 2000 characters")
    private String message;

    private UUID conversationId;

    @Builder.Default
    private String agentType = "GENERAL";

    @Builder.Default
    private boolean useRag = true;

    @Builder.Default
    private boolean useTools = true;
}
