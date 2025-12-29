package com.uep.pillar.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uep.pillar.dto.CreateArticleInput;
import com.uep.pillar.model.Article;
import com.uep.pillar.model.Category;
import com.uep.pillar.model.User;
import com.uep.pillar.model.enums.ArticleStatus;
import com.uep.pillar.repository.ArticleRepository;
import com.uep.pillar.repository.CategoryRepository;
import com.uep.pillar.repository.UserRepository;
import com.uep.pillar.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ArticleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private User testUser;
    private Category testCategory;
    private String authToken;

    @BeforeEach
    void setUp() {
        articleRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        testUser = User.builder()
                .email("writer@test.com")
                .password(passwordEncoder.encode("Test1234!"))
                .firstName("Test")
                .lastName("Writer")
                .build();
        testUser = userRepository.save(testUser);
        userRepository.flush(); // Ensure user is persisted before generating token

        testCategory = Category.builder()
                .name("Test Category")
                .slug("test-category")
                .build();
        testCategory = categoryRepository.save(testCategory);

        authToken = jwtTokenProvider.generateToken(testUser);
    }

    @Test
    void testGetArticles() throws Exception {
        // Create a test article
        Article article = Article.builder()
                .title("Test Article")
                .slug("test-article")
                .content("Test content")
                .status(ArticleStatus.PUBLISHED)
                .author(testUser)
                .category(testCategory)
                .featured(false)
                .viewCount(0L)
                .build();
        articleRepository.save(article);

        mockMvc.perform(get("/api/articles?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].title").exists());
    }

    @Test
    void testGetArticleById() throws Exception {
        Article article = Article.builder()
                .title("Test Article")
                .slug("test-article")
                .content("Test content")
                .status(ArticleStatus.PUBLISHED)
                .author(testUser)
                .category(testCategory)
                .featured(false)
                .viewCount(0L)
                .build();
        article = articleRepository.save(article);

        mockMvc.perform(get("/api/articles/{id}", article.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(article.getId()))
                .andExpect(jsonPath("$.title").value("Test Article"));
    }

    @Test
    void testGetArticleBySlug() throws Exception {
        Article article = Article.builder()
                .title("Test Article")
                .slug("test-article")
                .content("Test content")
                .status(ArticleStatus.PUBLISHED)
                .author(testUser)
                .category(testCategory)
                .featured(false)
                .viewCount(0L)
                .build();
        articleRepository.save(article);

        mockMvc.perform(get("/api/articles/slug/{slug}", "test-article"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("test-article"));
    }

    @Test
    void testGetFeaturedArticles() throws Exception {
        Article article = Article.builder()
                .title("Featured Article")
                .slug("featured-article")
                .content("Content")
                .status(ArticleStatus.PUBLISHED)
                .author(testUser)
                .category(testCategory)
                .featured(true)
                .viewCount(0L)
                .build();
        articleRepository.save(article);

        mockMvc.perform(get("/api/articles/featured?limit=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testCreateArticle() throws Exception {
        CreateArticleInput input = CreateArticleInput.builder()
                .title("New Article")
                .content("Article content")
                .excerpt("Article excerpt")
                .status(ArticleStatus.DRAFT)
                .categoryId(testCategory.getId().toString())
                .build();

        mockMvc.perform(post("/api/articles")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New Article"));
    }

    @Test
    void testIncrementArticleViews() throws Exception {
        Article article = Article.builder()
                .title("Test Article")
                .slug("test-article")
                .content("Content")
                .status(ArticleStatus.PUBLISHED)
                .author(testUser)
                .category(testCategory)
                .featured(false)
                .viewCount(0L)
                .build();
        article = articleRepository.save(article);
        articleRepository.flush(); // Ensure article is persisted

        // The controller should return the updated article with incremented view count
        // clearAutomatically=true in repository should ensure fresh data
        mockMvc.perform(post("/api/articles/{id}/views", article.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.viewCount").value(1));
    }
}

