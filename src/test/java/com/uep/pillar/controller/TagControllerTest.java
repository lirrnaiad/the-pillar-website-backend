package com.uep.pillar.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uep.pillar.dto.CreateTagInput;
import com.uep.pillar.dto.UpdateTagInput;
import com.uep.pillar.model.Tag;
import com.uep.pillar.repository.TagRepository;
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
class TagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TagRepository tagRepository;

    private Tag testTag;

    @BeforeEach
    void setUp() {
        tagRepository.deleteAll();
        testTag = Tag.builder()
                .name("Test Tag")
                .slug("test-tag")
                .build();
        testTag = tagRepository.save(testTag);
    }

    @Test
    void testGetAllTags() throws Exception {
        mockMvc.perform(get("/api/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").exists());
    }

    @Test
    void testGetTagById() throws Exception {
        mockMvc.perform(get("/api/tags/{id}", testTag.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testTag.getId()))
                .andExpect(jsonPath("$.name").value("Test Tag"));
    }

    @Test
    void testGetTagBySlug() throws Exception {
        mockMvc.perform(get("/api/tags/slug/{slug}", testTag.getSlug()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("test-tag"));
    }

    @Test
    void testCreateTag() throws Exception {
        CreateTagInput input = CreateTagInput.builder()
                .name("New Tag")
                .slug("new-tag")
                .build();

        mockMvc.perform(post("/api/tags")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Tag"));
    }

    @Test
    void testUpdateTag() throws Exception {
        UpdateTagInput input = UpdateTagInput.builder()
                .id(testTag.getId().toString())
                .name("Updated Tag")
                .build();

        mockMvc.perform(put("/api/tags/{id}", testTag.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Tag"));
    }

    @Test
    void testDeleteTag() throws Exception {
        mockMvc.perform(delete("/api/tags/{id}", testTag.getId()))
                .andExpect(status().isNoContent());
    }
}

