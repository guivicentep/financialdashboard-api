package com.gvp.financialdashboard.service;

import com.gvp.financialdashboard.domain.dto.CreateTransactionRequest;
import com.gvp.financialdashboard.domain.dto.UpdateTransactionRequest;
import com.gvp.financialdashboard.domain.entity.Category;
import com.gvp.financialdashboard.domain.entity.Transaction;
import com.gvp.financialdashboard.domain.entity.User;
import com.gvp.financialdashboard.domain.enums.TransactionType;
import com.gvp.financialdashboard.exception.BusinessException;
import com.gvp.financialdashboard.repository.CategoryRepository;
import com.gvp.financialdashboard.repository.TransactionRepository;
import com.gvp.financialdashboard.repository.UserRepository;
import com.gvp.financialdashboard.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionService")
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private User user;
    private Category incomeCategory;
    private Category expenseCategory;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.randomUUID())
                .clerkId("clerk_test_123")
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

    // ─────────────────────────────────────────────
    // CT-001 — Criar transação de entrada válida
    // ─────────────────────────────────────────────
    @Test
    @DisplayName("CT-001: deve criar transação de entrada válida")
    void shouldCreateValidIncomeTransaction() {
        var request = new CreateTransactionRequest(
                TransactionType.INCOME,
                incomeCategory.getId(),
                new BigDecimal("500.00"),
                LocalDate.now(),
                null
        );

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(categoryRepository.findById(incomeCategory.getId())).thenReturn(Optional.of(incomeCategory));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        Transaction result = transactionService.create(request, user.getId());

        assertThat(result).isNotNull();
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(result.getType()).isEqualTo(TransactionType.INCOME);
        assertThat(result.getCategory()).isEqualTo(incomeCategory);
        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getAmount()).isPositive();
        verify(transactionRepository).save(any(Transaction.class));
    }

    // ─────────────────────────────────────────────
    // CT-002 — Criar transação de saída válida
    // ─────────────────────────────────────────────
    @Test
    @DisplayName("CT-002: deve criar transação de saída válida")
    void shouldCreateValidExpenseTransaction() {
        var request = new CreateTransactionRequest(
                TransactionType.EXPENSE,
                expenseCategory.getId(),
                new BigDecimal("150.00"),
                LocalDate.now(),
                null
        );

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(categoryRepository.findById(expenseCategory.getId())).thenReturn(Optional.of(expenseCategory));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        Transaction result = transactionService.create(request, user.getId());

        assertThat(result).isNotNull();
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("150.00"));
        assertThat(result.getType()).isEqualTo(TransactionType.EXPENSE);
        assertThat(result.getAmount()).isPositive();
        verify(transactionRepository).save(any(Transaction.class));
    }

    // ─────────────────────────────────────────────
    // CT-013 — Rejeitar categoria incompatível (INCOME com categoria EXPENSE)
    // ─────────────────────────────────────────────
    @Test
    @DisplayName("CT-013: deve rejeitar categoria incompatível com o tipo da transação")
    void shouldRejectIncompatibleCategoryForIncome() {
        var request = new CreateTransactionRequest(
                TransactionType.INCOME,
                expenseCategory.getId(),
                new BigDecimal("100.00"),
                LocalDate.now(),
                null
        );

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(categoryRepository.findById(expenseCategory.getId())).thenReturn(Optional.of(expenseCategory));

        assertThatThrownBy(() -> transactionService.create(request, user.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("A categoria não é compatível com o tipo da transação");

        verify(transactionRepository, never()).save(any());
    }

    // ─────────────────────────────────────────────
    // CT-014 — Rejeitar categoria incompatível (EXPENSE com categoria INCOME)
    // ─────────────────────────────────────────────
    @Test
    @DisplayName("CT-014: deve rejeitar categoria INCOME para transação EXPENSE")
    void shouldRejectIncompatibleCategoryForExpense() {
        var request = new CreateTransactionRequest(
                TransactionType.EXPENSE,
                incomeCategory.getId(),
                new BigDecimal("100.00"),
                LocalDate.now(),
                null
        );

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(categoryRepository.findById(incomeCategory.getId())).thenReturn(Optional.of(incomeCategory));

        assertThatThrownBy(() -> transactionService.create(request, user.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("A categoria não é compatível com o tipo da transação");

        verify(transactionRepository, never()).save(any());
    }

    // ─────────────────────────────────────────────
    // CT-018 — Usuário não acessa transação de outro usuário
    // ─────────────────────────────────────────────
    @Test
    @DisplayName("CT-018: deve lançar exceção quando usuário não encontrado")
    void shouldThrowWhenUserNotFound() {
        var request = new CreateTransactionRequest(
                TransactionType.INCOME,
                incomeCategory.getId(),
                new BigDecimal("100.00"),
                LocalDate.now(),
                null
        );

        when(userRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.create(request, UUID.randomUUID()))
                .isInstanceOf(BusinessException.class);

        verify(transactionRepository, never()).save(any());
    }
    // ─────────────────────────────────────────────
    // CT-015 — Editar todos os campos de uma transação
    // ─────────────────────────────────────────────
    @Test
    @DisplayName("CT-015: deve editar todos os campos de uma transação existente")
    void shouldUpdateAllFieldsOfTransaction() {
        var transactionId = UUID.randomUUID();
        var newCategory = Category.builder()
                .id(UUID.randomUUID())
                .name("Freelance")
                .type(TransactionType.INCOME)
                .build();

        var existing = Transaction.builder()
                .id(transactionId)
                .user(user)
                .category(incomeCategory)
                .type(TransactionType.INCOME)
                .amount(new BigDecimal("500.00"))
                .date(LocalDate.now().minusDays(5))
                .description("antiga")
                .build();

        var request = new UpdateTransactionRequest(
                TransactionType.INCOME,
                newCategory.getId(),
                new BigDecimal("1000.00"),
                LocalDate.now(),
                "nova descrição"
        );

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(existing));
        when(categoryRepository.findById(newCategory.getId())).thenReturn(Optional.of(newCategory));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));

        Transaction result = transactionService.update(transactionId, request, user.getId());

        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("1000.00"));
        assertThat(result.getCategory()).isEqualTo(newCategory);
        assertThat(result.getDescription()).isEqualTo("nova descrição");
        assertThat(result.getDate()).isEqualTo(LocalDate.now());
        verify(transactionRepository).save(existing);
    }

    // ─────────────────────────────────────────────
    // CT-016 — Editar transação de outro usuário lança exceção
    // ─────────────────────────────────────────────
    @Test
    @DisplayName("CT-016: deve rejeitar edição de transação de outro usuário")
    void shouldRejectUpdateOfOtherUsersTransaction() {
        var otherUser = User.builder()
                .id(UUID.randomUUID())
                .clerkId("clerk_other")
                .build();

        var transactionId = UUID.randomUUID();
        var existing = Transaction.builder()
                .id(transactionId)
                .user(otherUser)
                .category(incomeCategory)
                .type(TransactionType.INCOME)
                .amount(new BigDecimal("500.00"))
                .date(LocalDate.now())
                .build();

        var request = new UpdateTransactionRequest(
                TransactionType.INCOME,
                incomeCategory.getId(),
                new BigDecimal("200.00"),
                LocalDate.now(),
                null
        );

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> transactionService.update(transactionId, request, user.getId()))
                .isInstanceOf(BusinessException.class);

        verify(transactionRepository, never()).save(any());
    }

    // ─────────────────────────────────────────────
    // CT-017 — Excluir transação existente
    // ─────────────────────────────────────────────
    @Test
    @DisplayName("CT-017: deve excluir transação existente do usuário")
    void shouldDeleteExistingTransaction() {
        var transactionId = UUID.randomUUID();
        var existing = Transaction.builder()
                .id(transactionId)
                .user(user)
                .category(incomeCategory)
                .type(TransactionType.INCOME)
                .amount(new BigDecimal("500.00"))
                .date(LocalDate.now())
                .build();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(existing));

        transactionService.delete(transactionId, user.getId());

        verify(transactionRepository).delete(existing);
    }

    // ─────────────────────────────────────────────
    // CT-017b — Excluir transação de outro usuário lança exceção
    // ─────────────────────────────────────────────
    @Test
    @DisplayName("CT-017b: deve rejeitar exclusão de transação de outro usuário")
    void shouldRejectDeleteOfOtherUsersTransaction() {
        var otherUser = User.builder()
                .id(UUID.randomUUID())
                .clerkId("clerk_other")
                .build();

        var transactionId = UUID.randomUUID();
        var existing = Transaction.builder()
                .id(transactionId)
                .user(otherUser)
                .category(incomeCategory)
                .type(TransactionType.INCOME)
                .amount(new BigDecimal("500.00"))
                .date(LocalDate.now())
                .build();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> transactionService.delete(transactionId, user.getId()))
                .isInstanceOf(BusinessException.class);

        verify(transactionRepository, never()).delete(any());
    }
}