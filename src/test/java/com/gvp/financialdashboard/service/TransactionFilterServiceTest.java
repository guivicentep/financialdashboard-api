package com.gvp.financialdashboard.service;

import com.gvp.financialdashboard.domain.dto.TransactionFilterRequest;
import com.gvp.financialdashboard.domain.entity.Category;
import com.gvp.financialdashboard.domain.entity.Transaction;
import com.gvp.financialdashboard.domain.entity.User;
import com.gvp.financialdashboard.domain.enums.AmountOperator;
import com.gvp.financialdashboard.domain.enums.TransactionType;
import com.gvp.financialdashboard.repository.TransactionRepository;
import com.gvp.financialdashboard.service.impl.TransactionServiceImpl;
import com.gvp.financialdashboard.repository.CategoryRepository;
import com.gvp.financialdashboard.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionService — Filtros e Ordenação")
class TransactionFilterServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private UUID userId;
    private User user;
    private Category incomeCategory;
    private Category expenseCategory;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = User.builder().id(userId).clerkId("clerk_test").build();

        incomeCategory = Category.builder()
                .id(UUID.randomUUID()).name("Salário").type(TransactionType.INCOME).build();

        expenseCategory = Category.builder()
                .id(UUID.randomUUID()).name("Alimentação").type(TransactionType.EXPENSE).build();
    }

    private Transaction income(double amount) {
        return Transaction.builder().id(UUID.randomUUID()).user(user)
                .category(incomeCategory).type(TransactionType.INCOME)
                .amount(BigDecimal.valueOf(amount)).date(LocalDate.now()).build();
    }

    private Transaction expense(double amount) {
        return Transaction.builder().id(UUID.randomUUID()).user(user)
                .category(expenseCategory).type(TransactionType.EXPENSE)
                .amount(BigDecimal.valueOf(amount)).date(LocalDate.now()).build();
    }

    // CT-028 — Filtrar sem parâmetros usa mês corrente
    @Test
    @DisplayName("CT-028: deve usar mês corrente quando não informado")
    void shouldUseCurrentMonthWhenNotProvided() {
        var now = LocalDate.now();
        var filter = new TransactionFilterRequest(
                null, null, null, null, null, null, null, null, null, null
        );

        when(transactionRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(List.of(income(100)));

        var result = transactionService.findAll(filter, userId);

        assertThat(result).hasSize(1);
        verify(transactionRepository).findAll(any(Specification.class), any(Sort.class));
    }

    // CT-029 — Filtrar por tipo INCOME
    @Test
    @DisplayName("CT-029: deve filtrar apenas transações INCOME")
    void shouldFilterByIncomeType() {
        var filter = new TransactionFilterRequest(
                4, 2026, TransactionType.INCOME, null, null, null, null, null, null, null
        );

        when(transactionRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(List.of(income(500), income(300)));

        var result = transactionService.findAll(filter, userId);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(t -> t.getType() == TransactionType.INCOME);
    }

    // CT-030 — Filtrar por tipo EXPENSE
    @Test
    @DisplayName("CT-030: deve filtrar apenas transações EXPENSE")
    void shouldFilterByExpenseType() {
        var filter = new TransactionFilterRequest(
                4, 2026, TransactionType.EXPENSE, null, null, null, null, null, null, null
        );

        when(transactionRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(List.of(expense(150), expense(200)));

        var result = transactionService.findAll(filter, userId);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(t -> t.getType() == TransactionType.EXPENSE);
    }

    // CT-031 — Filtrar por categoria
    @Test
    @DisplayName("CT-031: deve filtrar por categoria")
    void shouldFilterByCategory() {
        var filter = new TransactionFilterRequest(
                4, 2026, null, expenseCategory.getId(), null, null, null, null, null, null
        );

        when(transactionRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(List.of(expense(100)));

        var result = transactionService.findAll(filter, userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategory()).isEqualTo(expenseCategory);
    }

    // CT-032 — Filtrar por valor igual
    @Test
    @DisplayName("CT-032: deve filtrar por valor igual")
    void shouldFilterByEqualAmount() {
        var filter = new TransactionFilterRequest(
                4, 2026, null, null,
                AmountOperator.EQUAL, BigDecimal.valueOf(100),
                null, null, null, null
        );

        when(transactionRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(List.of(income(100)));

        var result = transactionService.findAll(filter, userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAmount()).isEqualByComparingTo("100");
    }

    // CT-033 — Filtrar por valor maior que
    @Test
    @DisplayName("CT-033: deve filtrar por valor maior que")
    void shouldFilterByGreaterThanAmount() {
        var filter = new TransactionFilterRequest(
                4, 2026, null, null,
                AmountOperator.GREATER_THAN, BigDecimal.valueOf(500),
                null, null, null, null
        );

        when(transactionRepository.findAll(any(Specification.class),  any(Sort.class)))
                .thenReturn(List.of(income(600), income(800)));

        var result = transactionService.findAll(filter, userId);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(t -> t.getAmount().compareTo(BigDecimal.valueOf(500)) > 0);
    }

    // CT-034 — Filtrar por valor menor que
    @Test
    @DisplayName("CT-034: deve filtrar por valor menor que")
    void shouldFilterByLessThanAmount() {
        var filter = new TransactionFilterRequest(
                4, 2026, null, null,
                AmountOperator.LESS_THAN, BigDecimal.valueOf(200),
                null, null, null, null
        );

        when(transactionRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(List.of(expense(100), expense(150)));

        var result = transactionService.findAll(filter, userId);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(t -> t.getAmount().compareTo(BigDecimal.valueOf(200)) < 0);
    }

    // CT-035 — Filtrar por intervalo de valor
    @Test
    @DisplayName("CT-035: deve filtrar por intervalo de valor")
    void shouldFilterByBetweenAmount() {
        var filter = new TransactionFilterRequest(
                4, 2026, null, null,
                AmountOperator.BETWEEN, null,
                BigDecimal.valueOf(100), BigDecimal.valueOf(500),
                null, null
        );

        when(transactionRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(List.of(income(100), income(300), income(500)));

        var result = transactionService.findAll(filter, userId);

        assertThat(result).hasSize(3);
        assertThat(result).allMatch(t ->
                t.getAmount().compareTo(BigDecimal.valueOf(100)) >= 0 &&
                        t.getAmount().compareTo(BigDecimal.valueOf(500)) <= 0);
    }

    // CT-036 — Filtros combinados
    @Test
    @DisplayName("CT-036: deve aplicar filtros combinados")
    void shouldApplyCombinedFilters() {
        var filter = new TransactionFilterRequest(
                4, 2026, TransactionType.EXPENSE, expenseCategory.getId(),
                AmountOperator.LESS_THAN, BigDecimal.valueOf(200),
                null, null, null, null
        );

        when(transactionRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(List.of(expense(100), expense(150)));

        var result = transactionService.findAll(filter, userId);

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(t -> t.getType() == TransactionType.EXPENSE);
        assertThat(result).allMatch(t -> t.getAmount().compareTo(BigDecimal.valueOf(200)) < 0);
    }

    // CT-037 — Ordenação por data ASC
    @Test
    @DisplayName("CT-037: deve ordenar por data ascendente")
    void shouldOrderByDateAsc() {
        var filter = new TransactionFilterRequest(
                4, 2026, null, null, null, null, null, null, "date", "ASC"
        );

        var t1 = Transaction.builder().id(UUID.randomUUID()).user(user)
                .category(incomeCategory).type(TransactionType.INCOME)
                .amount(BigDecimal.valueOf(100)).date(LocalDate.of(2026, 4, 1)).build();
        var t2 = Transaction.builder().id(UUID.randomUUID()).user(user)
                .category(incomeCategory).type(TransactionType.INCOME)
                .amount(BigDecimal.valueOf(200)).date(LocalDate.of(2026, 4, 15)).build();

        when(transactionRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(List.of(t1, t2));

        var result = transactionService.findAll(filter, userId);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getDate()).isBefore(result.get(1).getDate());
    }

    // CT-038 — Ordenação padrão por data DESC
    @Test
    @DisplayName("CT-038: deve ordenar por data descendente por padrão")
    void shouldOrderByDateDescByDefault() {
        var filter = new TransactionFilterRequest(
                4, 2026, null, null, null, null, null, null, null, null
        );

        var t1 = Transaction.builder().id(UUID.randomUUID()).user(user)
                .category(incomeCategory).type(TransactionType.INCOME)
                .amount(BigDecimal.valueOf(100)).date(LocalDate.of(2026, 4, 15)).build();
        var t2 = Transaction.builder().id(UUID.randomUUID()).user(user)
                .category(incomeCategory).type(TransactionType.INCOME)
                .amount(BigDecimal.valueOf(200)).date(LocalDate.of(2026, 4, 1)).build();

        when(transactionRepository.findAll(any(Specification.class), any(Sort.class)))
                .thenReturn(List.of(t1, t2));

        var result = transactionService.findAll(filter, userId);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getDate()).isAfter(result.get(1).getDate());
    }

    // CT-039 — Ordenação por valor ASC
    @Test
    @DisplayName("CT-039: deve ordenar por valor ascendente")
    void shouldOrderByAmountAsc() {
        var filter = new TransactionFilterRequest(
                4, 2026, null, null, null, null, null, null, "amount", "ASC"
        );

        when(transactionRepository.findAll(any(Specification.class),  any(Sort.class)))
                .thenReturn(List.of(income(100), income(500)));

        var result = transactionService.findAll(filter, userId);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getAmount()).isLessThan(result.get(1).getAmount());
    }

    // CT-040 — Ordenação por valor DESC
    @Test
    @DisplayName("CT-040: deve ordenar por valor descendente")
    void shouldOrderByAmountDesc() {
        var filter = new TransactionFilterRequest(
                4, 2026, null, null, null, null, null, null, "amount", "DESC"
        );

        when(transactionRepository.findAll(any(Specification.class),  any(Sort.class)))
                .thenReturn(List.of(income(500), income(100)));

        var result = transactionService.findAll(filter, userId);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getAmount()).isGreaterThan(result.get(1).getAmount());
    }
}