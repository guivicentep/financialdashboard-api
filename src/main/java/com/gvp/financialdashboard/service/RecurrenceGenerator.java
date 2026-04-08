package com.gvp.financialdashboard.service;

import com.gvp.financialdashboard.domain.entity.RecurringTransaction;
import com.gvp.financialdashboard.domain.entity.Transaction;

import java.util.List;

public interface RecurrenceGenerator {
    List<Transaction> generate(RecurringTransaction template);
}
