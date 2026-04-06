package com.gvp.financialdashboard.repository;

import com.gvp.financialdashboard.domain.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
}
