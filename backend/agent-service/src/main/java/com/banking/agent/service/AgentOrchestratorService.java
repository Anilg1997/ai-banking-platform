package com.banking.agent.service;

import com.banking.agent.dto.AgentActionResponse;
import com.banking.agent.dto.AgentRequest;
import com.banking.agent.dto.AgentResponse;
import com.banking.agent.dto.SourceInfo;
import com.banking.agent.model.AgentAction;
import com.banking.agent.model.AgentConversation;
import com.banking.agent.repository.AgentActionRepository;
import com.banking.agent.repository.AgentConversationRepository;
import com.banking.agent.security.AgentPrincipal;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import com.banking.agent.dto.ToolDefinition;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgentOrchestratorService {

    private final AgentConversationRepository conversationRepository;
    private final AgentActionRepository actionRepository;
    private final LangChain4jService langChain4jService;
    private final RagService ragService;
    private final McpToolExecutor toolExecutor;
    private final AgentPromptBuilder promptBuilder;
    private final ObjectMapper objectMapper;

    @Transactional
    public AgentResponse processMessage(AgentRequest request, AgentPrincipal principal, String jwtToken) {
        UUID userId = UUID.fromString(principal.getUserId());
        AgentConversation.AgentType agentType = parseAgentType(request.getAgentType());

        AgentConversation conversation = getOrCreateConversation(request, userId, agentType);

        String conversationHistory = conversation.getMessages();

        String ragContext = null;
        List<SourceInfo> sources = new ArrayList<>();
        if (request.isUseRag()) {
            sources = ragService.searchSimilar(request.getMessage(), 3);
            ragContext = sources.stream()
                    .map(s -> s.getTitle() + ": " + s.getContent())
                    .collect(Collectors.joining("\n"));
        }

        String systemPrompt = promptBuilder.buildSystemPrompt(agentType, ragContext, request.isUseTools());
        String taskPrompt = promptBuilder.buildTaskPrompt(request.getMessage(), conversationHistory);

        String llmResponse = langChain4jService.chat(systemPrompt, taskPrompt);

        List<Map<String, Object>> toolCalls = langChain4jService.parseToolCalls(llmResponse);
        String responseText = langChain4jService.extractResponseText(llmResponse);

        List<AgentActionResponse> actionResponses = new ArrayList<>();

        if (!toolCalls.isEmpty() && request.isUseTools()) {
            for (Map<String, Object> toolCall : toolCalls) {
                String toolName = (String) toolCall.get("name");
                @SuppressWarnings("unchecked")
                Map<String, Object> arguments = (Map<String, Object>) toolCall.getOrDefault("arguments", new HashMap<>());

                AgentAction action = AgentAction.builder()
                        .conversationId(conversation.getId())
                        .userId(userId)
                        .actionType(toolName)
                        .status(AgentAction.ActionStatus.EXECUTING)
                        .input(toJsonString(arguments))
                        .toolName(toolName)
                        .build();
                action = actionRepository.save(action);

                long startTime = System.currentTimeMillis();
                try {
                    Map<String, Object> toolResult = toolExecutor.executeTool(toolName, arguments, jwtToken);

                    action.setStatus(AgentAction.ActionStatus.COMPLETED);
                    action.setOutput(toJsonString(toolResult));
                    action.setDuration(System.currentTimeMillis() - startTime);
                    action.setCompletedAt(LocalDateTime.now());

                    actionResponses.add(AgentActionResponse.builder()
                            .actionType(toolName)
                            .status("COMPLETED")
                            .toolName(toolName)
                            .input(toJsonString(arguments))
                            .output(toJsonString(toolResult))
                            .duration(System.currentTimeMillis() - startTime)
                            .build());

                } catch (Exception e) {
                    action.setStatus(AgentAction.ActionStatus.FAILED);
                    action.setError(e.getMessage());
                    action.setDuration(System.currentTimeMillis() - startTime);
                    action.setCompletedAt(LocalDateTime.now());

                    actionResponses.add(AgentActionResponse.builder()
                            .actionType(toolName)
                            .status("FAILED")
                            .toolName(toolName)
                            .input(toJsonString(arguments))
                            .output(e.getMessage())
                            .duration(System.currentTimeMillis() - startTime)
                            .build());
                }
                actionRepository.save(action);
            }

            if (!actionResponses.isEmpty()) {
                String toolResultsPrompt = "The following tool calls were executed with these results:\n";
                for (AgentActionResponse actionResp : actionResponses) {
                    toolResultsPrompt += "- " + actionResp.getActionType() + ": " + actionResp.getOutput() + "\n";
                }
                toolResultsPrompt += "\nPlease provide a final response to the user incorporating these results.";

                String finalLlmResponse = langChain4jService.chat(systemPrompt, toolResultsPrompt);
                responseText = langChain4jService.extractResponseText(finalLlmResponse);
            }
        }

        String updatedHistory = updateConversationHistory(conversationHistory, request.getMessage(), responseText);
        conversation.setMessages(updatedHistory);
        conversation.setLastActivityAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        return AgentResponse.builder()
                .reply(responseText)
                .conversationId(conversation.getId())
                .agentType(agentType.name())
                .usedRag(request.isUseRag() && !sources.isEmpty())
                .usedTools(request.isUseTools() && !toolCalls.isEmpty())
                .actions(actionResponses)
                .sources(sources)
                .build();
    }

    @Transactional
    public AgentConversation createNewConversation(UUID userId, String agentType) {
        AgentConversation conversation = AgentConversation.builder()
                .userId(userId)
                .title("New conversation")
                .messages("[]")
                .agentType(parseAgentType(agentType))
                .sessionId(UUID.randomUUID().toString())
                .status(AgentConversation.ConversationStatus.ACTIVE)
                .build();
        return conversationRepository.save(conversation);
    }

    public List<AgentConversation> getUserConversations(UUID userId) {
        return conversationRepository.findByUserIdOrderByLastActivityAtDesc(userId);
    }

    public Optional<AgentConversation> getConversation(UUID id) {
        return conversationRepository.findById(id);
    }

    public void deleteConversation(UUID id) {
        conversationRepository.deleteById(id);
    }

    public Map<String, Object> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        try {
            langChain4jService.chat("System check", "Respond with OK");
            health.put("llm", "UP");
        } catch (Exception e) {
            health.put("llm", "DOWN - " + e.getMessage());
        }
        try {
            ragService.initializeVectorStore();
            health.put("chromadb", "UP");
        } catch (Exception e) {
            health.put("chromadb", "DOWN - " + e.getMessage());
        }
        health.put("status", "UP".equals(health.get("llm")) ? "UP" : "DEGRADED");
        return health;
    }

    public List<AgentConversation> getAllConversations() {
        return conversationRepository.findAll();
    }

    public List<AgentAction> getAllActions() {
        return actionRepository.findAll();
    }

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalConversations", conversationRepository.count());
        stats.put("totalActions", actionRepository.count());

        List<AgentAction> failedActions = actionRepository.findByStatus(AgentAction.ActionStatus.FAILED);
        List<AgentAction> completedActions = actionRepository.findByStatus(AgentAction.ActionStatus.COMPLETED);
        stats.put("failedActions", failedActions.size());
        stats.put("completedActions", completedActions.size());

        if (!completedActions.isEmpty()) {
            double avgDuration = completedActions.stream()
                    .filter(a -> a.getDuration() != null)
                    .mapToLong(AgentAction::getDuration)
                    .average()
                    .orElse(0);
            stats.put("averageActionDurationMs", avgDuration);
        }

        return stats;
    }

    private AgentConversation getOrCreateConversation(AgentRequest request, UUID userId, AgentConversation.AgentType agentType) {
        if (request.getConversationId() != null) {
            return conversationRepository.findById(request.getConversationId())
                    .orElseGet(() -> createNewConversation(userId, request.getAgentType()));
        }
        return createNewConversation(userId, request.getAgentType());
    }

    private AgentConversation.AgentType parseAgentType(String type) {
        try {
            return AgentConversation.AgentType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return AgentConversation.AgentType.GENERAL;
        }
    }

    private String updateConversationHistory(String existingHistory, String userMessage, String agentResponse) {
        try {
            List<Map<String, String>> messages = new ArrayList<>();
            if (existingHistory != null && !existingHistory.isEmpty() && !existingHistory.equals("[]")) {
                try {
                    var existingMessages = objectMapper.readValue(existingHistory, List.class);
                    for (Object msg : existingMessages) {
                        if (msg instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, String> m = (Map<String, String>) msg;
                            messages.add(m);
                        }
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse existing history, starting fresh");
                }
            }

            Map<String, String> userEntry = new HashMap<>();
            userEntry.put("role", "user");
            userEntry.put("content", userMessage);
            userEntry.put("timestamp", LocalDateTime.now().toString());
            messages.add(userEntry);

            Map<String, String> agentEntry = new HashMap<>();
            agentEntry.put("role", "assistant");
            agentEntry.put("content", agentResponse);
            agentEntry.put("timestamp", LocalDateTime.now().toString());
            messages.add(agentEntry);

            if (messages.size() > 50) {
                messages = messages.subList(messages.size() - 50, messages.size());
            }

            return objectMapper.writeValueAsString(messages);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize conversation history", e);
            return "[]";
        }
    }

    private String toJsonString(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return obj != null ? obj.toString() : "null";
        }
    }

    public List<ToolDefinition> listTools() {
        return toolExecutor.listTools();
    }

    private List<AgentAction> getActionsWithConversationIds(UUID userId, LocalDateTime start, LocalDateTime end) {
        return actionRepository.findByUserIdAndCreatedAtBetween(userId, start, end);
    }
}
