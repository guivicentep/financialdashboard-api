package com.gvp.financialdashboard.service;

import com.gvp.financialdashboard.domain.dto.DashboardFilterRequest;
import com.gvp.financialdashboard.domain.entity.Category;
import com.gvp.financialdashboard.domain.entity.Transaction;
import com.gvp.financialdashboard.domain.entity.User;
import com.gvp.financialdashboard.domain.enums.TransactionType;
import com.gvp.financialdashboard.repository.TransactionRepository;
import com.gvp.financialdashboard.service.impl.DashboardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardService — Resumo financeiro")
class DashboardServiceTest {

    @Mock private TransactionRepository transactionRepository;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    private User user;
    private Category incomeCategory;
    private Category expenseCategory;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.randomUUID())
                .clerkId("clerk_test")
                .build();

        incomeCategory = Category.builder()
                .id(UUID.randomUUID())
                .name("Salário")
                .type(TransactionType.INCOME)
                .build();

        expenseCategory = Category.builder()
                .id(UUID.randomUUID())
                .name("Alimentação")
                .type(TransactionType.EXPENSE)
                .build();
    }

    private Transaction income(BigDecimal amount) {
        return Transaction.builder()
                .id(UUID.randomUUID())
                .user(user)
                .category(incomeCategory)
                .type(TransactionType.INCOME)
                .amount(amount)
                .date(LocalDate.of(2026, 4, 5))
                .build();
    }

    private Transaction expense(BigDecimal amount) {
        return Transaction.builder()
                .id(UUID.randomUUID())
                .user(user)
                .category(expenseCategory)
                .type(TransactionType.EXPENSE)
                .amount(amount)
                .date(LocalDate.of(2026, 4, 10))
                .build();
    }

    private DashboardFilterRequest filterFor(int month, int year) {
        return new DashboardFilterRequest(month, year, null, null);
    }

    // ─────────────────────────────────────────────
    // CT-057 — Mês com receitas e despesas calcula balance
    // ─────────────────────────────────────────────
    @Test
    @DisplayName("CT-057: deve calcular balance corretamente com receitas e despesas")
    void shouldCalculateBalanceWithIncomeAndExpense() {
        var transactions = List.of(
                income(new BigDecimal("3000.00")),
                income(new BigDecimal("1000.00")),
                expense(new BigDecimal("500.00")),
                expense(new BigDecimal("200.00"))
        );

        when(transactionRepository.findAll(any(Specification.class)))
                .thenReturn(transactions);

        var result = dashboardService.getSummary(filterFor(4, 2026), user.getId());

        assertThat(result.totalIncome()).isEqualByComparingTo("4000.00");
        assertThat(result.totalExpense()).isEqualByComparingTo("700.00");
        assertThat(result.balance()).isEqualByComparingTo("3300.00");
        assertThat(result.transactionCount()).isEqualTo(4);
    }

    // ─────────────────────────────────────────────
    // CT-058 — Mês sem transações retorna zeros
    // ─────────────────────────────────────────────
    @Test
    @DisplayName("CT-058: mês sem transações deve retornar todos os valores zerados")
    void shouldReturnZerosWhenNoTransactions() {
        when(transactionRepository.findAll(any(Specification.class)))
                .thenReturn(List.of());

        var result = dashboardService.getSummary(filterFor(4, 2026), user.getId());

        assertThat(result.totalIncome()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.totalExpense()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.balance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.transactionCount()).isZero();
    }

    // ─────────────────────────────────────────────
    // CT-059 — Mês só com receitas
    // ─────────────────────────────────────────────
    @Test
    @DisplayName("CT-059: mês só com receitas deve ter expense=0 e balance=totalIncome")
    void shouldHandleOnlyIncome() {
        var transactions = List.of(
                income(new BigDecimal("2000.00")),
                income(new BigDecimal("500.00"))
        );

        when(transactionRepository.findAll(any(Specification.class)))
                .thenReturn(transactions);

        var result = dashboardService.getSummary(filterFor(4, 2026), user.getId());

        assertThat(result.totalIncome()).isEqualByComparingTo("2500.00");
        assertThat(result.totalExpense()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.balance()).isEqualByComparingTo("2500.00");
    }

    // ─────────────────────────────────────────────
    // CT-060 — Mês só com despesas
    // ─────────────────────────────────────────────
    @Test
    @DisplayName("CT-060: mês só com despesas deve ter income=0 e balance negativo")
    void shouldHandleOnlyExpense() {
        var transactions = List.of(
                expense(new BigDecimal("800.00")),
                expense(new BigDecimal("200.00"))
        );

        when(transactionRepository.findAll(any(Specification.class)))
                .thenReturn(transactions);

        var result = dashboardService.getSummary(filterFor(4, 2026), user.getId());

        assertThat(result.totalIncome()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.totalExpense()).isEqualByComparingTo("1000.00");
        assertThat(result.balance()).isEqualByComparingTo("-1000.00");
    }

    // ─────────────────────────────────────────────
    // CT-061 — Usa mês/ano corrente quando não informado
    // ─────────────────────────────────────────────
    @Test
    @DisplayName("CT-061: deve usar mês e ano corrente quando não informados")
    void shouldUseCurrentMonthAndYearWhenNotProvided() {
        var filter = new DashboardFilterRequest(null, null, null, null);

        when(transactionRepository.findAll(any(Specification.class)))
                .thenReturn(List.of());

        var result = dashboardService.getSummary(filter, user.getId());

        assertThat(result).isNotNull();
        assertThat(result.transactionCount()).isZero();
    }

    // ─────────────────────────────────────────────
    // CT-062 — Balance positivo quando receitas > despesas
    // ─────────────────────────────────────────────
    @Test
    @DisplayName("CT-062: balance deve ser positivo quando receitas superam despesas")
    void shouldReturnPositiveBalance() {
        var transactions = List.of(
                income(new BigDecimal("5000.00")),
                expense(new BigDecimal("1000.00"))
        );

        when(transactionRepository.findAll(any(Specification.class)))
                .thenReturn(transactions);

        var result = dashboardService.getSummary(filterFor(4, 2026), user.getId());

        assertThat(result.balance()).isPositive();
        assertThat(result.balance()).isEqualByComparingTo("4000.00");
    }

    // ─────────────────────────────────────────────
    // CT-063 — Balance negativo quando despesas > receitas
    // ─────────────────────────────────────────────
    @Test
    @DisplayName("CT-063: balance deve ser negativo quando despesas superam receitas")
    void shouldReturnNegativeBalance() {
        var transactions = List.of(
                income(new BigDecimal("500.00")),
                expense(new BigDecimal("2000.00"))
        );

        when(transactionRepository.findAll(any(Specification.class)))
                .thenReturn(transactions);

        var result = dashboardService.getSummary(filterFor(4, 2026), user.getId());

        assertThat(result.balance()).isNegative();
        assertThat(result.balance()).isEqualByComparingTo("-1500.00");
    }

    // ─────────────────────────────────────────────
    // CT-064 — transactionCount reflete o total correto
    // ─────────────────────────────────────────────
    @Test
    @DisplayName("CT-064: transactionCount deve refletir o total de transações do mês")
    void shouldReturnCorrectTransactionCount() {
        var transactions = List.of(
                income(new BigDecimal("1000.00")),
                income(new BigDecimal("2000.00")),
                expense(new BigDecimal("300.00")),
                expense(new BigDecimal("400.00")),
                expense(new BigDecimal("500.00"))
        );

        when(transactionRepository.findAll(any(Specification.class)))
                .thenReturn(transactions);

        var result = dashboardService.getSummary(filterFor(4, 2026), user.getId());

        assertThat(result.transactionCount()).isEqualTo(5);
    }
}