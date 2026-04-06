package com.gvp.financialdashboard.domain.entity;

import com.gvp.financialdashboard.domain.enums.RecurrenceType;
import com.gvp.financialdashboard.domain.enums.TransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "recurring_transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecurringTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "transaction_type")
    private TransactionType type;

    @Column(nullable = false, precision = 9, scale = 2)
    private BigDecimal amount;

    @Column(length = 100)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "recurrence_type", nullable = false, columnDefinition = "recurrence_type")
    private RecurrenceType recurrenceType;

    @Column(name = "recurrence_day", nullable = false)
    private Short recurrenceDay;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.isActive == null) this.isActive = true;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }


}
