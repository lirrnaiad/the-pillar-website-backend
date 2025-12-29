package com.uep.pillar.service;

import com.uep.pillar.exception.ResourceNotFoundException;
import com.uep.pillar.model.Category;
import com.uep.pillar.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CRUD tests for CategoryService.
 * Tests create, read, update, and delete operations.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CategoryServiceCRUDTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void testCreateCategory() {
        // CREATE
        Category category = categoryService.create(
                "News",
                "News articles category",
                "#E53935"
        );

        assertNotNull(category);
        assertNotNull(category.getId());
        assertEquals("News", category.getName());
        assertEquals("news", category.getSlug());
        assertEquals("News articles category", category.getDescription());
        assertEquals("#E53935", category.getColor());
    }

    @Test
    void testCreateCategoryAutoSlug() {
        // CREATE - slug should be auto-generated
        Category category = categoryService.create(
                "Science & Technology",
                "Sci-Tech articles",
                "#2196F3"
        );

        assertEquals("Science & Technology", category.getName());
        assertNotNull(category.getSlug());
        assertTrue(category.getSlug().contains("science"));
        assertTrue(category.getSlug().contains("technology"));
    }

    @Test
    void testCreateCategoryInvalidColor() {
        // CREATE with invalid color format
        assertThrows(IllegalArgumentException.class, () -> {
            categoryService.create(
                    "Invalid",
                    "Description",
                    "not-a-hex-color"
            );
        });
    }

    @Test
    void testReadCategory() {
        // CREATE first
        Category created = categoryService.create(
                "Read Test Category",
                "Description",
                "#E53935"
        );

        // READ by ID
        Category found = categoryService.findById(created.getId());
        assertNotNull(found);
        assertEquals(created.getId(), found.getId());
        assertEquals("Read Test Category", found.getName());

        // READ by slug
        Category foundBySlug = categoryService.findBySlug(created.getSlug())
                .orElseThrow();
        assertEquals(created.getId(), foundBySlug.getId());
    }

    @Test
    void testReadCategoryNotFound() {
        // READ non-existent category
        assertThrows(ResourceNotFoundException.class, () -> {
            categoryService.findById(99999);
        });
    }

    @Test
    void testUpdateCategory() {
        // CREATE
        Category category = categoryService.create(
                "Original Name",
                "Original description",
                "#E53935"
        );

        Integer categoryId = category.getId();
        String originalSlug = category.getSlug();

        // UPDATE
        Category updated = categoryService.update(
                categoryId,
                "Updated Name",
                "Updated description",
                "#2196F3"
        );

        assertEquals(categoryId, updated.getId());
        assertEquals("Updated Name", updated.getName());
        assertEquals("Updated description", updated.getDescription());
        assertEquals("#2196F3", updated.getColor());
        // Slug should change when name changes
        assertNotEquals(originalSlug, updated.getSlug());
    }

    @Test
    void testUpdateCategoryPartial() {
        // CREATE
        Category category = categoryService.create(
                "Partial Update",
                "Description",
                "#E53935"
        );

        // UPDATE only description
        Category updated = categoryService.update(
                category.getId(),
                null,
                "New description only",
                null
        );

        assertEquals("Partial Update", updated.getName()); // Unchanged
        assertEquals("New description only", updated.getDescription()); // Changed
        assertEquals("#E53935", updated.getColor()); // Unchanged
    }

    @Test
    void testDeleteCategory() {
        // CREATE
        Category category = categoryService.create(
                "To Be Deleted",
                "Description",
                "#E53935"
        );

        Integer categoryId = category.getId();
        assertTrue(categoryRepository.findById(categoryId).isPresent());

        // DELETE
        categoryService.delete(categoryId);

        // Verify deleted
        assertThrows(ResourceNotFoundException.class, () -> {
            categoryService.findById(categoryId);
        });
    }

    @Test
    void testListCategories() {
        // CREATE multiple categories
        categoryService.create("Category 1", "Desc 1", "#E53935");
        categoryService.create("Category 2", "Desc 2", "#2196F3");
        categoryService.create("Category 3", "Desc 3", "#4CAF50");

        // LIST all
        List<Category> allCategories = categoryService.findAll();
        assertTrue(allCategories.size() >= 3);
    }
}

