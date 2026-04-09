package com.gvp.financialdashboard.domain.dto;

import com.gvp.financialdashboard.domain.enums.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCategoryRequest(
        @NotBlank(message = "name é obrigatório")
        String name,

        @NotNull(message = "type é obrigatório")
        TransactionType type
) {
}
