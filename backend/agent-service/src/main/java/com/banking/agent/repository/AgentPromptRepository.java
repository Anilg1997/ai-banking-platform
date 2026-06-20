package com.banking.agent.repository;

import com.banking.agent.model.AgentPrompt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AgentPromptRepository extends JpaRepository<AgentPrompt, UUID> {

    Optional<AgentPrompt> findByNameAndIsActiveTrue(String name);
}
