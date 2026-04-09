package com.gvp.financialdashboard.service.impl;

import com.gvp.financialdashboard.domain.dto.DashboardFilterRequest;
import com.gvp.financialdashboard.domain.dto.DashboardSummaryResponse;
import com.gvp.financialdashboard.domain.entity.Transaction;
import com.gvp.financialdashboard.domain.enums.TransactionType;
import com.gvp.financialdashboard.repository.TransactionRepository;
import com.gvp.financialdashboard.repository.spec.TransactionSpecification;
import com.gvp.financialdashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final TransactionRepository transactionRepository;

    @Override
    public DashboardSummaryResponse getSummary(DashboardFilterRequest filter, UUID userId) {
        var now = LocalDate.now();
        var month = filter.month() != null ? filter.month() : now.getMonthValue();
        var year = filter.year() != null ? filter.year() : now.getYear();

        var spec = TransactionSpecification.withFilters(
                userId,
                month,
                year,
                filter.type(),
                filter.categoryId(),
                null,
                null,
                null,
                null
        );

        var transactions = transactionRepository.findAll(spec);

        var totalIncome = transactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        var totalExpense = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        var balance = totalIncome.subtract(totalExpense);

        return new DashboardSummaryResponse(
                totalIncome,
                totalExpense,
                balance,
                transactions.size()
        );
    }
}
