package com.gvp.financialdashboard.service.impl;

import com.gvp.financialdashboard.domain.dto.CreateRecurringTransactionRequest;
import com.gvp.financialdashboard.domain.dto.UpdateRecurringTransactionRequest;
import com.gvp.financialdashboard.domain.entity.RecurringTransaction;
import com.gvp.financialdashboard.exception.BusinessException;
import com.gvp.financialdashboard.repository.CategoryRepository;
import com.gvp.financialdashboard.repository.RecurringTransactionRepository;
import com.gvp.financialdashboard.repository.TransactionRepository;
import com.gvp.financialdashboard.repository.UserRepository;
import com.gvp.financialdashboard.service.RecurrenceGenerator;
import com.gvp.financialdashboard.service.RecurringTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecurringTransactionServiceImpl implements RecurringTransactionService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final RecurringTransactionRepository recurringTransactionRepository;
    private final TransactionRepository transactionRepository;
    private final RecurrenceGenerator recurrenceGenerator;

    @Override
    @Transactional
    public RecurringTransaction create(CreateRecurringTransactionRequest request, UUID userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));

        var category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new BusinessException("Categoria não encontrada"));

        if (category.getType() != request.type()) {
            throw new BusinessException("A categoria não é compatível com o tipo da transação");
        }

        var template = RecurringTransaction.builder()
                .user(user)
                .category(category)
                .type(request.type())
                .amount(request.amount())
                .description(request.description())
                .recurrenceType(request.recurrenceType())
                .recurrenceDay(request.recurrenceDay())
                .startDate(request.startDate())
                .occurrences(request.occurrences())
                .build();

        var saved = recurringTransactionRepository.save(template);
        var instances = recurrenceGenerator.generate(saved);
        transactionRepository.saveAll(instances);

        return saved;
    }

    @Override
    @Transactional
    public RecurringTransaction update(UUID templateId, UpdateRecurringTransactionRequest request, UUID userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));

        var template = recurringTransactionRepository.findById(templateId)
                .orElseThrow(() -> new BusinessException("Template não encontrado"));

        if (!template.getUser().getId().equals(user.getId())) {
            throw new BusinessException("Template não encontrado");
        }

        var category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new BusinessException("Categoria não encontrada"));

        if (category.getType() != request.type()) {
            throw new BusinessException("A categoria não é compatível com o tipo da transação");
        }

        // Separar instâncias passadas das futuras
        var today = LocalDate.now();
        var allInstances = transactionRepository.findByRecurringTransactionId(templateId);
        var futureInstances = allInstances.stream()
                .filter(t -> !t.getDate().isBefore(today))
                .toList();

        // Deletar apenas as futuras
        transactionRepository.deleteAll(futureInstances);

        // Atualizar template
        template.setType(request.type());
        template.setCategory(category);
        template.setAmount(request.amount());
        template.setDescription(request.description());
        template.setRecurrenceType(request.recurrenceType());
        template.setRecurrenceDay(request.recurrenceDay());
        template.setStartDate(request.startDate());
        template.setOccurrences(request.occurrences());

        var saved = recurringTransactionRepository.save(template);

        // Regerar instâncias futuras
        var newInstances = recurrenceGenerator.generate(saved);
        transactionRepository.saveAll(newInstances);

        return saved;
    }

    @Override
    @Transactional
    public void delete(UUID templateId, UUID userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));

        var template = recurringTransactionRepository.findById(templateId)
                .orElseThrow(() -> new BusinessException("Template não encontrado"));

        if (!template.getUser().getId().equals(user.getId())) {
            throw new BusinessException("Template não encontrado");
        }

        var today = LocalDate.now();
        var allInstances = transactionRepository.findByRecurringTransactionId(templateId);

        var futureInstances = allInstances.stream()
                .filter(t -> !t.getDate().isBefore(today))
                .toList();

        var pastInstances = allInstances.stream()
                .filter(t -> t.getDate().isBefore(today))
                .toList();

        // Deletar futuras
        transactionRepository.deleteAll(futureInstances);

        // Desvincular passadas
        pastInstances.forEach(t -> t.setRecurringTransaction(null));
        transactionRepository.saveAll(pastInstances);

        recurringTransactionRepository.delete(template);
    }

    @Override
    public List<RecurringTransaction> findAll(UUID userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));

        return recurringTransactionRepository.findByUserId(userId);
    }
}