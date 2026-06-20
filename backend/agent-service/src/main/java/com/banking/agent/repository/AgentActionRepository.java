package com.banking.agent.repository;

import com.banking.agent.model.AgentAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AgentActionRepository extends JpaRepository<AgentAction, UUID> {

    List<AgentAction> findByConversationIdOrderByCreatedAtDesc(UUID conversationId);

    List<AgentAction> findByUserIdAndCreatedAtBetween(UUID userId, LocalDateTime start, LocalDateTime end);

    List<AgentAction> findByStatus(AgentAction.ActionStatus status);
}
