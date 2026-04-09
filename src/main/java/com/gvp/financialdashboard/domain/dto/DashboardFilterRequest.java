package com.gvp.financialdashboard.domain.dto;

import com.gvp.financialdashboard.domain.enums.TransactionType;

import java.util.UUID;

public record DashboardFilterRequest(
        Integer month,
        Integer year,
        TransactionType type,
        UUID categoryId
) {
}
