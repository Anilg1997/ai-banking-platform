package com.banking.transaction.controller;

import com.banking.transaction.service.SseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/transactions/stream")
@RequiredArgsConstructor
public class TransactionSSEController {

    private final SseService sseService;

    @GetMapping(value = "/{userId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamTransactions(@PathVariable String userId) {
        return sseService.createEmitter(userId);
    }
}
