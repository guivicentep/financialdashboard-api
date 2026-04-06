package com.gvp.financialdashboard.service;

import com.gvp.financialdashboard.domain.dto.CreateTransactionRequest;
import com.gvp.financialdashboard.domain.dto.UpdateTransactionRequest;
import com.gvp.financialdashboard.domain.entity.Transaction;

import java.util.UUID;

public interface TransactionService {
    Transaction create(CreateTransactionRequest request, UUID userId);
    Transaction update(UUID transactionId, UpdateTransactionRequest request, UUID userId);
    void delete(UUID transactionId, UUID userId);
}
