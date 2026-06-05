package com.banking.notification.service;

import com.banking.notification.dto.NotificationResponse;
import com.banking.notification.model.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Slf4j
public class SseNotificationService {

    private final Map<String, List<SseEmitter>> userEmitters = new ConcurrentHashMap<>();

    public SseEmitter createEmitter(String userId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        userEmitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError(e -> removeEmitter(userId, emitter));

        try {
            emitter.send(SseEmitter.event()
                    .name("connected")
                    .data(Map.of("userId", userId, "status", "connected")));
        } catch (IOException e) {
            removeEmitter(userId, emitter);
        }

        log.info("SSE connected for user: {}. Total connections: {}", userId, userEmitters.size());
        return emitter;
    }

    public void sendNotification(String userId, Notification notification) {
        List<SseEmitter> emitters = userEmitters.get(userId);
        if (emitters == null || emitters.isEmpty()) return;

        NotificationResponse response = NotificationResponse.fromNotification(notification);
        List<SseEmitter> deadEmitters = new CopyOnWriteArrayList<>();

        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(response));
            } catch (IOException e) {
                deadEmitters.add(emitter);
            }
        });

        emitters.removeAll(deadEmitters);
    }

    public void broadcastToAll(Object data) {
        userEmitters.forEach((userId, emitters) -> {
            List<SseEmitter> dead = new CopyOnWriteArrayList<>();
            emitters.forEach(emitter -> {
                try {
                    emitter.send(SseEmitter.event().name("broadcast").data(data));
                } catch (IOException e) {
                    dead.add(emitter);
                }
            });
            emitters.removeAll(dead);
        });
    }

    private void removeEmitter(String userId, SseEmitter emitter) {
        List<SseEmitter> emitters = userEmitters.get(userId);
        if (emitters != null) {
            emitters.remove(emitter);
            if (emitters.isEmpty()) {
                userEmitters.remove(userId);
            }
        }
    }
}
