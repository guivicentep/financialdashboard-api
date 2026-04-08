package com.gvp.financialdashboard.service;

import com.gvp.financialdashboard.domain.dto.BalanceSummaryResponse;

import java.util.UUID;

public interface BalanceService {
    BalanceSummaryResponse getSummary(UUID userId, int month, int year);
}
