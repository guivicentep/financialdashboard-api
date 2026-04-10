package com.gvp.financialdashboard.controller;

import com.gvp.financialdashboard.domain.dto.CreateTransactionRequest;
import com.gvp.financialdashboard.domain.dto.TransactionFilterRequest;
import com.gvp.financialdashboard.domain.dto.TransactionResponse;
import com.gvp.financialdashboard.domain.dto.UpdateTransactionRequest;
import com.gvp.financialdashboard.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionResponse> create(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody CreateTransactionRequest request
    ) {
        var response = TransactionResponse.from(transactionService.create(request, userId));
        return ResponseEntity.created(URI.create("/transactions/" + response.id())).body(response);
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> list(
            @RequestHeader("X-User-Id") UUID userId,
            @ModelAttribute TransactionFilterRequest filter
    ) {
        var response = transactionService.findAll(filter, userId)
                .stream()
                .map(TransactionResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> update(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTransactionRequest request
    ) {
        return ResponseEntity.ok(
                TransactionResponse.from(transactionService.update(id, request, userId))
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID id
    ) {
        transactionService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }
}