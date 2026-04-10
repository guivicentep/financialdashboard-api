package com.gvp.financialdashboard.service.impl;

import com.gvp.financialdashboard.domain.dto.CategoryResponse;
import com.gvp.financialdashboard.domain.dto.CreateCategoryRequest;
import com.gvp.financialdashboard.domain.entity.Category;
import com.gvp.financialdashboard.exception.BusinessException;
import com.gvp.financialdashboard.repository.CategoryRepository;
import com.gvp.financialdashboard.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public CategoryResponse create(CreateCategoryRequest request) {
        var category = Category.builder()
                .name(request.name())
                .type(request.type())
                .build();

        return CategoryResponse.from(categoryRepository.save(category));
    }

    @Override
    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll()
                .stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @Override
    public void delete(UUID id) {
        var category = categoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Categoria não encontrada"));

        categoryRepository.delete(category);
    }
}
