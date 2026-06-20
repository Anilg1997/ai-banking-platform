package com.banking.agent.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "agent_conversations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentConversation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    @JdbcTypeCode(SqlTypes.JSON)
    private String messages;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AgentType agentType;

    @Column(length = 255)
    private String sessionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConversationStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private LocalDateTime lastActivityAt;

    public enum AgentType {
        GENERAL,
        FINANCIAL_ADVISOR,
        FRAUD_ANALYST,
        LOAN_OFFICER,
        CARD_SPECIALIST
    }

    public enum ConversationStatus {
        ACTIVE,
        CLOSED,
        EXPIRED
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        lastActivityAt = LocalDateTime.now();
        if (status == null) {
            status = ConversationStatus.ACTIVE;
        }
        if (agentType == null) {
            agentType = AgentType.GENERAL;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
