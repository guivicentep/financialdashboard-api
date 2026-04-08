package com.gvp.financialdashboard.service;

import com.gvp.financialdashboard.domain.dto.CreateRecurringTransactionRequest;
import com.gvp.financialdashboard.domain.dto.UpdateRecurringTransactionRequest;
import com.gvp.financialdashboard.domain.entity.Category;
import com.gvp.financialdashboard.domain.entity.RecurringTransaction;
import com.gvp.financialdashboard.domain.entity.Transaction;
import com.gvp.financialdashboard.domain.entity.User;
import com.gvp.financialdashboard.domain.enums.RecurrenceType;
import com.gvp.financialdashboard.domain.enums.TransactionType;
import com.gvp.financialdashboard.exception.BusinessException;
import com.gvp.financialdashboard.repository.CategoryRepository;
import com.gvp.financialdashboard.repository.RecurringTransactionRepository;
import com.gvp.financialdashboard.repository.TransactionRepository;
import com.gvp.financialdashboard.repository.UserRepository;
import com.gvp.financialdashboard.service.impl.RecurringTransactionServiceImpl;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecurringTransactionService")
class RecurringTransactionServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private RecurringTransactionRepository recurringTransactionRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private RecurrenceGenerator recurrenceGenerator;

    @InjectMocks
    private RecurringTransactionServiceImpl recurringTransactionService;

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

    private CreateRecurringTransactionRequest createRequest(
            TransactionType type, UUID categoryId, int occurrences) {
        return new CreateRecurringTransactionRequest(
                type,
                categoryId,
                new BigDecimal("1000.00"),
                "descrição",
                RecurrenceType.MONTHLY,
                (short) 5,
                LocalDate.of(2026, 4, 5),
                (short) occurrences
        );
    }

    private RecurringTransaction buildTemplate(User user, Category category, int occurrences) {
        return RecurringTransaction.builder()
                .id(UUID.randomUUID())
                .user(user)
                .category(category)
                .type(category.getType())
                .amount(new BigDecimal("1000.00"))
                .description("descrição")
                .recurrenceType(RecurrenceType.MONTHLY)
                .recurrenceDay((short) 5)
                .startDate(LocalDate.of(2026, 4, 5))
                .occurrences((short) occurrences)
                .build();
    }

    // ─────────────────────────────────────────────
    // CT-041 — Criar template e gerar instâncias
    // ─────────────────────────────────────────────
    @Test
    @DisplayName("CT-041: deve criar template e gerar instâncias de transações")
    void shouldCreateTemplateAndGenerateInstances() {
        var request = createRequest(TransactionType.INCOME, incomeCategory.getId(), 3);
        var template = buildTemplate(user, incomeCategory, 3);

        var instances = List.of(
                Transaction.builder().id(UUID.randomUUID()).build(),
                Transaction.builder().id(UUID.randomUUID()).build(),
                Transaction.builder().id(UUID.randomUUID()).build()
        );

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(categoryRepository.findById(incomeCategory.getId())).thenReturn(Optional.of(incomeCategory));
        when(recurringTransactionRepository.save(any())).thenReturn(template);
        when(recurrenceGenerator.generate(template)).thenReturn(instances);
        when(transactionRepository.saveAll(instances)).thenReturn(instances);

        var result = recurringTransactionService.create(request, user.getId());

        assertThat(result).isEqualTo(template);
        verify(recurringTransactionRepository).save(any());
        verify(recurrenceGenerator).generate(template);
        verify(transactionRepository).saveAll(instances);
    }

    // ─────────────────────────────────────────────
    // CT-042 — Criar template com categoria incompatível
    // ─────────────────────────────────────────────
    @Test
    @DisplayName("CT-042: deve rejeitar template com categoria incompatível ao tipo")
    void shouldRejectIncompatibleCategory() {
        var request = createRequest(TransactionType.INCOME, expenseCategory.getId(), 3);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(categoryRepository.findById(expenseCategory.getId())).thenReturn(Optional.of(expenseCategory));

        assertThatThrownBy(() ->
                recurringTransactionService.create(request, user.getId()))
                .isInstanceOf(BusinessException.class);

        verify(recurringTransactionRepository, never()).save(any());
        verify(transactionRepository, never()).saveAll(any());
    }

    // ─────────────────────────────────────────────
    // CT-043 — Editar template preserva instâncias passadas
    // ─────────────────────────────────────────────
    @Test
    @DisplayName("CT-043: editar template deve preservar instâncias passadas e regerar futuras")
    void shouldPreservePastInstancesOnUpdate() {
        var templateId = UUID.randomUUID();
        var template = buildTemplate(user, incomeCategory, 6);
        template.setId(templateId);

        var today = LocalDate.now();

        var pastTransaction = Transaction.builder()
                .id(UUID.randomUUID())
                .recurringTransaction(template)
                .date(today.minusMonths(2))
                .build();

        var futureTransaction = Transaction.builder()
                .id(UUID.randomUUID())
                .recurringTransaction(template)
                .date(today.plusMonths(1))
                .build();

        var updateRequest = new UpdateRecurringTransactionRequest(
                TransactionType.INCOME,
                incomeCategory.getId(),
                new BigDecimal("1500.00"),
                "atualizado",
                RecurrenceType.MONTHLY,
                (short) 5,
                LocalDate.of(2026, 4, 5),
                (short) 6
        );

        var newInstances = List.of(
                Transaction.builder().id(UUID.randomUUID()).build()
        );

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(recurringTransactionRepository.findById(templateId)).thenReturn(Optional.of(template));
        when(categoryRepository.findById(incomeCategory.getId())).thenReturn(Optional.of(incomeCategory));
        when(transactionRepository.findByRecurringTransactionId(templateId))
                .thenReturn(List.of(pastTransaction, futureTransaction));
        when(recurringTransactionRepository.save(any())).thenReturn(template);
        when(recurrenceGenerator.generate(any())).thenReturn(newInstances);
        when(transactionRepository.saveAll(any())).thenReturn(newInstances);

        recurringTransactionService.update(templateId, updateRequest, user.getId());

        verify(transactionRepository).deleteAll(List.of(futureTransaction));
        verify(transactionRepository, never()).delete(pastTransaction);
        verify(transactionRepository).saveAll(any());
    }

    // ─────────────────────────────────────────────
    // CT-044 — Editar template de outro usuário lança exceção
    // ─────────────────────────────────────────────
    @Test
    @DisplayName("CT-044: deve rejeitar edição de template de outro usuário")
    void shouldRejectUpdateOfOtherUsersTemplate() {
        var otherUser = User.builder().id(UUID.randomUUID()).clerkId("clerk_other").build();
        var templateId = UUID.randomUUID();
        var template = buildTemplate(otherUser, incomeCategory, 3);
        template.setId(templateId);

        var updateRequest = new UpdateRecurringTransactionRequest(
                TransactionType.INCOME,
                incomeCategory.getId(),
                new BigDecimal("500.00"),
                null,
                RecurrenceType.MONTHLY,
                (short) 5,
                LocalDate.of(2026, 4, 5),
                (short) 3
        );

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(recurringTransactionRepository.findById(templateId)).thenReturn(Optional.of(template));

        assertThatThrownBy(() ->
                recurringTransactionService.update(templateId, updateRequest, user.getId()))
                .isInstanceOf(BusinessException.class);

        verify(recurringTransactionRepository, never()).save(any());
    }

    // ─────────────────────────────────────────────
    // CT-045 — Excluir template remove instâncias futuras
    // ─────────────────────────────────────────────
    @Test
    @DisplayName("CT-045: excluir template deve remover instâncias futuras")
    void shouldDeleteFutureInstancesOnTemplateDelete() {
        var templateId = UUID.randomUUID();
        var template = buildTemplate(user, incomeCategory, 6);
        template.setId(templateId);

        var today = LocalDate.now();

        var pastTransaction = Transaction.builder()
                .id(UUID.randomUUID())
                .recurringTransaction(template)
                .date(today.minusMonths(1))
                .build();

        var futureTransaction = Transaction.builder()
                .id(UUID.randomUUID())
                .recurringTransaction(template)
                .date(today.plusMonths(1))
                .build();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(recurringTransactionRepository.findById(templateId)).thenReturn(Optional.of(template));
        when(transactionRepository.findByRecurringTransactionId(templateId))
                .thenReturn(List.of(pastTransaction, futureTransaction));

        recurringTransactionService.delete(templateId, user.getId());

        verify(transactionRepository).deleteAll(List.of(futureTransaction));
        verify(recurringTransactionRepository).delete(template);
    }

    // ─────────────────────────────────────────────
    // CT-046 — Excluir template preserva instâncias passadas
    // ─────────────────────────────────────────────
    @Test
    @DisplayName("CT-046: excluir template deve preservar instâncias passadas com vínculo nulo")
    void shouldNullifyPastInstancesOnTemplateDelete() {
        var templateId = UUID.randomUUID();
        var template = buildTemplate(user, incomeCategory, 6);
        template.setId(templateId);

        var today = LocalDate.now();

        var pastTransaction = Transaction.builder()
                .id(UUID.randomUUID())
                .recurringTransaction(template)
                .date(today.minusMonths(2))
                .build();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(recurringTransactionRepository.findById(templateId)).thenReturn(Optional.of(template));
        when(transactionRepository.findByRecurringTransactionId(templateId))
                .thenReturn(List.of(pastTransaction));

        recurringTransactionService.delete(templateId, user.getId());

        assertThat(pastTransaction.getRecurringTransaction()).isNull();
        verify(transactionRepository).saveAll(List.of(pastTransaction));
        verify(recurringTransactionRepository).delete(template);
    }

    // ─────────────────────────────────────────────
    // CT-047 — Excluir template de outro usuário lança exceção
    // ─────────────────────────────────────────────
    @Test
    @DisplayName("CT-047: deve rejeitar exclusão de template de outro usuário")
    void shouldRejectDeleteOfOtherUsersTemplate() {
        var otherUser = User.builder().id(UUID.randomUUID()).clerkId("clerk_other").build();
        var templateId = UUID.randomUUID();
        var template = buildTemplate(otherUser, incomeCategory, 3);
        template.setId(templateId);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(recurringTransactionRepository.findById(templateId)).thenReturn(Optional.of(template));

        assertThatThrownBy(() ->
                recurringTransactionService.delete(templateId, user.getId()))
                .isInstanceOf(BusinessException.class);

        verify(recurringTransactionRepository, never()).delete(any());
    }

    // ─────────────────────────────────────────────
    // CT-048 — Criar template com 12 ocorrências gera 12 instâncias
    // ─────────────────────────────────────────────
    @Test
    @DisplayName("CT-048: deve gerar exatamente 12 instâncias para 12 ocorrências")
    void shouldGenerateExactly12Instances() {
        var request = createRequest(TransactionType.INCOME, incomeCategory.getId(), 12);
        var template = buildTemplate(user, incomeCategory, 12);

        var instances = java.util.stream.IntStream.range(0, 12)
                .mapToObj(i -> Transaction.builder().id(UUID.randomUUID()).build())
                .toList();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(categoryRepository.findById(incomeCategory.getId())).thenReturn(Optional.of(incomeCategory));
        when(recurringTransactionRepository.save(any())).thenReturn(template);
        when(recurrenceGenerator.generate(template)).thenReturn(instances);
        when(transactionRepository.saveAll(instances)).thenReturn(instances);

        recurringTransactionService.create(request, user.getId());

        verify(recurrenceGenerator).generate(template);
        verify(transactionRepository).saveAll(instances);
        assertThat(instances).hasSize(12);
    }
}