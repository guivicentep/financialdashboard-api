package com.gvp.financialdashboard.domain.dto;

import com.gvp.financialdashboard.domain.enums.RecurrenceType;
import com.gvp.financialdashboard.domain.enums.TransactionType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateRecurringTransactionRequest(
        @NotNull(message = "O tipo é obrigatório")
        TransactionType type,

        @NotNull(message = "A categoria é obrigatória")
        UUID categoryId,

        @NotNull(message = "O valor é obrigatório")
        @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero")
        @DecimalMax(value = "99000.00", message = "O valor máximo permitido é R$ 99.000,00")
        BigDecimal amount,

        @Size(max = 100, message = "A descrição deve ter no máximo 100 caracteres")
        String description,

        @NotNull(message = "O tipo de recorrência é obrigatório")
        RecurrenceType recurrenceType,

        @NotNull(message = "O dia de recorrência é obrigatório")
        @Min(value = 1, message = "O dia deve ser entre 1 e 31")
        @Max(value = 31, message = "O dia deve ser entre 1 e 31")
        Short recurrenceDay,

        @NotNull(message = "A data de início é obrigatória")
        LocalDate startDate,

        @NotNull(message = "O número de ocorrências é obrigatório")
        @Min(value = 1, message = "O número de ocorrências deve ser pelo menos 1")
        @Max(value = 360, message = "O número de ocorrências não pode exceder 360")
        Short occurrences
) {
}
