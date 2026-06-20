package com.banking.agent.repository;

import com.banking.agent.model.AgentConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AgentConversationRepository extends JpaRepository<AgentConversation, UUID> {

    List<AgentConversation> findByUserIdAndStatusOrderByLastActivityAtDesc(UUID userId, AgentConversation.ConversationStatus status);

    List<AgentConversation> findByUserIdOrderByLastActivityAtDesc(UUID userId);

    List<AgentConversation> findByUserId(UUID userId);

    Optional<AgentConversation> findBySessionId(String sessionId);

    List<AgentConversation> findByUserIdAndAgentType(UUID userId, AgentConversation.AgentType agentType);
}
