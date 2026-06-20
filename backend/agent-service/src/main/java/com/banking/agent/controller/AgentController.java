package com.banking.agent.controller;

import com.banking.agent.dto.AgentRequest;
import com.banking.agent.dto.AgentResponse;
import com.banking.agent.dto.ToolDefinition;
import com.banking.agent.model.AgentConversation;
import com.banking.agent.security.AgentPrincipal;
import com.banking.agent.service.AgentOrchestratorService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
public class AgentController {

    private final AgentOrchestratorService orchestratorService;

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return "";
    }

    @PostMapping("/chat")
    public ResponseEntity<AgentResponse> chat(
            @Valid @RequestBody AgentRequest request,
            @AuthenticationPrincipal AgentPrincipal principal,
            HttpServletRequest httpRequest) {
        String jwtToken = extractToken(httpRequest);
        AgentResponse response = orchestratorService.processMessage(request, principal, jwtToken);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/conversations/new")
    public ResponseEntity<Map<String, Object>> createConversation(
            @AuthenticationPrincipal AgentPrincipal principal,
            @RequestParam(defaultValue = "GENERAL") String agentType) {
        AgentConversation conversation = orchestratorService.createNewConversation(
                UUID.fromString(principal.getUserId()), agentType);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "id", conversation.getId(),
                        "agentType", conversation.getAgentType().name(),
                        "status", conversation.getStatus().name(),
                        "sessionId", conversation.getSessionId(),
                        "createdAt", conversation.getCreatedAt()
                ));
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<AgentConversation>> getConversations(
            @AuthenticationPrincipal AgentPrincipal principal) {
        List<AgentConversation> conversations = orchestratorService
                .getUserConversations(UUID.fromString(principal.getUserId()));
        return ResponseEntity.ok(conversations);
    }

    @GetMapping("/conversations/{id}")
    public ResponseEntity<AgentConversation> getConversation(@PathVariable UUID id) {
        return orchestratorService.getConversation(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/conversations/{id}")
    public ResponseEntity<Void> deleteConversation(@PathVariable UUID id) {
        orchestratorService.deleteConversation(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/analyze")
    public ResponseEntity<AgentResponse> analyze(
            @Valid @RequestBody AgentRequest request,
            @AuthenticationPrincipal AgentPrincipal principal,
            HttpServletRequest httpRequest) {
        String jwtToken = extractToken(httpRequest);
        request.setUseTools(true);
        request.setUseRag(true);
        AgentResponse response = orchestratorService.processMessage(request, principal, jwtToken);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tools")
    public ResponseEntity<List<ToolDefinition>> listTools() {
        return ResponseEntity.ok(orchestratorService.listTools());
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(orchestratorService.healthCheck());
    }
}
