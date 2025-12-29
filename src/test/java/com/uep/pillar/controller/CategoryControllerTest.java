package com.uep.pillar.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uep.pillar.dto.CreateCategoryInput;
import com.uep.pillar.dto.UpdateCategoryInput;
import com.uep.pillar.model.Category;
import com.uep.pillar.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category testCategory;

    @BeforeEach
    void setUp() {
        categoryRepository.deleteAll();
        testCategory = Category.builder()
                .name("Test Category")
                .slug("test-category")
                .description("Test description")
                .color("#E53935")
                .build();
        testCategory = categoryRepository.save(testCategory);
    }

    @Test
    void testGetAllCategories() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").exists());
    }

    @Test
    void testGetCategoryById() throws Exception {
        mockMvc.perform(get("/api/categories/{id}", testCategory.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testCategory.getId()))
                .andExpect(jsonPath("$.name").value("Test Category"));
    }

    @Test
    void testGetCategoryBySlug() throws Exception {
        mockMvc.perform(get("/api/categories/slug/{slug}", testCategory.getSlug()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("test-category"));
    }

    @Test
    void testCreateCategory() throws Exception {
        CreateCategoryInput input = CreateCategoryInput.builder()
                .name("New Category")
                .slug("new-category")
                .description("New description")
                .color("#FF0000")
                .build();

        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Category"));
    }

    @Test
    void testUpdateCategory() throws Exception {
        UpdateCategoryInput input = UpdateCategoryInput.builder()
                .id(testCategory.getId().toString())
                .name("Updated Category")
                .description("Updated description")
                .build();

        mockMvc.perform(put("/api/categories/{id}", testCategory.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Category"));
    }

    @Test
    void testDeleteCategory() throws Exception {
        mockMvc.perform(delete("/api/categories/{id}", testCategory.getId()))
                .andExpect(status().isNoContent());
    }
}

