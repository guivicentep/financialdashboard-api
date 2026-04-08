package com.gvp.financialdashboard.service.impl;

import com.gvp.financialdashboard.domain.entity.RecurringTransaction;
import com.gvp.financialdashboard.domain.entity.Transaction;
import com.gvp.financialdashboard.domain.enums.RecurrenceType;
import com.gvp.financialdashboard.service.RecurrenceGenerator;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class RecurrenceGeneratorImpl implements RecurrenceGenerator {

    @Override
    public List<Transaction> generate(RecurringTransaction template) {
        var instances = new ArrayList<Transaction>();
        var baseDate = resolveFirstDate(template);

        for (int i = 0; i < template.getOccurrences(); i++) {
            var date = shift(baseDate, template.getRecurrenceType(), i);
            instances.add(Transaction.builder()
                    .user(template.getUser())
                    .category(template.getCategory())
                    .type(template.getType())
                    .amount(template.getAmount())
                    .description(template.getDescription())
                    .date(date)
                    .recurringTransaction(template)
                    .build());
        }

        return instances;
    }

    private LocalDate resolveFirstDate(RecurringTransaction template) {
        var start = template.getStartDate();
        return switch (template.getRecurrenceType()) {
            case MONTHLY -> start.withDayOfMonth(
                    Math.min(template.getRecurrenceDay(),
                            start.lengthOfMonth()));
            case DAILY, WEEKLY, YEARLY -> start;
        };
    }

    private LocalDate shift(LocalDate base, RecurrenceType type, int index) {
        return switch (type) {
            case DAILY -> base.plusDays(index);
            case WEEKLY -> base.plusWeeks(index);
            case MONTHLY -> {
                var shifted = base.plusMonths(index);
                yield shifted.withDayOfMonth(
                        Math.min(base.getDayOfMonth(), shifted.lengthOfMonth()));
            }
            case YEARLY -> base.plusYears(index);
        };
    }
}