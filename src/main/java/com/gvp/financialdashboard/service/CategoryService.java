package com.gvp.financialdashboard.service;

import com.gvp.financialdashboard.domain.dto.CategoryResponse;
import com.gvp.financialdashboard.domain.dto.CreateCategoryRequest;

import java.util.List;
import java.util.UUID;

public interface CategoryService {
    CategoryResponse create(CreateCategoryRequest request);
    List<CategoryResponse> findAll();
    void delete(UUID id);
}
