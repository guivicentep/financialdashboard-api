package com.gvp.financialdashboard.domain.dto;

import com.gvp.financialdashboard.domain.entity.Transaction;
import com.gvp.financialdashboard.domain.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        UUID categoryId,
        String categoryName,
        TransactionType type,
        BigDecimal amount,
        String description,
        LocalDate date
) {
    public static TransactionResponse from(Transaction t) {
        return new TransactionResponse(
                t.getId(),
                t.getCategory().getId(),
                t.getCategory().getName(),
                t.getType(),
                t.getAmount(),
                t.getDescription(),
                t.getDate()
        );
    }
}
