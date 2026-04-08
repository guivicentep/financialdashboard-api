package com.gvp.financialdashboard.domain.dto;

import java.math.BigDecimal;

public record BalanceSummaryResponse (
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal balance
) {}
