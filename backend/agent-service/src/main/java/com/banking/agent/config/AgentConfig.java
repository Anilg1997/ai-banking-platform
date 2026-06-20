package com.banking.agent.config;

import dev.langchain4j.model.ollama.OllamaChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentConfig {

    @Bean
    public OllamaChatLanguageModel ollamaChatLanguageModel(
            @Value("${ollama.base-url}") String baseUrl,
            @Value("${ollama.chat-model}") String modelName,
            @Value("${ollama.temperature:0.7}") double temperature) {
        return OllamaChatLanguageModel.builder()
                .baseUrl(baseUrl)
                .modelName(modelName)
                .temperature(temperature)
                .build();
    }

    @Bean
    public OllamaEmbeddingModel ollamaEmbeddingModel(
            @Value("${ollama.base-url}") String baseUrl,
            @Value("${ollama.embed-model}") String embedModel) {
        return OllamaEmbeddingModel.builder()
                .baseUrl(baseUrl)
                .modelName(embedModel)
                .build();
    }
}
