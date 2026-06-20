package com.banking.agent.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentResponse {

    private String reply;
    private UUID conversationId;
    private String agentType;
    private boolean usedRag;
    private boolean usedTools;
    private List<AgentActionResponse> actions;
    private List<SourceInfo> sources;
}
