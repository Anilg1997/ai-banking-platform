package com.banking.notification.controller;

import com.banking.notification.service.SseNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/notifications/stream")
@RequiredArgsConstructor
public class NotificationSSEController {

    private final SseNotificationService sseService;

    @GetMapping(value = "/{userId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamNotifications(@PathVariable String userId) {
        return sseService.createEmitter(userId);
    }
}
