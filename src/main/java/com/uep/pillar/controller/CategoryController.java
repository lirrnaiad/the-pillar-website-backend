package com.uep.pillar.controller;

import com.uep.pillar.dto.CreateCategoryInput;
import com.uep.pillar.dto.UpdateCategoryInput;
import com.uep.pillar.model.Category;
import com.uep.pillar.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for category management operations.
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * Get all categories.
     * GET /api/categories
     */
    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(categoryService.findAll());
    }

    /**
     * Get category by ID.
     * GET /api/categories/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Integer id) {
        return ResponseEntity.ok(categoryService.findById(id));
    }

    /**
     * Get category by slug.
     * GET /api/categories/slug/{slug}
     */
    @GetMapping("/slug/{slug}")
    public ResponseEntity<Category> getCategoryBySlug(@PathVariable String slug) {
        return categoryService.findBySlug(slug)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a new category.
     * POST /api/categories
     */
    @PostMapping
    public ResponseEntity<Category> createCategory(@RequestBody CreateCategoryInput input) {
        Category category = categoryService.create(
                input.getName(),
                input.getDescription(),
                input.getColor()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(category);
    }

    /**
     * Update an existing category.
     * PUT /api/categories/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(
            @PathVariable Integer id,
            @RequestBody UpdateCategoryInput input) {
        Category category = categoryService.update(
                id,
                input.getName(),
                input.getDescription(),
                input.getColor()
        );
        return ResponseEntity.ok(category);
    }

    /**
     * Delete a category.
     * DELETE /api/categories/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Integer id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

