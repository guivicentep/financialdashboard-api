package com.gvp.financialdashboard.repository;

import com.gvp.financialdashboard.domain.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID>, JpaSpecificationExecutor<Transaction> {
    @Query("""
        SELECT t FROM Transaction t
        WHERE t.user.id = :userId
        AND FUNCTION('MONTH', t.date) = :month
        AND FUNCTION('YEAR', t.date) = :year
        """)
    List<Transaction> findByUserIdAndMonthAndYear(UUID userId, int month, int year);

    List<Transaction> findByRecurringTransactionId(UUID recurringTransactionId);
}
