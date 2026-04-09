package com.gvp.financialdashboard.domain.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
        @NotBlank(message = "clerkId é obrigatório")
        String clerkId
) {
}
