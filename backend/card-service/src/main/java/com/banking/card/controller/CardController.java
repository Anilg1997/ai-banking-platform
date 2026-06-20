package com.banking.card.controller;

import com.banking.card.dto.*;
import com.banking.card.security.CardPrincipal;
import com.banking.card.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @PostMapping("/apply")
    public ResponseEntity<CardApplicationResponse> applyForCard(
            @Valid @RequestBody CardApplicationRequest request,
            @AuthenticationPrincipal CardPrincipal principal) {
        CardApplicationResponse response = cardService.applyForCard(request, principal.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my-cards")
    public ResponseEntity<List<CardSummary>> getMyCards(@AuthenticationPrincipal CardPrincipal principal) {
        List<CardSummary> cards = cardService.getUserCards(principal.getUserId());
        return ResponseEntity.ok(cards);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CardResponse> getCard(
            @PathVariable String id,
            @AuthenticationPrincipal CardPrincipal principal) {
        CardResponse response = cardService.getCard(id, principal.getUserId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<List<CardTransactionResponse>> getCardTransactions(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @AuthenticationPrincipal CardPrincipal principal) {
        List<CardTransactionResponse> txns = cardService.getCardTransactions(id, principal.getUserId(), page, size);
        return ResponseEntity.ok(txns);
    }

    @GetMapping("/{id}/statement")
    public ResponseEntity<CardStatementResponse> getLatestStatement(
            @PathVariable String id,
            @AuthenticationPrincipal CardPrincipal principal) {
        CardStatementResponse response = cardService.getLatestStatement(id, principal.getUserId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/statements")
    public ResponseEntity<List<CardStatementResponse>> getAllStatements(
            @PathVariable String id,
            @AuthenticationPrincipal CardPrincipal principal) {
        List<CardStatementResponse> statements = cardService.getAllStatements(id, principal.getUserId());
        return ResponseEntity.ok(statements);
    }

    @GetMapping("/{id}/available-credit")
    public ResponseEntity<Map<String, Object>> getAvailableCredit(
            @PathVariable String id,
            @AuthenticationPrincipal CardPrincipal principal) {
        Map<String, Object> credit = cardService.getAvailableCredit(id, principal.getUserId());
        return ResponseEntity.ok(credit);
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<CardResponse> activateCard(
            @PathVariable String id,
            @Valid @RequestBody ActivateCardRequest request,
            @AuthenticationPrincipal CardPrincipal principal) {
        CardResponse response = cardService.activateCard(id, principal.getUserId(), request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/freeze")
    public ResponseEntity<CardResponse> freezeCard(
            @PathVariable String id,
            @AuthenticationPrincipal CardPrincipal principal) {
        CardResponse response = cardService.freezeCard(id, principal.getUserId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/unfreeze")
    public ResponseEntity<CardResponse> unfreezeCard(
            @PathVariable String id,
            @AuthenticationPrincipal CardPrincipal principal) {
        CardResponse response = cardService.unfreezeCard(id, principal.getUserId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/report-lost")
    public ResponseEntity<CardResponse> reportLost(
            @PathVariable String id,
            @AuthenticationPrincipal CardPrincipal principal) {
        CardResponse response = cardService.reportLostCard(id, principal.getUserId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/make-payment")
    public ResponseEntity<CardTransactionResponse> makePayment(
            @PathVariable String id,
            @Valid @RequestBody CardPaymentRequest request,
            @AuthenticationPrincipal CardPrincipal principal) {
        CardTransactionResponse response = cardService.makePayment(id, principal.getUserId(), request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/transactions/category/{category}")
    public ResponseEntity<List<CardTransactionResponse>> getTransactionsByCategory(
            @PathVariable String id,
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @AuthenticationPrincipal CardPrincipal principal) {
        List<CardTransactionResponse> txns = cardService.getTransactionsByCategory(id, principal.getUserId(), category, page, size);
        return ResponseEntity.ok(txns);
    }

    @GetMapping("/transactions/{ref}")
    public ResponseEntity<CardTransactionResponse> getTransactionByRef(@PathVariable String ref) {
        CardTransactionResponse response = cardService.getTransactionByRef(ref);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/transactions/{id}/dispute")
    public ResponseEntity<CardTransactionResponse> disputeTransaction(
            @PathVariable String id,
            @Valid @RequestBody DisputeRequest request,
            @AuthenticationPrincipal CardPrincipal principal) {
        CardTransactionResponse response = cardService.disputeTransaction(id, principal.getUserId(), request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/rewards/summary")
    public ResponseEntity<Map<String, Object>> getRewardsSummary(@AuthenticationPrincipal CardPrincipal principal) {
        Map<String, Object> summary = cardService.getRewardsSummary(principal.getUserId());
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/application/status")
    public ResponseEntity<CardApplicationResponse> getApplicationStatus(
            @RequestParam String applicationId,
            @AuthenticationPrincipal CardPrincipal principal) {
        CardApplicationResponse response = cardService.getApplicationStatus(applicationId, principal.getUserId());
        return ResponseEntity.ok(response);
    }
}
