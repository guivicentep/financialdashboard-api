package com.gvp.financialdashboard.service.impl;

import com.gvp.financialdashboard.domain.dto.CreateTransactionRequest;
import com.gvp.financialdashboard.domain.dto.TransactionFilterRequest;
import com.gvp.financialdashboard.domain.dto.UpdateTransactionRequest;
import com.gvp.financialdashboard.domain.entity.Transaction;
import com.gvp.financialdashboard.exception.BusinessException;
import com.gvp.financialdashboard.repository.CategoryRepository;
import com.gvp.financialdashboard.repository.TransactionRepository;
import com.gvp.financialdashboard.repository.UserRepository;
import com.gvp.financialdashboard.repository.spec.TransactionSpecification;
import com.gvp.financialdashboard.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Override
    public Transaction create(CreateTransactionRequest request, UUID userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));

        var category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new BusinessException("Categoria não encontrada"));

        if (category.getType() != request.type()) {
            throw new BusinessException("A categoria não é compatível com o tipo da transação");
        }

        var transaction = Transaction.builder()
                .user(user)
                .category(category)
                .type(request.type())
                .amount(request.amount())
                .date(request.date())
                .description(request.description())
                .build();

        return transactionRepository.save(transaction);
    }

    @Override
    public Transaction update(UUID transactionId, UpdateTransactionRequest request, UUID userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));

        var transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new BusinessException("Transação não encontrada"));

        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new BusinessException("Transação não encontrada");
        }

        var category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new BusinessException("Categoria não encontrada"));

        if (category.getType() != request.type()) {
            throw new BusinessException("A categoria não é compatível com o tipo da transação");
        }

        transaction.setType(request.type());
        transaction.setCategory(category);
        transaction.setAmount(request.amount());
        transaction.setDate(request.date());
        transaction.setDescription(request.description());

        return transactionRepository.save(transaction);
    }

    @Override
    public void delete(UUID transactionId, UUID userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));

        var transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new BusinessException("Transação não encontrada"));

        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new BusinessException("Transação não encontrada");
        }

        ((org.springframework.data.repository.CrudRepository<Transaction, UUID>) transactionRepository)
                .delete(transaction);
    }

    @Override
    public List<Transaction> findAll(TransactionFilterRequest filter, UUID userId) {
        var now =  LocalDate.now();
        var month = filter.month() != null ? filter.month() : now.getMonthValue();
        var year =  filter.year() != null ? filter.year() : now.getYear();

        var spec = TransactionSpecification.withFilters(
                userId,
                month,
                year,
                filter.type(),
                filter.categoryId(),
                filter.amountOperator(),
                filter.amount(),
                filter.amountMin(),
                filter.amountMax()
        );

        var orderBy = filter.orderBy() != null ? filter.orderBy() : "date";
        var direction = "ASC".equalsIgnoreCase(filter.direction()) ? Sort.Direction.ASC : Sort.Direction.DESC;

        return transactionRepository.findAll(spec, Sort.by(direction, orderBy));
    }
}
