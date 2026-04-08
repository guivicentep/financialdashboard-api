package com.gvp.financialdashboard.repository.spec;

import com.gvp.financialdashboard.domain.entity.Transaction;
import com.gvp.financialdashboard.domain.enums.AmountOperator;
import com.gvp.financialdashboard.domain.enums.TransactionType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

public class TransactionSpecification {

    private TransactionSpecification() {}

    public static Specification<Transaction> withFilters(
            UUID userId,
            int month,
            int year,
            TransactionType type,
            UUID categoryId,
            AmountOperator amountOperator,
            BigDecimal amount,
            BigDecimal amountMin,
            BigDecimal amountMax
    ) {
        return (root, query, cb) -> {
            var predicates = new ArrayList<Predicate>();

            // obrigatórios
            predicates.add(cb.equal(root.get("user").get("id"), userId));
            predicates.add(cb.equal(cb.function("MONTH", Integer.class, root.get("date")), month));
            predicates.add(cb.equal(cb.function("YEAR", Integer.class, root.get("date")), year));

            // opcionais
            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }

            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }

            if (amountOperator != null && amount != null) {
                predicates.add(switch (amountOperator) {
                    case EQUAL ->  cb.equal(root.get("amount"), amount);
                    case GREATER_THAN ->  cb.greaterThan(root.get("amount"), amount);
                    case LESS_THAN ->  cb.lessThan(root.get("amount"), amount);
                    case BETWEEN ->  cb.between(root.get("amount"), amountMin, amountMax);
                });
            }

            if (amountOperator == AmountOperator.BETWEEN && amountMin != null && amountMax != null) {
                predicates.removeIf(Objects::isNull);
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
