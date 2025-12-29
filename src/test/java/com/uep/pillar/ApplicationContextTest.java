package com.uep.pillar;

import com.uep.pillar.model.Article;
import com.uep.pillar.model.enums.ArticleStatus;
import com.uep.pillar.model.Category;
import com.uep.pillar.model.Tag;
import com.uep.pillar.model.User;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests to verify compilation and basic functionality works.
 * This test verifies:
 * - Lombok annotations (getters, setters, builders) are working
 * - Basic entity creation and manipulation
 * - Entity initialization
 */
class ApplicationContextTest {


    @Test
    void lombokGettersAndSettersWork() {
        // Test that Lombok @Getter and @Setter annotations work
        User user = new User();
        user.setEmail("test@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        
        assertEquals("test@example.com", user.getEmail());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("John Doe", user.getFullName());
    }

    @Test
    void lombokBuilderWorks() {
        // Test that Lombok @Builder annotation works
        Tag tag = Tag.builder()
                .name("Technology")
                .slug("technology")
                .build();
        
        assertNotNull(tag);
        assertEquals("Technology", tag.getName());
        assertEquals("technology", tag.getSlug());
    }

    @Test
    void articleEntityWorks() {
        // Test Article entity with Lombok annotations
        Article article = Article.builder()
                .title("Test Article")
                .slug("test-article")
                .content("Test content")
                .status(ArticleStatus.DRAFT)
                .featured(false)
                .viewCount(0L)
                .tags(new HashSet<>())
                .build();
        
        assertNotNull(article);
        assertEquals("Test Article", article.getTitle());
        assertEquals("test-article", article.getSlug());
        assertEquals(ArticleStatus.DRAFT, article.getStatus());
        assertFalse(article.isFeatured());
        assertEquals(0L, article.getViewCount());
        assertNotNull(article.getTags());
        assertFalse(article.isPublished());
    }

    @Test
    void articleTagsInitialization() {
        // Test that Article tags are properly initialized (instance initializer)
        Article article = new Article();
        assertNotNull(article.getTags(), "Tags should be initialized to empty HashSet");
        assertTrue(article.getTags().isEmpty(), "Tags should be empty initially");
    }

    @Test
    void categoryEntityWorks() {
        // Test Category entity
        Category category = Category.builder()
                .name("News")
                .slug("news")
                .description("News category")
                .color("#E53935")
                .build();
        
        assertNotNull(category);
        assertEquals("News", category.getName());
        assertEquals("news", category.getSlug());
        assertEquals("#E53935", category.getColor());
    }


    @Test
    void lombokDataAnnotationWorks() {
        // Test that @Data annotation works (generates equals, hashCode, toString)
        User user1 = User.builder()
                .id(1L)
                .email("test@example.com")
                .build();
        
        User user2 = User.builder()
                .id(1L)
                .email("test@example.com")
                .build();
        
        // @Data should generate equals() based on id
        assertEquals(user1, user2);
        assertEquals(user1.hashCode(), user2.hashCode());
        
        // toString should work (excludes password for security)
        String toString = user1.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("User"));
        assertFalse(toString.contains("password"), "toString should not expose password");
    }
}

