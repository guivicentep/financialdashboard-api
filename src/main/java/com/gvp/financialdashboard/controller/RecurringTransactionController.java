package com.gvp.financialdashboard.controller;

import com.gvp.financialdashboard.domain.dto.CreateRecurringTransactionRequest;
import com.gvp.financialdashboard.domain.dto.RecurringTransactionResponse;
import com.gvp.financialdashboard.domain.dto.UpdateRecurringTransactionRequest;
import com.gvp.financialdashboard.service.RecurringTransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/recurring-transactions")
@RequiredArgsConstructor
public class RecurringTransactionController {

    private final RecurringTransactionService recurringTransactionService;

    @PostMapping
    public ResponseEntity<RecurringTransactionResponse> create(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody CreateRecurringTransactionRequest request
    ) {
        var response = RecurringTransactionResponse.from(recurringTransactionService.create(request, userId));
        return ResponseEntity.created(URI.create("/recurring-transactions/" + response.id())).body(response);
    }

    @GetMapping
    public ResponseEntity<List<RecurringTransactionResponse>> list(
            @RequestHeader("X-User-Id") UUID userId
    ) {
        var response = recurringTransactionService.findAll(userId)
                .stream()
                .map(RecurringTransactionResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecurringTransactionResponse> update(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRecurringTransactionRequest request
    ) {
        return ResponseEntity.ok(
                RecurringTransactionResponse.from(recurringTransactionService.update(id, request, userId))
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID id
    ) {
        recurringTransactionService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }
}