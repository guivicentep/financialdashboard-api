package com.gvp.financialdashboard.domain.dto;

import com.gvp.financialdashboard.domain.enums.AmountOperator;
import com.gvp.financialdashboard.domain.enums.TransactionType;

import java.math.BigDecimal;
import java.util.UUID;

public record TransactionFilterRequest(
        Integer month,
        Integer year,
        TransactionType type,
        UUID categoryId,
        AmountOperator amountOperator,
        BigDecimal amount,
        BigDecimal amountMin,
        BigDecimal amountMax,
        String orderBy,
        String direction
) {
}
