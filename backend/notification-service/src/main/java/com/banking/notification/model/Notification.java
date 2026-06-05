package com.banking.notification.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notif_user", columnList = "userId"),
    @Index(name = "idx_notif_read", columnList = "read"),
    @Index(name = "idx_notif_created", columnList = "createdAt")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, length = 50)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(length = 100)
    private String referenceId;

    @Column(length = 30)
    private String referenceType;

    @Column(nullable = false)
    private boolean read;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime readAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum NotificationType {
        TRANSACTION_COMPLETED,
        TRANSACTION_FAILED,
        TRANSACTION_REVERSED,
        ACCOUNT_CREATED,
        ACCOUNT_STATUS_CHANGE,
        DEPOSIT_RECEIVED,
        PAYMENT_DUE,
        SECURITY_ALERT,
        SYSTEM_UPDATE
    }
}
