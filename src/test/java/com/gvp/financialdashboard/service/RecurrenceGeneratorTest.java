package com.gvp.financialdashboard.service;

import com.gvp.financialdashboard.domain.entity.Category;
import com.gvp.financialdashboard.domain.entity.RecurringTransaction;
import com.gvp.financialdashboard.domain.entity.User;
import com.gvp.financialdashboard.domain.enums.RecurrenceType;
import com.gvp.financialdashboard.domain.enums.TransactionType;
import com.gvp.financialdashboard.service.impl.RecurrenceGeneratorImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RecurrenceGenerator — Geração de instâncias")
class RecurrenceGeneratorTest {

    private RecurrenceGeneratorImpl generator;

    private User user;
    private Category category;

    @BeforeEach
    void setUp() {
        generator = new RecurrenceGeneratorImpl();

        user = User.builder()
                .id(UUID.randomUUID())
                .clerkId("clerk_test")
                .build();

        category = Category.builder()
                .id(UUID.randomUUID())
                .name("Salário")
                .type(TransactionType.INCOME)
                .build();
    }

    private RecurringTransaction buildTemplate(
            RecurrenceType recurrenceType,
            short recurrenceDay,
            LocalDate startDate,
            short occurrences
    ) {
        return RecurringTransaction.builder()
                .id(UUID.randomUUID())
                .user(user)
                .category(category)
                .type(TransactionType.INCOME)
                .amount(new BigDecimal("1000.00"))
                .description("descrição")
                .recurrenceType(recurrenceType)
                .recurrenceDay(recurrenceDay)
                .startDate(startDate)
                .occurrences(occurrences)
                .build();
    }

    // CT-049 — MONTHLY 3x gera 3 instâncias nas datas corretas
    @Test
    @DisplayName("CT-049: MONTHLY 3x deve gerar 3 instâncias nas datas corretas")
    void shouldGenerateMonthlyInstances() {
        var template = buildTemplate(
                RecurrenceType.MONTHLY, (short) 10,
                LocalDate.of(2026, 4, 10), (short) 3
        );

        var result = generator.generate(template);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getDate()).isEqualTo(LocalDate.of(2026, 4, 10));
        assertThat(result.get(1).getDate()).isEqualTo(LocalDate.of(2026, 5, 10));
        assertThat(result.get(2).getDate()).isEqualTo(LocalDate.of(2026, 6, 10));
    }

    // CT-050 — MONTHLY respeita o dia de recorrência
    @Test
    @DisplayName("CT-050: MONTHLY deve respeitar o dia de recorrência configurado")
    void shouldRespectRecurrenceDay() {
        var template = buildTemplate(
                RecurrenceType.MONTHLY, (short) 15,
                LocalDate.of(2026, 1, 15), (short) 4
        );

        var result = generator.generate(template);

        assertThat(result).hasSize(4);
        assertThat(result).allMatch(t -> t.getDate().getDayOfMonth() == 15);
    }

    // CT-051 — MONTHLY em mês curto usa último dia disponível
    @Test
    @DisplayName("CT-051: MONTHLY em mês curto deve usar o último dia disponível")
    void shouldHandleShortMonths() {
        var template = buildTemplate(
                RecurrenceType.MONTHLY, (short) 31,
                LocalDate.of(2026, 1, 31), (short) 3
        );

        var result = generator.generate(template);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getDate()).isEqualTo(LocalDate.of(2026, 1, 31));
        assertThat(result.get(1).getDate()).isEqualTo(LocalDate.of(2026, 2, 28));
        assertThat(result.get(2).getDate()).isEqualTo(LocalDate.of(2026, 3, 31));
    }

    // CT-052 — DAILY 7x gera 7 instâncias em dias consecutivos
    @Test
    @DisplayName("CT-052: DAILY 7x deve gerar 7 instâncias em dias consecutivos")
    void shouldGenerateDailyInstances() {
        var template = buildTemplate(
                RecurrenceType.DAILY, (short) 1,
                LocalDate.of(2026, 4, 1), (short) 7
        );

        var result = generator.generate(template);

        assertThat(result).hasSize(7);
        for (int i = 0; i < 7; i++) {
            assertThat(result.get(i).getDate())
                    .isEqualTo(LocalDate.of(2026, 4, 1).plusDays(i));
        }
    }

    // CT-053 — WEEKLY 4x gera 4 instâncias em semanas consecutivas
    @Test
    @DisplayName("CT-053: WEEKLY 4x deve gerar 4 instâncias em semanas consecutivas")
    void shouldGenerateWeeklyInstances() {
        var template = buildTemplate(
                RecurrenceType.WEEKLY, (short) 1,
                LocalDate.of(2026, 4, 6), (short) 4
        );

        var result = generator.generate(template);

        assertThat(result).hasSize(4);
        assertThat(result.get(0).getDate()).isEqualTo(LocalDate.of(2026, 4, 6));
        assertThat(result.get(1).getDate()).isEqualTo(LocalDate.of(2026, 4, 13));
        assertThat(result.get(2).getDate()).isEqualTo(LocalDate.of(2026, 4, 20));
        assertThat(result.get(3).getDate()).isEqualTo(LocalDate.of(2026, 4, 27));
    }

    // CT-054 — YEARLY 3x gera 3 instâncias em anos consecutivos
    @Test
    @DisplayName("CT-054: YEARLY 3x deve gerar 3 instâncias em anos consecutivos")
    void shouldGenerateYearlyInstances() {
        var template = buildTemplate(
                RecurrenceType.YEARLY, (short) 1,
                LocalDate.of(2026, 4, 8), (short) 3
        );

        var result = generator.generate(template);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getDate()).isEqualTo(LocalDate.of(2026, 4, 8));
        assertThat(result.get(1).getDate()).isEqualTo(LocalDate.of(2027, 4, 8));
        assertThat(result.get(2).getDate()).isEqualTo(LocalDate.of(2028, 4, 8));
    }

    // CT-055 — Instâncias têm vínculo correto com o template
    @Test
    @DisplayName("CT-055: instâncias geradas devem ter vínculo com o template")
    void shouldLinkInstancesToTemplate() {
        var template = buildTemplate(
                RecurrenceType.MONTHLY, (short) 5,
                LocalDate.of(2026, 4, 5), (short) 3
        );

        var result = generator.generate(template);

        assertThat(result).allMatch(t -> t.getRecurringTransaction() == template);
    }

    // CT-056 — Instâncias herdam amount, type e category do template
    @Test
    @DisplayName("CT-056: instâncias geradas devem herdar amount, type e category do template")
    void shouldInheritFieldsFromTemplate() {
        var template = buildTemplate(
                RecurrenceType.MONTHLY, (short) 5,
                LocalDate.of(2026, 4, 5), (short) 3
        );

        var result = generator.generate(template);

        assertThat(result).allMatch(t ->
                t.getAmount().compareTo(new BigDecimal("1000.00")) == 0 &&
                        t.getType() == TransactionType.INCOME &&
                        t.getCategory() == category &&
                        t.getUser() == user
        );
    }
}