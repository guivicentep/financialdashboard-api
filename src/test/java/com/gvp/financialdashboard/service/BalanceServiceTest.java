package com.gvp.financialdashboard.service;

import com.gvp.financialdashboard.domain.entity.Category;
import com.gvp.financialdashboard.domain.entity.Transaction;
import com.gvp.financialdashboard.domain.entity.User;
import com.gvp.financialdashboard.domain.enums.TransactionType;
import com.gvp.financialdashboard.repository.TransactionRepository;
import com.gvp.financialdashboard.service.impl.BalanceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("BalanceService")
class BalanceServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private BalanceServiceImpl balanceService;

    private UUID userId;
    private Category incomeCategory;
    private Category expenseCategory;
    private User user;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        user = User.builder()
                .id(userId)
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

    private Transaction income(double amount, int month) {
        return Transaction.builder()
                .id(UUID.randomUUID())
                .user(user)
                .category(incomeCategory)
                .type(TransactionType.INCOME)
                .amount(BigDecimal.valueOf(amount))
                .date(LocalDate.of(2026, month, 15))
                .build();
    }

    private Transaction expense(double amount, int month) {
        return Transaction.builder()
                .id(UUID.randomUUID())
                .user(user)
                .category(expenseCategory)
                .type(TransactionType.EXPENSE)
                .amount(BigDecimal.valueOf(amount))
                .date(LocalDate.of(2026, month, 15))
                .build();
    }

    // CT-019 — Calcular balanço com entradas e saídas
    @Test
    @DisplayName("CT-019: deve calcular balanço com entradas e saídas")
    void shouldCalculateBalanceWithIncomeAndExpense() {
        when(transactionRepository.findByUserIdAndMonthAndYear(userId, 4, 2026))
                .thenReturn(List.of(income(3000, 4), expense(1200, 4)));

        var result = balanceService.getSummary(userId, 4, 2026);

        assertThat(result.totalIncome()).isEqualByComparingTo("3000.00");
        assertThat(result.totalExpense()).isEqualByComparingTo("1200.00");
        assertThat(result.balance()).isEqualByComparingTo("1800.00");
    }

    // CT-020 — Calcular balanço com resultado negativo
    @Test
    @DisplayName("CT-020: deve calcular balanço negativo")
    void shouldCalculateNegativeBalance() {
        when(transactionRepository.findByUserIdAndMonthAndYear(userId, 4, 2026))
                .thenReturn(List.of(income(500, 4), expense(1200, 4)));

        var result = balanceService.getSummary(userId, 4, 2026);

        assertThat(result.totalIncome()).isEqualByComparingTo("500.00");
        assertThat(result.totalExpense()).isEqualByComparingTo("1200.00");
        assertThat(result.balance()).isEqualByComparingTo("-700.00");
    }

    // CT-021 — Calcular balanço com apenas entradas
    @Test
    @DisplayName("CT-021: deve calcular balanço com apenas entradas")
    void shouldCalculateBalanceWithOnlyIncome() {
        when(transactionRepository.findByUserIdAndMonthAndYear(userId, 4, 2026))
                .thenReturn(List.of(income(2000, 4)));

        var result = balanceService.getSummary(userId, 4, 2026);

        assertThat(result.totalIncome()).isEqualByComparingTo("2000.00");
        assertThat(result.totalExpense()).isEqualByComparingTo("0.00");
        assertThat(result.balance()).isEqualByComparingTo("2000.00");
    }

    // CT-022 — Calcular balanço com apenas saídas
    @Test
    @DisplayName("CT-022: deve calcular balanço com apenas saídas")
    void shouldCalculateBalanceWithOnlyExpense() {
        when(transactionRepository.findByUserIdAndMonthAndYear(userId, 4, 2026))
                .thenReturn(List.of(expense(800, 4)));

        var result = balanceService.getSummary(userId, 4, 2026);

        assertThat(result.totalIncome()).isEqualByComparingTo("0.00");
        assertThat(result.totalExpense()).isEqualByComparingTo("800.00");
        assertThat(result.balance()).isEqualByComparingTo("-800.00");
    }

    // CT-023 — Calcular balanço de mês sem transações
    @Test
    @DisplayName("CT-023: deve retornar zeros quando não há transações no mês")
    void shouldReturnZerosWhenNoTransactions() {
        when(transactionRepository.findByUserIdAndMonthAndYear(userId, 4, 2026))
                .thenReturn(List.of());

        var result = balanceService.getSummary(userId, 4, 2026);

        assertThat(result.totalIncome()).isEqualByComparingTo("0.00");
        assertThat(result.totalExpense()).isEqualByComparingTo("0.00");
        assertThat(result.balance()).isEqualByComparingTo("0.00");
    }

    // CT-024 — Balanço não inclui transações de outros meses
    @Test
    @DisplayName("CT-024: deve ignorar transações de outros meses")
    void shouldIgnoreTransactionsFromOtherMonths() {
        when(transactionRepository.findByUserIdAndMonthAndYear(userId, 1, 2026))
                .thenReturn(List.of(income(1000, 1)));

        var result = balanceService.getSummary(userId, 1, 2026);

        assertThat(result.totalIncome()).isEqualByComparingTo("1000.00");
        assertThat(result.totalExpense()).isEqualByComparingTo("0.00");
        assertThat(result.balance()).isEqualByComparingTo("1000.00");
    }

    // CT-025 — Agrupar gastos por categoria
    @Test
    @DisplayName("CT-025: deve agrupar gastos por categoria corretamente")
    void shouldGroupExpensesByCategory() {
        var transportCategory = Category.builder()
                .id(UUID.randomUUID())
                .name("Transporte")
                .type(TransactionType.EXPENSE)
                .build();

        var t1 = Transaction.builder().id(UUID.randomUUID()).user(user)
                .category(expenseCategory).type(TransactionType.EXPENSE)
                .amount(BigDecimal.valueOf(100)).date(LocalDate.of(2026, 4, 1)).build();
        var t2 = Transaction.builder().id(UUID.randomUUID()).user(user)
                .category(expenseCategory).type(TransactionType.EXPENSE)
                .amount(BigDecimal.valueOf(200)).date(LocalDate.of(2026, 4, 2)).build();
        var t3 = Transaction.builder().id(UUID.randomUUID()).user(user)
                .category(expenseCategory).type(TransactionType.EXPENSE)
                .amount(BigDecimal.valueOf(150)).date(LocalDate.of(2026, 4, 3)).build();
        var t4 = Transaction.builder().id(UUID.randomUUID()).user(user)
                .category(transportCategory).type(TransactionType.EXPENSE)
                .amount(BigDecimal.valueOf(80)).date(LocalDate.of(2026, 4, 4)).build();

        when(transactionRepository.findByUserIdAndMonthAndYear(userId, 4, 2026))
                .thenReturn(List.of(t1, t2, t3, t4));

        var result = balanceService.getSummary(userId, 4, 2026);

        assertThat(result.totalExpense()).isEqualByComparingTo("530.00");
        assertThat(result.totalIncome()).isEqualByComparingTo("0.00");
        assertThat(result.balance()).isEqualByComparingTo("-530.00");
    }

    // CT-026 — Agrupar entradas por categoria
    @Test
    @DisplayName("CT-026: deve agrupar entradas por categoria corretamente")
    void shouldGroupIncomeByCategory() {
        var freelanceCategory = Category.builder()
                .id(UUID.randomUUID())
                .name("Freelance")
                .type(TransactionType.INCOME)
                .build();

        var t1 = Transaction.builder().id(UUID.randomUUID()).user(user)
                .category(incomeCategory).type(TransactionType.INCOME)
                .amount(BigDecimal.valueOf(3000)).date(LocalDate.of(2026, 4, 1)).build();
        var t2 = Transaction.builder().id(UUID.randomUUID()).user(user)
                .category(incomeCategory).type(TransactionType.INCOME)
                .amount(BigDecimal.valueOf(500)).date(LocalDate.of(2026, 4, 2)).build();
        var t3 = Transaction.builder().id(UUID.randomUUID()).user(user)
                .category(freelanceCategory).type(TransactionType.INCOME)
                .amount(BigDecimal.valueOf(1000)).date(LocalDate.of(2026, 4, 3)).build();

        when(transactionRepository.findByUserIdAndMonthAndYear(userId, 4, 2026))
                .thenReturn(List.of(t1, t2, t3));

        var result = balanceService.getSummary(userId, 4, 2026);

        assertThat(result.totalIncome()).isEqualByComparingTo("4500.00");
        assertThat(result.totalExpense()).isEqualByComparingTo("0.00");
        assertThat(result.balance()).isEqualByComparingTo("4500.00");
    }

    // CT-027 — Período padrão é o mês corrente
    @Test
    @DisplayName("CT-027: deve usar mês e ano corrente como padrão")
    void shouldUseCurrentMonthAsDefault() {
        var now = LocalDate.now();
        when(transactionRepository.findByUserIdAndMonthAndYear(userId, now.getMonthValue(), now.getYear()))
                .thenReturn(List.of(income(1000, now.getMonthValue())));

        var result = balanceService.getSummary(userId, now.getMonthValue(), now.getYear());

        assertThat(result.totalIncome()).isEqualByComparingTo("1000.00");
    }
}