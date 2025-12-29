# The Pillar Backend - Architecture Guide

> **Version:** 1.0.0  
> **Last Updated:** December 2025  
> **Authors:** The Pillar Development Team

---

## Table of Contents

1. [Overview](#overview)
2. [Reference Publications](#reference-publications)
3. [Core Architecture Improvements](#core-architecture-improvements)
4. [Project Structure](#project-structure)
5. [Database Schema](#database-schema)
6. [Implementation Priority](#implementation-priority)
7. [Dependencies](#dependencies)
8. [Configuration](#configuration)

---

## Overview

This document outlines the architectural decisions and best practices for The Pillar E-Publication Website backend. The architecture is designed for scalability, maintainability, and follows industry standards used by major university publications.

### Tech Stack

| Layer | Technology |
|-------|------------|
| Framework | Spring Boot 3.2.0 |
| Language | Java 17 |
| API | REST |
| Database | PostgreSQL |
| Cache | Redis |
| Object Storage | Cloudinary / AWS S3 |
| Build Tool | Maven |
| Security | Spring Security + JWT |

---

## Reference Publications

### Philippine University Publications

| Publication | University | Website |
|-------------|------------|---------|
| The Varsitarian | UST | varsitarian.net |
| Philippine Collegian | UP | phkule.org |
| The LaSallian | DLSU | thelasallian.com |
| The GUIDON | ADMU | theguidon.com |

### Global Publication Platforms

- Medium
- Substack
- The Guardian
- The New York Times
- Dev.to

---

## Core Architecture Improvements

### 1. Cloud Object Storage

**Problem:** Storing images in the project bloats the repository, slows deployments, and doesn't scale.

**Solution:** Use cloud object storage (Cloudinary, AWS S3, DigitalOcean Spaces).

#### Architecture Diagram

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   Frontend  │────▶│   Backend   │────▶│  Cloudinary │
│   (React)   │     │ (Spring Boot)│     │   / S3      │
└─────────────┘     └──────┬──────┘     └──────┬──────┘
                          │                    │
                          ▼                    │
                   ┌─────────────┐             │
                   │  PostgreSQL │◀────────────┘
                   │  (URL refs) │   (stores URL, not file)
                   └─────────────┘
```

#### Recommended Services (Student Budget)

| Service | Free Tier | Best For |
|---------|-----------|----------|
| **Cloudinary** | 25GB storage, 25GB bandwidth/month | Images with transformations |
| **AWS S3** | 5GB (12 months) | Production-grade |
| **Supabase Storage** | 1GB free | If using Supabase |
| **DigitalOcean Spaces** | $5/month, 250GB | GitHub Student Pack credit |
| **Uploadcare** | 3GB free | Simple uploads |

#### Implementation Notes

- Store only the URL/path in the database, never the file itself
- Use Cloudinary's transformation URLs for responsive images
- Implement signed URLs for private/admin-only media

---

### 2. Article Slugs (SEO-Friendly URLs)

**Problem:** URLs like `/article/123` are not SEO-friendly and not human-readable.

**Solution:** Generate unique slugs from article titles.

#### Examples

```
Instead of:  /article/123
Use:         /article/uep-launches-new-research-center-2025
```

#### Database Schema

```sql
ALTER TABLE articles ADD COLUMN slug VARCHAR(255) UNIQUE NOT NULL;
CREATE INDEX idx_articles_slug ON articles(slug);
```

#### Slug Generation Rules

1. Convert to lowercase
2. Replace spaces with hyphens
3. Remove special characters
4. Append number if duplicate (e.g., `my-article-2`)
5. Limit to 100 characters

---

### 3. Caching Layer (Redis)

**Problem:** Database queries for every request don't scale.

**Solution:** Cache frequently accessed content in Redis.

#### Cache Strategy

```
┌─────────┐     ┌─────────┐     ┌────────────┐
│ Request │────▶│  Redis  │────▶│ PostgreSQL │
└─────────┘     │ (Cache) │     │ (if miss)  │
                └─────────┘     └────────────┘
```

#### What to Cache

| Content | TTL (Time to Live) |
|---------|-------------------|
| Homepage articles | 5 minutes |
| Category listings | 5 minutes |
| Individual articles | 1 hour |
| Author profiles | 1 day |
| Static content | 1 day |

---

### 4. Content Delivery Network (CDN)

**Problem:** Slow image loading, especially for users far from the server.

**Solution:** Serve media through a CDN.

#### Recommended CDN Options

- **Cloudflare** (Free tier available)
- **Cloudinary** (Built-in CDN)
- **BunnyCDN** (Budget-friendly)

---

### 5. Full-Text Search

**Problem:** Database `LIKE` queries are slow and don't handle relevance ranking.

**Solution:** Implement proper full-text search.

#### Options

| Solution | Complexity | Cost | Recommendation |
|----------|------------|------|----------------|
| PostgreSQL Full-Text | Low | Free | Start here |
| Meilisearch | Medium | Free (self-host) | Scale to this |
| Elasticsearch | High | $$$ | Enterprise |
| Algolia | Medium | Free tier | If budget allows |

#### PostgreSQL Full-Text Setup

```sql
-- Add search vector column
ALTER TABLE articles ADD COLUMN search_vector tsvector;

-- Create index
CREATE INDEX idx_articles_search ON articles USING GIN(search_vector);

-- Update trigger
CREATE TRIGGER articles_search_update
BEFORE INSERT OR UPDATE ON articles
FOR EACH ROW EXECUTE FUNCTION
tsvector_update_trigger(search_vector, 'pg_catalog.english', title, content);
```

---

### 6. Article Workflow & Status

**Problem:** No clear workflow from draft to publication.

**Solution:** Implement article status workflow.

#### Status Flow

```
DRAFT → PENDING_REVIEW → PUBLISHED → ARCHIVED
              ↓
          REJECTED (with feedback)
```

#### Status Enum

```java
public enum ArticleStatus {
    DRAFT,           // Initial state, only author can see
    PENDING_REVIEW,  // Submitted for editor review
    PUBLISHED,       // Live on the website
    ARCHIVED,        // Hidden but preserved
    REJECTED         // Returned with feedback
}
```

---

### 7. Article Versioning

**Problem:** No way to track changes or revert mistakes.

**Solution:** Store revision history.

#### Database Schema

```sql
CREATE TABLE article_revisions (
    id BIGSERIAL PRIMARY KEY,
    article_id BIGINT REFERENCES articles(id),
    title VARCHAR(255),
    content TEXT,
    revised_by BIGINT REFERENCES users(id),
    revised_at TIMESTAMP DEFAULT NOW(),
    revision_note VARCHAR(255)
);
```

---

### 8. Soft Deletes

**Problem:** Permanent deletion loses data and creates legal/audit issues.

**Solution:** Never actually delete; mark as deleted.

#### Implementation

```java
@Entity
public class Article {
    // ... other fields
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;  // null = not deleted
    
    public boolean isDeleted() {
        return deletedAt != null;
    }
}
```

#### Repository Query

```java
@Query("SELECT a FROM Article a WHERE a.deletedAt IS NULL")
List<Article> findAllActive();
```

---

### 9. Audit Trail

**Problem:** No accountability for CMS actions.

**Solution:** Log all content changes.

#### Database Schema

```sql
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    action VARCHAR(50),      -- CREATE, UPDATE, DELETE, PUBLISH
    entity_type VARCHAR(50), -- ARTICLE, MEDIA, USER
    entity_id BIGINT,
    old_value JSONB,
    new_value JSONB,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_user ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_created ON audit_logs(created_at);
```

---

### 10. SEO & Social Sharing

**Problem:** Poor visibility on search engines and social media.

**Solution:** Add SEO metadata to articles.

#### Article SEO Fields

```java
// SEO fields
private String metaTitle;        // Custom SEO title (60 chars max)
private String metaDescription;  // Meta description (155-160 chars)
private String canonicalUrl;     // Canonical URL if cross-posted

// Open Graph (Facebook, LinkedIn)
private String ogTitle;
private String ogDescription;
private String ogImage;          // 1200x630 recommended

// Twitter Card
private String twitterCard;      // summary_large_image
private String twitterTitle;
private String twitterImage;     // 1200x600 recommended
```

---

### 11. RSS Feeds

**Problem:** No way for readers to subscribe to updates.

**Solution:** Generate RSS feeds.

#### Feed Endpoints

| Endpoint | Description |
|----------|-------------|
| `/rss/all` | All published articles |
| `/rss/news` | News category only |
| `/rss/opinion` | Opinion category only |
| `/rss/author/{id}` | Articles by specific author |

---

## Project Structure

```
the-pillar-website-backend/
├── src/main/java/com/uep/pillar/
│   ├── ThePillarApplication.java
│   │
│   ├── config/
│   │   ├── SecurityConfig.java
│   │   ├── CorsConfig.java
│   │   ├── CacheConfig.java           # Redis configuration
│   │   └── CloudStorageConfig.java    # Cloudinary/S3 config
│   │
│   ├── model/
│   │   ├── Article.java
│   │   ├── User.java
│   │   ├── Role.java
│   │   ├── Category.java
│   │   ├── Tag.java
│   │   ├── Media.java
│   │   ├── PublicationIssue.java
│   │   ├── ArticleRevision.java       # Version history
│   │   └── AuditLog.java              # Audit trail
│   │
│   ├── model/enums/
│   │   ├── ArticleStatus.java         # DRAFT, PUBLISHED, etc.
│   │   ├── MediaType.java             # IMAGE, VIDEO, DOCUMENT
│   │   └── AuditAction.java           # CREATE, UPDATE, DELETE
│   │
│   ├── repository/
│   │   ├── ArticleRepository.java
│   │   ├── UserRepository.java
│   │   ├── CategoryRepository.java
│   │   ├── TagRepository.java
│   │   ├── MediaRepository.java
│   │   ├── ArticleRevisionRepository.java
│   │   └── AuditLogRepository.java
│   │
│   ├── service/
│   │   ├── ArticleService.java
│   │   ├── UserService.java
│   │   ├── MediaService.java
│   │   ├── AuthService.java
│   │   ├── CloudStorageService.java   # Upload handling
│   │   ├── SlugService.java           # Slug generation
│   │   ├── CacheService.java          # Cache management
│   │   ├── SearchService.java         # Full-text search
│   │   ├── RssFeedService.java        # RSS generation
│   │   └── AuditService.java          # Audit logging
│   │
│   ├── controller/
│   │   ├── AuthController.java
│   │   ├── ArticleController.java
│   │   ├── UserController.java
│   │   ├── CategoryController.java
│   │   ├── TagController.java
│   │   ├── MediaUploadController.java
│   │   └── ... (other REST controllers)
│   │
│   ├── dto/
│   │   ├── ArticleDTO.java
│   │   ├── ArticleCreateInput.java
│   │   ├── ArticleUpdateInput.java
│   │   ├── UserDTO.java
│   │   ├── MediaUploadResponse.java
│   │   └── SearchResult.java
│   │
│   ├── security/
│   │   ├── JwtTokenProvider.java
│   │   ├── JwtAuthFilter.java
│   │   └── UserDetailsServiceImpl.java
│   │
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java
│   │   ├── ArticleNotFoundException.java
│   │   ├── SlugAlreadyExistsException.java
│   │   ├── UnauthorizedException.java
│   │   └── StorageException.java
│   │
│   └── util/
│       ├── SlugGenerator.java
│       ├── DateUtils.java
│       └── ValidationUtils.java
│
└── src/main/resources/
    ├── application.properties
    ├── application-dev.properties
    └── application-prod.properties
```

---

## Database Schema

### Entity Relationship Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                         DATABASE SCHEMA                         │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌──────────┐       ┌──────────────┐       ┌──────────────┐    │
│  │  roles   │       │    users     │       │   articles   │    │
│  ├──────────┤       ├──────────────┤       ├──────────────┤    │
│  │ id       │◀──────│ role_id      │──────▶│ author_id    │    │
│  │ name     │       │ id           │       │ id           │    │
│  │ perms    │       │ email        │       │ title        │    │
│  └──────────┘       │ password     │       │ slug         │    │
│                     │ first_name   │       │ content      │    │
│                     │ last_name    │       │ excerpt      │    │
│                     │ avatar_url   │       │ status       │    │
│                     │ bio          │       │ featured     │    │
│                     │ created_at   │       │ view_count   │    │
│                     │ updated_at   │       │ category_id  │    │
│                     │ deleted_at   │       │ cover_id     │    │
│                     └──────────────┘       │ issue_id     │    │
│                                            │ meta_*       │    │
│  ┌──────────────┐                         │ created_at   │    │
│  │    media     │                         │ updated_at   │    │
│  ├──────────────┤                         │ published_at │    │
│  │ id           │◀────────────────────────│ deleted_at   │    │
│  │ url          │                         └──────────────┘    │
│  │ public_id    │                               │             │
│  │ alt_text     │                               │             │
│  │ type         │       ┌───────────────────────┘             │
│  │ size_bytes   │       │                                     │
│  │ width        │       ▼                                     │
│  │ height       │  ┌──────────────────┐                      │
│  │ uploaded_by  │  │ article_revisions│                      │
│  │ created_at   │  ├──────────────────┤                      │
│  └──────────────┘  │ id               │                      │
│                    │ article_id       │                      │
│  ┌──────────────┐  │ title            │                      │
│  │  categories  │  │ content          │                      │
│  ├──────────────┤  │ revised_by       │                      │
│  │ id           │  │ revised_at       │                      │
│  │ name         │  │ revision_note    │                      │
│  │ slug         │  └──────────────────┘                      │
│  │ description  │                                            │
│  │ color        │  ┌──────────────────┐                      │
│  └──────────────┘  │   audit_logs     │                      │
│                    ├──────────────────┤                      │
│  ┌──────────────┐  │ id               │                      │
│  │     tags     │  │ user_id          │                      │
│  ├──────────────┤  │ action           │                      │
│  │ id           │  │ entity_type      │                      │
│  │ name         │  │ entity_id        │                      │
│  │ slug         │  │ old_value        │                      │
│  └──────────────┘  │ new_value        │                      │
│        │           │ ip_address       │                      │
│        │           │ created_at       │                      │
│        ▼           └──────────────────┘                      │
│  ┌──────────────┐                                            │
│  │ article_tags │  (junction table)                          │
│  ├──────────────┤                                            │
│  │ article_id   │                                            │
│  │ tag_id       │                                            │
│  └──────────────┘                                            │
│                                                              │
│  ┌──────────────────┐                                        │
│  │ publication_issues│                                       │
│  ├──────────────────┤                                        │
│  │ id               │                                        │
│  │ title            │                                        │
│  │ slug             │                                        │
│  │ cover_url        │                                        │
│  │ published_at     │                                        │
│  │ description      │                                        │
│  └──────────────────┘                                        │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

### SQL Schema

```sql
-- Roles
CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    permissions JSONB DEFAULT '{}'
);

-- Users
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    avatar_url TEXT,
    bio TEXT,
    role_id INT REFERENCES roles(id),
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    deleted_at TIMESTAMP
);

-- Categories
CREATE TABLE categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    slug VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    color VARCHAR(7) -- hex color
);

-- Publication Issues
CREATE TABLE publication_issues (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    slug VARCHAR(255) UNIQUE NOT NULL,
    cover_url TEXT,
    description TEXT,
    published_at TIMESTAMP
);

-- Media
CREATE TABLE media (
    id BIGSERIAL PRIMARY KEY,
    url TEXT NOT NULL,
    public_id VARCHAR(255), -- Cloudinary public ID
    alt_text VARCHAR(255),
    type VARCHAR(50) NOT NULL, -- IMAGE, VIDEO, DOCUMENT
    size_bytes BIGINT,
    width INT,
    height INT,
    uploaded_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT NOW()
);

-- Articles
CREATE TABLE articles (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    slug VARCHAR(255) UNIQUE NOT NULL,
    content TEXT NOT NULL,
    excerpt TEXT,
    status VARCHAR(50) DEFAULT 'DRAFT',
    featured BOOLEAN DEFAULT FALSE,
    view_count BIGINT DEFAULT 0,
    
    -- Relations
    author_id BIGINT NOT NULL REFERENCES users(id),
    category_id INT REFERENCES categories(id),
    cover_id BIGINT REFERENCES media(id),
    issue_id BIGINT REFERENCES publication_issues(id),
    
    -- SEO
    meta_title VARCHAR(60),
    meta_description VARCHAR(160),
    og_image TEXT,
    
    -- Search
    search_vector tsvector,
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    published_at TIMESTAMP,
    deleted_at TIMESTAMP
);

-- Tags
CREATE TABLE tags (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    slug VARCHAR(100) UNIQUE NOT NULL
);

-- Article-Tags Junction
CREATE TABLE article_tags (
    article_id BIGINT REFERENCES articles(id) ON DELETE CASCADE,
    tag_id INT REFERENCES tags(id) ON DELETE CASCADE,
    PRIMARY KEY (article_id, tag_id)
);

-- Article Revisions
CREATE TABLE article_revisions (
    id BIGSERIAL PRIMARY KEY,
    article_id BIGINT REFERENCES articles(id) ON DELETE CASCADE,
    title VARCHAR(255),
    content TEXT,
    revised_by BIGINT REFERENCES users(id),
    revised_at TIMESTAMP DEFAULT NOW(),
    revision_note VARCHAR(255)
);

-- Audit Logs
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    action VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT,
    old_value JSONB,
    new_value JSONB,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);

-- Indexes
CREATE INDEX idx_articles_slug ON articles(slug);
CREATE INDEX idx_articles_status ON articles(status);
CREATE INDEX idx_articles_author ON articles(author_id);
CREATE INDEX idx_articles_category ON articles(category_id);
CREATE INDEX idx_articles_published ON articles(published_at DESC);
CREATE INDEX idx_articles_search ON articles USING GIN(search_vector);
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_user ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_created ON audit_logs(created_at DESC);

-- Full-text search trigger
CREATE OR REPLACE FUNCTION articles_search_update() RETURNS trigger AS $$
BEGIN
    NEW.search_vector := 
        setweight(to_tsvector('english', COALESCE(NEW.title, '')), 'A') ||
        setweight(to_tsvector('english', COALESCE(NEW.excerpt, '')), 'B') ||
        setweight(to_tsvector('english', COALESCE(NEW.content, '')), 'C');
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

CREATE TRIGGER articles_search_trigger
BEFORE INSERT OR UPDATE ON articles
FOR EACH ROW EXECUTE FUNCTION articles_search_update();

-- Insert default categories
INSERT INTO categories (name, slug, description, color) VALUES
('News', 'news', 'Campus and community news', '#E53935'),
('Feature', 'feature', 'In-depth feature articles', '#1E88E5'),
('Opinion', 'opinion', 'Opinion pieces and columns', '#43A047'),
('Sci-Tech', 'sci-tech', 'Science and technology content', '#8E24AA'),
('Photos', 'photos', 'Photojournalism', '#F4511E'),
('Cartoons', 'cartoons', 'Cartoons and comics', '#FDD835'),
('Videos', 'videos', 'Video content', '#00ACC1'),
('Editorial', 'editorial', 'Editorial board articles', '#6D4C41');

-- Insert default roles
INSERT INTO roles (name, permissions) VALUES
('ADMIN', '{"all": true}'),
('EDITOR', '{"articles": ["create", "read", "update", "delete", "publish"], "media": ["create", "read", "delete"]}'),
('WRITER', '{"articles": ["create", "read", "update"], "media": ["create", "read"]}');
```

---

## Implementation Priority

| Priority | Feature | Effort | Impact | Status |
|----------|---------|--------|--------|--------|
| 🔴 P0 | Cloud Storage (Cloudinary) | Medium | High | ⬜ |
| 🔴 P0 | Article Slugs | Low | High | ⬜ |
| 🔴 P0 | Article Status Workflow | Medium | High | ⬜ |
| 🟠 P1 | Soft Deletes | Low | Medium | ⬜ |
| 🟠 P1 | Basic Audit Logging | Medium | Medium | ⬜ |
| 🟠 P1 | JWT Authentication | Medium | High | ⬜ |
| 🟡 P2 | Redis Caching | Medium | High | ⬜ |
| 🟡 P2 | Full-Text Search | Medium | Medium | ⬜ |
| 🟡 P2 | SEO Metadata | Low | Medium | ⬜ |
| 🟢 P3 | Article Versioning | High | Low | ⬜ |
| 🟢 P3 | RSS Feeds | Low | Low | ⬜ |
| 🟢 P3 | CDN Integration | Medium | Medium | ⬜ |

---

## Dependencies

Add these to `pom.xml`:

```xml
<!-- Cloud Storage: Cloudinary -->
<dependency>
    <groupId>com.cloudinary</groupId>
    <artifactId>cloudinary-http44</artifactId>
    <version>1.36.0</version>
</dependency>

<!-- Caching: Redis -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

<!-- Slug Generation -->
<dependency>
    <groupId>com.github.slugify</groupId>
    <artifactId>slugify</artifactId>
    <version>3.0.6</version>
</dependency>

<!-- RSS Feed Generation -->
<dependency>
    <groupId>com.rometools</groupId>
    <artifactId>rome</artifactId>
    <version>2.1.0</version>
</dependency>

<!-- JSON Processing (for audit logs) -->
<dependency>
    <groupId>com.vladmihalcea</groupId>
    <artifactId>hibernate-types-60</artifactId>
    <version>2.21.1</version>
</dependency>
```

---

## Configuration

### Cloudinary (`application.properties`)

```properties
# Cloudinary Configuration
cloudinary.cloud-name=${CLOUDINARY_CLOUD_NAME}
cloudinary.api-key=${CLOUDINARY_API_KEY}
cloudinary.api-secret=${CLOUDINARY_API_SECRET}
cloudinary.folder=pillar-uploads
```

### Redis (`application.properties`)

```properties
# Redis Configuration
spring.cache.type=redis
spring.data.redis.host=${REDIS_HOST:localhost}
spring.data.redis.port=${REDIS_PORT:6379}
spring.data.redis.password=${REDIS_PASSWORD:}
spring.cache.redis.time-to-live=300000
```

### Environment Variables (Production)

| Variable | Description |
|----------|-------------|
| `DATABASE_URL` | PostgreSQL connection URL |
| `DB_USERNAME` | Database username |
| `DB_PASSWORD` | Database password |
| `JWT_SECRET` | Secret key for JWT tokens |
| `CLOUDINARY_CLOUD_NAME` | Cloudinary cloud name |
| `CLOUDINARY_API_KEY` | Cloudinary API key |
| `CLOUDINARY_API_SECRET` | Cloudinary API secret |
| `REDIS_HOST` | Redis server host |
| `REDIS_PORT` | Redis server port |
| `REDIS_PASSWORD` | Redis password (if any) |

---

## Best Practices Checklist

### API Design
- [ ] Use cursor-based pagination for lists
- [ ] Implement rate limiting
- [ ] Return consistent error responses
- [ ] Use proper HTTP status codes

### Security
- [ ] Validate all inputs
- [ ] Sanitize HTML content (prevent XSS)
- [ ] Use parameterized queries (prevent SQL injection)
- [ ] Implement CORS properly
- [ ] Hash passwords with BCrypt
- [ ] Use HTTPS in production

### Performance
- [ ] Index frequently queried columns
- [ ] Use eager/lazy loading appropriately
- [ ] Implement caching for read-heavy operations
- [ ] Optimize images before upload
- [ ] Use connection pooling

### Code Quality
- [ ] Write unit tests for services
- [x] Write integration tests for REST controllers
- [ ] Document public APIs
- [ ] Use consistent naming conventions
- [ ] Handle exceptions gracefully

---

## References

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Boot REST API Documentation](https://spring.io/guides/gs/rest-service/)
- [Cloudinary Java SDK](https://cloudinary.com/documentation/java_integration)
- [PostgreSQL Full-Text Search](https://www.postgresql.org/docs/current/textsearch.html)
- [Redis Spring Data](https://docs.spring.io/spring-data/redis/reference/html/)

---

*This document should be updated as the architecture evolves.*

