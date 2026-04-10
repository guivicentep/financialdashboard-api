package com.gvp.financialdashboard.domain.dto;

import com.gvp.financialdashboard.domain.entity.RecurringTransaction;
import com.gvp.financialdashboard.domain.enums.RecurrenceType;
import com.gvp.financialdashboard.domain.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record RecurringTransactionResponse(
        UUID id,
        UUID categoryId,
        String categoryName,
        TransactionType type,
        BigDecimal amount,
        String description,
        RecurrenceType recurrenceType,
        Short recurrenceDay,
        LocalDate startDate,
        Short occurrences
) {
    public static RecurringTransactionResponse from(RecurringTransaction rt) {
        return new RecurringTransactionResponse(
                rt.getId(),
                rt.getCategory().getId(),
                rt.getCategory().getName(),
                rt.getType(),
                rt.getAmount(),
                rt.getDescription(),
                rt.getRecurrenceType(),
                rt.getRecurrenceDay(),
                rt.getStartDate(),
                rt.getOccurrences()
        );
    }
}