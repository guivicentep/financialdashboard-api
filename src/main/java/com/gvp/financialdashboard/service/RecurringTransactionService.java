package com.gvp.financialdashboard.service;

import com.gvp.financialdashboard.domain.dto.CreateRecurringTransactionRequest;
import com.gvp.financialdashboard.domain.dto.UpdateRecurringTransactionRequest;
import com.gvp.financialdashboard.domain.entity.RecurringTransaction;

import java.util.List;
import java.util.UUID;

public interface RecurringTransactionService {
    RecurringTransaction create(CreateRecurringTransactionRequest request, UUID userId);
    RecurringTransaction update(UUID templateId, UpdateRecurringTransactionRequest request, UUID userId);
    void delete(UUID templateId, UUID userId);
    List<RecurringTransaction> findAll(UUID userId);
}
