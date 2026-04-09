package com.gvp.financialdashboard.domain.dto;

import com.gvp.financialdashboard.domain.entity.Category;
import com.gvp.financialdashboard.domain.enums.TransactionType;

import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name,
        TransactionType type
) {
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getType());
    }
}
