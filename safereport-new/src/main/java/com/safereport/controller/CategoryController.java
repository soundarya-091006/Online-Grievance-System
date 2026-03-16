package com.safereport.controller;

import com.safereport.dto.response.ApiResponse;
import com.safereport.entity.Category;
import com.safereport.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Category>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(categoryRepository.findByActiveTrue()));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Category>>> getAllIncludingInactive() {
        return ResponseEntity.ok(ApiResponse.success(categoryRepository.findAll()));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Category>> create(@RequestBody Category category) {
        if (categoryRepository.existsByName(category.getName())) {
            throw new RuntimeException("Category with this name already exists");
        }
        return ResponseEntity.ok(
                ApiResponse.success("Category created", categoryRepository.save(category)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Category>> update(
            @PathVariable("id") Long id,
            @RequestBody Category updated) {
        Category cat = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        if (updated.getName() != null) cat.setName(updated.getName());
        if (updated.getDescription() != null) cat.setDescription(updated.getDescription());
        if (updated.getIcon() != null) cat.setIcon(updated.getIcon());
        return ResponseEntity.ok(ApiResponse.success("Category updated", categoryRepository.save(cat)));
    }

    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Category>> toggle(@PathVariable("id") Long id) {
        Category cat = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        cat.setActive(!cat.isActive());
        return ResponseEntity.ok(ApiResponse.success("Category status toggled", categoryRepository.save(cat)));
    }
}
