-- ============================================
-- The Pillar E-Publication Website Database Schema
-- Version: 1.0.0
-- Database: PostgreSQL
-- ============================================

-- Drop tables if they exist (for clean setup)
DROP TABLE IF EXISTS audit_logs CASCADE;
DROP TABLE IF EXISTS article_revisions CASCADE;
DROP TABLE IF EXISTS article_tags CASCADE;
DROP TABLE IF EXISTS tags CASCADE;
DROP TABLE IF EXISTS articles CASCADE;
DROP TABLE IF EXISTS media CASCADE;
DROP TABLE IF EXISTS publication_issues CASCADE;
DROP TABLE IF EXISTS categories CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS roles CASCADE;

-- ============================================
-- ROLES TABLE
-- ============================================
CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    permissions JSONB DEFAULT '{}'
);

COMMENT ON TABLE roles IS 'User roles with associated permissions';
COMMENT ON COLUMN roles.permissions IS 'JSON object containing permission mappings';

-- ============================================
-- USERS TABLE
-- ============================================
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

COMMENT ON TABLE users IS 'System users including admins, editors, and writers';
COMMENT ON COLUMN users.deleted_at IS 'Soft delete timestamp - null means active';

-- ============================================
-- CATEGORIES TABLE
-- ============================================
CREATE TABLE categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    slug VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    color VARCHAR(7) -- hex color like #E53935
);

COMMENT ON TABLE categories IS 'Article categories (News, Feature, Opinion, etc.)';
COMMENT ON COLUMN categories.color IS 'Hex color code for UI display';

-- ============================================
-- PUBLICATION ISSUES TABLE
-- ============================================
CREATE TABLE publication_issues (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    slug VARCHAR(255) UNIQUE NOT NULL,
    cover_url TEXT,
    description TEXT,
    published_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW()
);

COMMENT ON TABLE publication_issues IS 'Publication volumes/issues for grouping articles';

-- ============================================
-- MEDIA TABLE
-- ============================================
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

COMMENT ON TABLE media IS 'Media files stored in Cloudinary';
COMMENT ON COLUMN media.public_id IS 'Cloudinary public ID for transformations and deletion';
COMMENT ON COLUMN media.type IS 'Media type: IMAGE, VIDEO, or DOCUMENT';

-- ============================================
-- ARTICLES TABLE
-- ============================================
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
    
    -- SEO Metadata
    meta_title VARCHAR(60),
    meta_description VARCHAR(160),
    og_image TEXT,
    
    -- Full-text Search Vector
    search_vector tsvector,
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW(),
    published_at TIMESTAMP,
    deleted_at TIMESTAMP
);

COMMENT ON TABLE articles IS 'Published articles with SEO metadata and full-text search';
COMMENT ON COLUMN articles.status IS 'Article status: DRAFT, PENDING_REVIEW, PUBLISHED, ARCHIVED, REJECTED';
COMMENT ON COLUMN articles.search_vector IS 'PostgreSQL full-text search vector';
COMMENT ON COLUMN articles.deleted_at IS 'Soft delete timestamp - null means active';

-- ============================================
-- TAGS TABLE
-- ============================================
CREATE TABLE tags (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    slug VARCHAR(100) UNIQUE NOT NULL
);

COMMENT ON TABLE tags IS 'Article tags for flexible categorization';

-- ============================================
-- ARTICLE-TAGS JUNCTION TABLE
-- ============================================
CREATE TABLE article_tags (
    article_id BIGINT REFERENCES articles(id) ON DELETE CASCADE,
    tag_id INT REFERENCES tags(id) ON DELETE CASCADE,
    PRIMARY KEY (article_id, tag_id)
);

COMMENT ON TABLE article_tags IS 'Many-to-many relationship between articles and tags';

-- ============================================
-- ARTICLE REVISIONS TABLE
-- ============================================
CREATE TABLE article_revisions (
    id BIGSERIAL PRIMARY KEY,
    article_id BIGINT REFERENCES articles(id) ON DELETE CASCADE,
    title VARCHAR(255),
    content TEXT,
    revised_by BIGINT REFERENCES users(id),
    revised_at TIMESTAMP DEFAULT NOW(),
    revision_note VARCHAR(255)
);

COMMENT ON TABLE article_revisions IS 'Article version history for tracking changes';

-- ============================================
-- AUDIT LOGS TABLE
-- ============================================
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

COMMENT ON TABLE audit_logs IS 'Audit trail for all CMS actions';
COMMENT ON COLUMN audit_logs.action IS 'Action type: CREATE, UPDATE, DELETE, PUBLISH, LOGIN, LOGOUT';
COMMENT ON COLUMN audit_logs.entity_type IS 'Entity type: ARTICLE, MEDIA, USER, CATEGORY, TAG';

-- ============================================
-- INDEXES
-- ============================================

-- Article indexes for common queries
CREATE INDEX idx_articles_slug ON articles(slug);
CREATE INDEX idx_articles_status ON articles(status);
CREATE INDEX idx_articles_author ON articles(author_id);
CREATE INDEX idx_articles_category ON articles(category_id);
CREATE INDEX idx_articles_published ON articles(published_at DESC);
CREATE INDEX idx_articles_featured ON articles(featured) WHERE featured = TRUE;
CREATE INDEX idx_articles_active ON articles(deleted_at) WHERE deleted_at IS NULL;

-- Full-text search index
CREATE INDEX idx_articles_search ON articles USING GIN(search_vector);

-- Audit log indexes
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_user ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_created ON audit_logs(created_at DESC);

-- User indexes
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_active ON users(deleted_at) WHERE deleted_at IS NULL;

-- Media indexes
CREATE INDEX idx_media_uploaded_by ON media(uploaded_by);
CREATE INDEX idx_media_type ON media(type);

-- Category and Tag indexes
CREATE INDEX idx_categories_slug ON categories(slug);
CREATE INDEX idx_tags_slug ON tags(slug);

-- ============================================
-- FULL-TEXT SEARCH TRIGGER FUNCTION
-- ============================================
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

-- ============================================
-- UPDATED_AT TRIGGER FUNCTION
-- ============================================
CREATE OR REPLACE FUNCTION update_updated_at_column() RETURNS trigger AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_users_updated_at
BEFORE UPDATE ON users
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_articles_updated_at
BEFORE UPDATE ON articles
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ============================================
-- DEFAULT DATA: ROLES
-- ============================================
INSERT INTO roles (name, permissions) VALUES
('ADMIN', '{"all": true}'),
('EDITOR', '{"articles": ["create", "read", "update", "delete", "publish"], "media": ["create", "read", "delete"], "users": ["read"]}'),
('WRITER', '{"articles": ["create", "read", "update"], "media": ["create", "read"]}');

-- ============================================
-- DEFAULT DATA: CATEGORIES
-- ============================================
INSERT INTO categories (name, slug, description, color) VALUES
('News', 'news', 'Campus and community news', '#E53935'),
('Feature', 'feature', 'In-depth feature articles', '#1E88E5'),
('Opinion', 'opinion', 'Opinion pieces and columns', '#43A047'),
('Sci-Tech', 'sci-tech', 'Science and technology content', '#8E24AA'),
('Photos', 'photos', 'Photojournalism', '#F4511E'),
('Cartoons', 'cartoons', 'Cartoons and comics', '#FDD835'),
('Videos', 'videos', 'Video content', '#00ACC1'),
('Editorial', 'editorial', 'Editorial board articles', '#6D4C41');

-- ============================================
-- OPTIONAL: CREATE DEFAULT ADMIN USER
-- Password: 'admin123' (BCrypt hashed)
-- ============================================
-- INSERT INTO users (email, password, first_name, last_name, role_id) VALUES
-- ('admin@uep.edu.ph', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKXKQRriFbPUL6nh0nOFvNQIFQ6e', 'Admin', 'User', 1);

-- ============================================
-- GRANT STATEMENTS (adjust based on your setup)
-- ============================================
-- GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO your_user;
-- GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO your_user;

-- ============================================
-- END OF SCHEMA
-- ============================================

