package com.gvp.financialdashboard.controller;

import com.gvp.financialdashboard.domain.dto.CategoryResponse;
import com.gvp.financialdashboard.domain.dto.CreateCategoryRequest;
import com.gvp.financialdashboard.domain.dto.CreateUserRequest;
import com.gvp.financialdashboard.domain.dto.UserResponse;
import com.gvp.financialdashboard.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CreateCategoryRequest request) {
        var response = categoryService.create(request);
        return ResponseEntity.created(URI.create("/categories/" + response.id())).body(response);
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> list() {
        return ResponseEntity.ok(categoryService.findAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
