package com.uep.pillar.service;

import com.uep.pillar.exception.ResourceNotFoundException;
import com.uep.pillar.model.Category;
import com.uep.pillar.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final SlugService slugService;
    
    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("^#[0-9A-Fa-f]{6}$");

    @Transactional(readOnly = true)
    public Category findById(Integer id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
    }

    @Transactional(readOnly = true)
    public Optional<Category> findBySlug(String slug) {
        return categoryRepository.findBySlug(slug);
    }

    @Transactional(readOnly = true)
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    @Transactional
    public Category create(String name, String description, String color) {
        validateColor(color);
        String slug = slugService.generateUniqueSlug(name, Category.class, null);
        Category category = Category.builder()
                .name(name)
                .slug(slug)
                .description(description)
                .color(color)
                .build();
        return categoryRepository.save(category);
    }

    @Transactional
    public Category update(Integer id, String name, String description, String color) {
        Category existing = findById(id);
        if (name != null && !name.equals(existing.getName())) {
            existing.setName(name);
            String slug = slugService.generateUniqueSlug(name, Category.class, id.longValue());
            existing.setSlug(slug);
        }
        if (description != null) existing.setDescription(description);
        if (color != null) {
            validateColor(color);
            existing.setColor(color);
        }
        return categoryRepository.save(existing);
    }

    @Transactional
    public void delete(Integer id) {
        Category existing = findById(id);
        categoryRepository.delete(existing);
    }

    /**
     * Validate hex color format.
     * Expected format: #RRGGBB (e.g., #E53935)
     */
    private void validateColor(String color) {
        if (color != null && !HEX_COLOR_PATTERN.matcher(color).matches()) {
            throw new IllegalArgumentException("Invalid color format. Expected format: #RRGGBB (e.g., #E53935)");
        }
    }
}
