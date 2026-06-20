package com.banking.card.controller;

import com.banking.card.dto.*;
import com.banking.card.security.CardPrincipal;
import com.banking.card.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/cards")
@RequiredArgsConstructor
public class CardAdminController {

    private final CardService cardService;

    @GetMapping
    public ResponseEntity<List<CardResponse>> listAllCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        List<CardResponse> cards = cardService.getAllCards(page, size, status);
        return ResponseEntity.ok(cards);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CardResponse> getCardDetails(@PathVariable String id) {
        CardResponse response = cardService.getCardById(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<CardResponse> updateCardStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        CardResponse response = cardService.updateCardStatus(id, body.get("status"));
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/limit")
    public ResponseEntity<CardResponse> updateCreditLimit(
            @PathVariable String id,
            @RequestBody Map<String, BigDecimal> body) {
        CardResponse response = cardService.updateCreditLimit(id, body.get("creditLimit"));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/applications")
    public ResponseEntity<List<CardApplicationResponse>> listApplications(
            @RequestParam(required = false) String status) {
        List<CardApplicationResponse> apps = cardService.getAllApplications(status);
        return ResponseEntity.ok(apps);
    }

    @GetMapping("/applications/{id}")
    public ResponseEntity<CardApplicationResponse> getApplicationDetails(@PathVariable String id) {
        CardApplicationResponse response = cardService.getApplicationById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/applications/{id}/approve")
    public ResponseEntity<CardResponse> approveApplication(
            @PathVariable String id,
            @AuthenticationPrincipal CardPrincipal principal) {
        CardResponse response = cardService.approveApplication(id, principal.getUsername());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/applications/{id}/reject")
    public ResponseEntity<CardApplicationResponse> rejectApplication(
            @PathVariable String id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal CardPrincipal principal) {
        CardApplicationResponse response = cardService.rejectApplication(
                id, principal.getUsername(), body.getOrDefault("reason", "Declined"));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCardStats() {
        Map<String, Object> stats = cardService.getCardStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/transactions/recent")
    public ResponseEntity<List<CardTransactionResponse>> getRecentTransactions(
            @RequestParam(defaultValue = "20") int limit) {
        List<CardTransactionResponse> txns = cardService.getRecentTransactions(limit);
        return ResponseEntity.ok(txns);
    }
}
