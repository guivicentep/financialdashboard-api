package com.gvp.financialdashboard.domain.dto;

import com.gvp.financialdashboard.domain.enums.TransactionType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;

@DisplayName("CreateTransactionRequest - Validações")
public class CreateTransactionRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    private Set<ConstraintViolation<CreateTransactionRequest>> validate(CreateTransactionRequest request) {
        return validator.validate(request);
    }

    private String messagesOf(Set<ConstraintViolation<CreateTransactionRequest>> violations) {
        return violations.stream()
                .map(ConstraintViolation::getMessage)
                .reduce("", (a, b) -> a + b);
    }

    // CT-003 - Rejeitar amount = 0
    @Test
    @DisplayName("CT-003: deve rejeitar amount igual a zero")
    void shouldRejectAmountZero() {
        var request = new CreateTransactionRequest(
                TransactionType.INCOME,
                UUID.randomUUID(),
                BigDecimal.ZERO,
                LocalDate.now(),
                null
        );

        var violations = validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(messagesOf(violations)).contains("O valor deve ser maior que zero");
    }

    // CT-004 - Rejeitar amount negativo
    @Test
    @DisplayName("CT-004: deve rejeitar amount negativo")
    void shouldRejectNegativeAmount() {
        var request = new CreateTransactionRequest(
                TransactionType.EXPENSE,
                UUID.randomUUID(),
                new BigDecimal("-100.00"),
                LocalDate.now(),
                null
        );

        var violations = validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(messagesOf(violations)).contains("O valor deve ser maior que zero");
    }

    // CT-005 - Rejeitar amount acima do limite
    @Test
    @DisplayName("CT-005: deve rejeitar amount acima de 99000")
    void shouldRejectAmountAboveLimit() {
        var request = new CreateTransactionRequest(
                TransactionType.INCOME,
                UUID.randomUUID(),
                new BigDecimal("99001.00"),
                LocalDate.now(),
                null
        );

        var violations = validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(messagesOf(violations)).contains("O valor máximo permitido é R$ 99.000,00");
    }

    // CT-006 - Aceitar amount no limite máximo
    @Test
    @DisplayName("CT-006: deve aceitar amount igual a 99000")
    void shouldAcceptAmountAtLimit() {
        var request = new CreateTransactionRequest(
                TransactionType.INCOME,
                UUID.randomUUID(),
                new BigDecimal("99000.00"),
                LocalDate.now(),
                null
        );

        var violations = validate(request);

        assertThat(violations).isEmpty();
    }

    // CT-007 - Rejeitar data futura
    @Test
    @DisplayName("CT-008: deve rejeitar data futura")
    void shouldRejectFutureDate() {
        var request = new CreateTransactionRequest(
                TransactionType.INCOME,
                UUID.randomUUID(),
                new BigDecimal("100.00"),
                LocalDate.now().plusDays(1),
                null
        );

        var violations = validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(messagesOf(violations)).contains("A data não pode ser futura");
    }

    // CT-009 — Aceitar sem descrição
    @Test
    @DisplayName("CT-009: deve aceitar transação sem descrição")
    void shouldAcceptNullDescription() {
        var request = new CreateTransactionRequest(
                TransactionType.INCOME,
                UUID.randomUUID(),
                new BigDecimal("100.00"),
                LocalDate.now(),
                null
        );

        var violations = validate(request);

        assertThat(violations).isEmpty();
    }

    // CT-010 — Rejeitar descrição acima de 100 caracteres
    @Test
    @DisplayName("CT-010: deve rejeitar descrição com 101 caracteres")
    void shouldRejectDescriptionOver100Chars() {
        var request = new CreateTransactionRequest(
                TransactionType.INCOME,
                UUID.randomUUID(),
                new BigDecimal("100.00"),
                LocalDate.now(),
                "a".repeat(101)
        );

        var violations = validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(messagesOf(violations)).contains("A descrição deve ter no máximo 100 caracteres");
    }

    // CT-011 — Aceitar descrição com exatamente 100 caracteres
    @Test
    @DisplayName("CT-011: deve aceitar descrição com exatamente 100 caracteres")
    void shouldAcceptDescriptionWith100Chars() {
        var request = new CreateTransactionRequest(
                TransactionType.INCOME,
                UUID.randomUUID(),
                new BigDecimal("100.00"),
                LocalDate.now(),
                "a".repeat(100)
        );

        var violations = validate(request);

        assertThat(violations).isEmpty();
    }

    // CT-012 — Rejeitar sem category_id
    @Test
    @DisplayName("CT-012: deve rejeitar transação sem categoryId")
    void shouldRejectNullCategoryId() {
        var request = new CreateTransactionRequest(
                TransactionType.INCOME,
                null,
                new BigDecimal("100.00"),
                LocalDate.now(),
                null
        );

        var violations = validate(request);

        assertThat(violations).isNotEmpty();
        assertThat(messagesOf(violations)).contains("A categoria é obrigatória");
    }
}

