package com.banking.notification.service;

import com.banking.notification.dto.NotificationResponse;
import com.banking.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public List<NotificationResponse> getUserNotifications(String userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(NotificationResponse::fromNotification)
                .collect(Collectors.toList());
    }

    public List<NotificationResponse> getUnreadNotifications(String userId) {
        return notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId).stream()
                .map(NotificationResponse::fromNotification)
                .collect(Collectors.toList());
    }

    public List<NotificationResponse> getRecentNotifications(String userId, int limit) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, limit))
                .stream()
                .map(NotificationResponse::fromNotification)
                .collect(Collectors.toList());
    }

    public Map<String, Object> getUnreadCount(String userId) {
        long count = notificationRepository.countByUserIdAndReadFalse(userId);
        return Map.of("count", count, "userId", userId);
    }

    @Transactional
    public void markAsRead(String notificationId) {
        notificationRepository.markAsRead(notificationId);
        log.debug("Marked notification as read: {}", notificationId);
    }

    @Transactional
    public void markAllAsRead(String userId) {
        int updated = notificationRepository.markAllAsRead(userId);
        log.info("Marked {} notifications as read for user: {}", updated, userId);
    }

    @Transactional
    public void deleteNotification(String id) {
        notificationRepository.deleteById(id);
    }
}
