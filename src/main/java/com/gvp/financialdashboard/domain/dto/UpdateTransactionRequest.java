package com.gvp.financialdashboard.domain.dto;

import com.gvp.financialdashboard.domain.enums.TransactionType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record UpdateTransactionRequest(
        @NotNull(message = "O tipo é obrigatório")
        TransactionType type,

        @NotNull(message = "A categoria é obrigatória")
        UUID cateogryId,

        @NotNull(message = "O valor é obrigatório")
        @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero")
        @DecimalMax(value = "99000.00", message = "O valor máximo permitido é R$ 99.000,00")
        BigDecimal amount,

        @NotNull(message = "A data é obrigatória")
        @PastOrPresent(message = "A data não pode ser futura")
        LocalDate date,

        @Size(max = 100, message = "A descrição deve ter no máximo 100 caracteres")
        String description
) {
}
