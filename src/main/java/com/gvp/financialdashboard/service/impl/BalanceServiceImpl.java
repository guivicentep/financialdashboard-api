package com.gvp.financialdashboard.service.impl;

import com.gvp.financialdashboard.domain.dto.BalanceSummaryResponse;
import com.gvp.financialdashboard.domain.enums.TransactionType;
import com.gvp.financialdashboard.repository.TransactionRepository;
import com.gvp.financialdashboard.service.BalanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BalanceServiceImpl implements BalanceService {

    private final TransactionRepository transactionRepository;

    @Override
    public BalanceSummaryResponse getSummary(UUID userId, int month, int year) {
        var transactions = transactionRepository.findByUserIdAndMonthAndYear(userId, month, year);

        var totalIncome = transactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .map(t -> t.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        var totalExpense = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .map(t -> t.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        var balance = totalIncome.subtract(totalExpense);

        return new BalanceSummaryResponse(totalIncome, totalExpense, balance);
    }
}
