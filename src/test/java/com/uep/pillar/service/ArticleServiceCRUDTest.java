package com.uep.pillar.service;

import com.uep.pillar.exception.ResourceNotFoundException;
import com.uep.pillar.model.Article;
import com.uep.pillar.model.Category;
import com.uep.pillar.model.Tag;
import com.uep.pillar.model.User;
import com.uep.pillar.model.enums.ArticleStatus;
import com.uep.pillar.repository.ArticleRepository;
import com.uep.pillar.repository.CategoryRepository;
import com.uep.pillar.repository.RoleRepository;
import com.uep.pillar.repository.TagRepository;
import com.uep.pillar.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CRUD tests for ArticleService.
 * Tests create, read, update, and delete operations.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ArticleServiceCRUDTest {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testAuthor;
    private Category testCategory;
    private Tag testTag1;
    private Tag testTag2;

    @BeforeEach
    void setUp() {
        // Create test author
        testAuthor = User.builder()
                .email("author@test.com")
                .password(passwordEncoder.encode("password123"))
                .firstName("Test")
                .lastName("Author")
                .build();
        testAuthor = userRepository.save(testAuthor);

        // Create test category
        testCategory = Category.builder()
                .name("Technology")
                .slug("technology")
                .description("Tech articles")
                .color("#E53935")
                .build();
        testCategory = categoryRepository.save(testCategory);

        // Create test tags
        testTag1 = Tag.builder()
                .name("Java")
                .slug("java")
                .build();
        testTag1 = tagRepository.save(testTag1);

        testTag2 = Tag.builder()
                .name("Spring")
                .slug("spring")
                .build();
        testTag2 = tagRepository.save(testTag2);
    }

    @Test
    void testCreateArticle() {
        // CREATE
        Article article = articleService.create(
                "Test Article",
                "Article content here",
                "Short excerpt",
                testAuthor,
                testCategory.getId(),
                null,
                Set.of(testTag1.getId(), testTag2.getId()),
                null
        );

        assertNotNull(article);
        assertNotNull(article.getId());
        assertEquals("Test Article", article.getTitle());
        assertEquals("test-article", article.getSlug());
        assertEquals("Article content here", article.getContent());
        assertEquals("Short excerpt", article.getExcerpt());
        assertEquals(ArticleStatus.DRAFT, article.getStatus());
        assertEquals(testAuthor.getId(), article.getAuthor().getId());
        assertEquals(testCategory.getId(), article.getCategory().getId());
        assertEquals(2, article.getTags().size());
        assertTrue(article.getTags().contains(testTag1));
        assertTrue(article.getTags().contains(testTag2));
    }

    @Test
    void testReadArticle() {
        // CREATE first
        Article created = articleService.create(
                "Read Test Article",
                "Content",
                "Excerpt",
                testAuthor,
                null,
                null,
                null,
                null
        );

        // READ by ID
        Article found = articleService.findById(created.getId());
        assertNotNull(found);
        assertEquals(created.getId(), found.getId());
        assertEquals("Read Test Article", found.getTitle());

        // READ by slug
        Article foundBySlug = articleService.findBySlug(created.getSlug())
                .orElseThrow();
        assertEquals(created.getId(), foundBySlug.getId());
    }

    @Test
    void testReadArticleNotFound() {
        // READ non-existent article
        assertThrows(ResourceNotFoundException.class, () -> {
            articleService.findById(99999L);
        });
    }

    @Test
    void testUpdateArticle() {
        // CREATE
        Article article = articleService.create(
                "Original Title",
                "Original content",
                "Original excerpt",
                testAuthor,
                testCategory.getId(),
                null,
                Set.of(testTag1.getId()),
                null
        );

        Long articleId = article.getId();
        String originalSlug = article.getSlug();

        // UPDATE
        Article updated = articleService.update(
                articleId,
                "Updated Title",
                "Updated content",
                "Updated excerpt",
                testCategory.getId(),
                null,
                Set.of(testTag2.getId())
        );

        assertEquals(articleId, updated.getId());
        assertEquals("Updated Title", updated.getTitle());
        assertEquals("Updated content", updated.getContent());
        assertEquals("Updated excerpt", updated.getExcerpt());
        // Slug should change when title changes
        assertNotEquals(originalSlug, updated.getSlug());
        assertEquals(1, updated.getTags().size());
        assertTrue(updated.getTags().contains(testTag2));
    }

    @Test
    void testUpdateArticlePartial() {
        // CREATE
        Article article = articleService.create(
                "Partial Update Test",
                "Content",
                "Excerpt",
                testAuthor,
                null,
                null,
                null,
                null
        );

        // UPDATE only content (other fields null)
        Article updated = articleService.update(
                article.getId(),
                null,
                "New content only",
                null,
                null,
                null,
                null
        );

        assertEquals("Partial Update Test", updated.getTitle()); // Unchanged
        assertEquals("New content only", updated.getContent()); // Changed
        assertEquals("Excerpt", updated.getExcerpt()); // Unchanged
    }

    @Test
    void testDeleteArticle() {
        // CREATE
        Article article = articleService.create(
                "To Be Deleted",
                "Content",
                "Excerpt",
                testAuthor,
                null,
                null,
                null,
                null
        );

        Long articleId = article.getId();
        assertTrue(articleRepository.findById(articleId).isPresent());

        // DELETE (soft delete)
        articleService.softDelete(articleId);
        
        // Flush to ensure the soft delete is persisted
        articleRepository.flush();

        // Verify soft delete - should not be found in normal queries
        assertThrows(ResourceNotFoundException.class, () -> {
            articleService.findById(articleId);
        });
    }

    @Test
    void testListArticles() {
        // CREATE multiple articles
        articleService.create("Article 1", "Content 1", "Excerpt 1", testAuthor, null, null, null, null);
        articleService.create("Article 2", "Content 2", "Excerpt 2", testAuthor, null, null, null, null);
        articleService.create("Article 3", "Content 3", "Excerpt 3", testAuthor, null, null, null, null);

        // LIST all using repository
        List<Article> allArticles = articleRepository.findAll();
        assertTrue(allArticles.size() >= 3);
    }

    @Test
    void testPublishArticle() {
        // CREATE
        Article article = articleService.create(
                "Publish Test",
                "Content",
                "Excerpt",
                testAuthor,
                null,
                null,
                null,
                null
        );

        assertEquals(ArticleStatus.DRAFT, article.getStatus());
        assertNull(article.getPublishedAt());

        // PUBLISH
        Article published = articleService.publish(article.getId());

        assertEquals(ArticleStatus.PUBLISHED, published.getStatus());
        assertNotNull(published.getPublishedAt());
    }

    @Test
    void testSetFeatured() {
        // CREATE
        Article article = articleService.create(
                "Featured Test",
                "Content",
                "Excerpt",
                testAuthor,
                null,
                null,
                null,
                null
        );

        assertFalse(article.isFeatured());

        // SET FEATURED
        Article featured = articleService.setFeatured(article.getId(), true);

        assertTrue(featured.isFeatured());
    }
}

