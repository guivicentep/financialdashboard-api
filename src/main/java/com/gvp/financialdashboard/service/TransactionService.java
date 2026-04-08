package com.gvp.financialdashboard.service;

import com.gvp.financialdashboard.domain.dto.CreateTransactionRequest;
import com.gvp.financialdashboard.domain.dto.TransactionFilterRequest;
import com.gvp.financialdashboard.domain.dto.UpdateTransactionRequest;
import com.gvp.financialdashboard.domain.entity.Transaction;

import java.util.List;
import java.util.UUID;

public interface TransactionService {
    Transaction create(CreateTransactionRequest request, UUID userId);
    Transaction update(UUID transactionId, UpdateTransactionRequest request, UUID userId);
    void delete(UUID transactionId, UUID userId);
    List<Transaction> findAll(TransactionFilterRequest filter, UUID userId);
}
