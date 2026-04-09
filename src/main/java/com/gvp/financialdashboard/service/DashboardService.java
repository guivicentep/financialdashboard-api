package com.gvp.financialdashboard.service;

import com.gvp.financialdashboard.domain.dto.DashboardFilterRequest;
import com.gvp.financialdashboard.domain.dto.DashboardSummaryResponse;

import java.util.UUID;

public interface DashboardService {
    DashboardSummaryResponse getSummary(DashboardFilterRequest filter, UUID userId);
}
