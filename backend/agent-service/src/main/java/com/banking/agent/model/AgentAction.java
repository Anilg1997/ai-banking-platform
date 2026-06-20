package com.banking.agent.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "agent_actions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentAction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID conversationId;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 100)
    private String actionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ActionStatus status;

    @Column(columnDefinition = "TEXT")
    private String input;

    @Column(columnDefinition = "TEXT")
    private String output;

    @Column(length = 255)
    private String toolName;

    private Long duration;

    @Column(columnDefinition = "TEXT")
    private String error;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime completedAt;

    public enum ActionStatus {
        PENDING,
        EXECUTING,
        COMPLETED,
        FAILED
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = ActionStatus.PENDING;
        }
    }
}
