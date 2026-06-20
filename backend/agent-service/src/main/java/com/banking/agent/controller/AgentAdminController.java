package com.banking.agent.controller;

import com.banking.agent.model.AgentAction;
import com.banking.agent.model.AgentConversation;
import com.banking.agent.model.AgentPrompt;
import com.banking.agent.model.RagDocument;
import com.banking.agent.repository.AgentPromptRepository;
import com.banking.agent.service.AgentOrchestratorService;
import com.banking.agent.service.RagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/agent")
@RequiredArgsConstructor
public class AgentAdminController {

    private final AgentOrchestratorService orchestratorService;
    private final AgentPromptRepository promptRepository;
    private final RagService ragService;

    @GetMapping("/conversations")
    public ResponseEntity<List<AgentConversation>> getAllConversations() {
        return ResponseEntity.ok(orchestratorService.getAllConversations());
    }

    @GetMapping("/actions")
    public ResponseEntity<List<AgentAction>> getAllActions() {
        return ResponseEntity.ok(orchestratorService.getAllActions());
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(orchestratorService.getStats());
    }

    @PostMapping("/prompts")
    public ResponseEntity<AgentPrompt> createPrompt(@RequestBody AgentPrompt prompt) {
        AgentPrompt saved = promptRepository.save(prompt);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/prompts/{id}")
    public ResponseEntity<AgentPrompt> updatePrompt(@PathVariable UUID id, @RequestBody AgentPrompt prompt) {
        return promptRepository.findById(id)
                .map(existing -> {
                    existing.setName(prompt.getName());
                    existing.setPromptType(prompt.getPromptType());
                    existing.setContent(prompt.getContent());
                    existing.setVariables(prompt.getVariables());
                    existing.setActive(prompt.isActive());
                    existing.setVersion(existing.getVersion() + 1);
                    return ResponseEntity.ok(promptRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/prompts")
    public ResponseEntity<List<AgentPrompt>> listPrompts() {
        return ResponseEntity.ok(promptRepository.findAll());
    }

    @PostMapping("/knowledge/refresh")
    public ResponseEntity<Map<String, String>> refreshKnowledge() {
        ragService.initializeVectorStore();
        ragService.batchAddKnowledgeBase();
        return ResponseEntity.ok(Map.of("status", "Knowledge base refreshed successfully"));
    }

    @PostMapping("/knowledge/upload")
    public ResponseEntity<Map<String, String>> uploadDocument(@RequestBody Map<String, String> request) {
        String title = request.get("title");
        String content = request.get("content");
        String category = request.getOrDefault("category", "UPLOADED");
        String source = request.getOrDefault("source", "manual_upload");
        ragService.addDocument(title, content, category, source);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("status", "Document uploaded successfully", "title", title));
    }

    @GetMapping("/knowledge/documents")
    public ResponseEntity<List<RagDocument>> listDocuments() {
        return ResponseEntity.ok(ragService.getAllDocuments());
    }

    @DeleteMapping("/knowledge/documents/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable UUID id) {
        ragService.deleteDocument(id.toString());
        return ResponseEntity.noContent().build();
    }
}
