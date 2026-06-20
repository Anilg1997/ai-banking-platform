package com.banking.agent.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import dev.langchain4j.model.ollama.OllamaChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.model.output.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class LangChain4jService {

    private final OllamaChatLanguageModel chatLanguageModel;
    private final OllamaEmbeddingModel embeddingModel;
    private final ObjectMapper objectMapper;

    public String chat(String systemPrompt, String userPrompt) {
        try {
            String fullPrompt = systemPrompt + "\n\n" + userPrompt;
            Response<String> response = chatLanguageModel.generate(fullPrompt);
            return response.content();
        } catch (Exception e) {
            log.error("Error calling Ollama LLM: {}", e.getMessage());
            throw new RuntimeException("Failed to get LLM response: " + e.getMessage(), e);
        }
    }

    public float[] generateEmbeddings(String text) {
        try {
            Response<float[]> response = embeddingModel.embed(text);
            return response.content();
        } catch (Exception e) {
            log.error("Error generating embeddings: {}", e.getMessage());
            throw new RuntimeException("Failed to generate embeddings: " + e.getMessage(), e);
        }
    }

    public List<Map<String, Object>> parseToolCalls(String llmResponse) {
        List<Map<String, Object>> toolCalls = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(llmResponse);
            if (root.has("toolCalls") && root.get("toolCalls").isArray()) {
                ArrayNode calls = (ArrayNode) root.get("toolCalls");
                for (JsonNode call : calls) {
                    Map<String, Object> toolCall = new HashMap<>();
                    toolCall.put("name", call.get("name").asText());
                    if (call.has("arguments")) {
                        Map<String, Object> args = new HashMap<>();
                        call.get("arguments").fields().forEachRemaining(
                                entry -> args.put(entry.getKey(), entry.getValue().asText())
                        );
                        toolCall.put("arguments", args);
                    }
                    toolCalls.add(toolCall);
                }
            }
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse tool calls from LLM response: {}", e.getMessage());
        }
        return toolCalls;
    }

    public String extractResponseText(String llmResponse) {
        try {
            JsonNode root = objectMapper.readTree(llmResponse);
            if (root.has("response")) {
                return root.get("response").asText();
            }
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse response from LLM, using raw response");
        }
        return llmResponse;
    }
}
