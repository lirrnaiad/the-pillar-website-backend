-- ============================================
-- Seed Script for Photos, Videos, and Comics & Cartoons Articles
-- Run this after seed_articles.sql to populate the database with sample content
-- This script is idempotent - can be run multiple times safely
-- ============================================

-- ============================================
-- 1. CREATE TAGS FOR NEW SUBCATEGORIES
-- ============================================
INSERT INTO tags (name, slug) VALUES
-- Photos subcategories
('KODAK', 'kodak'),
-- Videos subcategories
('Video Report', 'video-report'),
('Interview Video', 'interview-video'),
-- Cartoons & Comics subcategories
('Cartoons', 'cartoons'),
('Comics', 'comics')
ON CONFLICT (slug) DO NOTHING;

-- ============================================
-- 2. INSERT ARTICLES FOR PHOTOS, VIDEOS, AND CARTOONS
-- ============================================

DO $$
DECLARE
    photos_cat_id INT;
    videos_cat_id INT;
    cartoons_cat_id INT;
    writer_user_id BIGINT;
    kodak_tag_id INT;
    video_report_tag_id INT;
    interview_video_tag_id INT;
    cartoons_tag_id INT;
    comics_tag_id INT;
    placeholder_media_id BIGINT;
BEGIN
    -- Get category IDs
    SELECT id INTO photos_cat_id FROM categories WHERE slug = 'photos';
    SELECT id INTO videos_cat_id FROM categories WHERE slug = 'videos';
    SELECT id INTO cartoons_cat_id FROM categories WHERE slug = 'cartoons';
    
    -- Get writer user ID
    SELECT id INTO writer_user_id FROM users WHERE email = 'writer@uep.edu.ph' LIMIT 1;
    
    -- Get tag IDs
    SELECT id INTO kodak_tag_id FROM tags WHERE slug = 'kodak';
    SELECT id INTO video_report_tag_id FROM tags WHERE slug = 'video-report';
    SELECT id INTO interview_video_tag_id FROM tags WHERE slug = 'interview-video';
    SELECT id INTO cartoons_tag_id FROM tags WHERE slug = 'cartoons';
    SELECT id INTO comics_tag_id FROM tags WHERE slug = 'comics';
    
    -- Get or create a placeholder media entry (we'll use a generic placeholder image URL)
    -- In production, you'd upload actual images to Cloudinary and use those URLs
    SELECT id INTO placeholder_media_id FROM media WHERE url LIKE '%placeholder%' LIMIT 1;
    IF placeholder_media_id IS NULL THEN
        INSERT INTO media (url, alt_text, type, uploaded_by)
        VALUES (
            'https://via.placeholder.com/1200x800/082640/dfd0b8?text=Photo+Article',
            'Placeholder image for photo article',
            'IMAGE',
            writer_user_id
        ) RETURNING id INTO placeholder_media_id;
    END IF;

    -- ============================================
    -- PHOTOS ARTICLES (3 articles)
    -- ============================================
    
    -- Photos Article 1: Featured KODAK
    INSERT INTO articles (title, slug, content, excerpt, status, featured, view_count, author_id, category_id, cover_id, published_at)
    VALUES (
        'UEPians: Sa Bisig ng Gabi',
        'uepians-sa-bisig-ng-gabi',
        '<p>Sa gitna ng ingay, sa likod ng gulo— naroon ang tanglaw ng tala, para sa taong patuloy na nakatingala.</p><p>Kapag sumapit ang dilim, hindi man tiyak ang landas na tatahakin, patuloy pa ring naglalakad ang ilan, bitbit ang liwanag ng panalangin.</p><img src="https://via.placeholder.com/1335x890/082640/dfd0b8?text=Photo+1" alt="Night scene with students" /><p>Mga kuha nina Paolo Pinca, Clarence Tuballas, Queen Surio, at Irel Torio</p><p>Verbo: Nino Balawang</p><img src="https://via.placeholder.com/839x559/082640/dfd0b8?text=Photo+2" alt="Students at night" /><img src="https://via.placeholder.com/1267x844/082640/dfd0b8?text=Photo+3" alt="Evening campus scene" /><img src="https://via.placeholder.com/611x407/082640/dfd0b8?text=Photo+4" alt="Night photography" /><img src="https://via.placeholder.com/611x407/082640/dfd0b8?text=Photo+5" alt="Campus at night" /><img src="https://via.placeholder.com/611x407/082640/dfd0b8?text=Photo+6" alt="Students walking" /><img src="https://via.placeholder.com/1252x834/082640/dfd0b8?text=Photo+7" alt="Final night scene" />',
        'A photographic journey through the night life of UEP students, capturing moments of hope and perseverance.',
        'PUBLISHED',
        true,
        245,
        writer_user_id,
        photos_cat_id,
        placeholder_media_id,
        '2025-10-15 18:00:00'
    ) ON CONFLICT (slug) DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a.id, kodak_tag_id
    FROM articles a
    WHERE a.slug = 'uepians-sa-bisig-ng-gabi'
    AND NOT EXISTS (SELECT 1 FROM article_tags WHERE article_id = a.id AND tag_id = kodak_tag_id);

    -- Photos Article 2: KODAK
    INSERT INTO articles (title, slug, content, excerpt, status, featured, view_count, author_id, category_id, cover_id, published_at)
    VALUES (
        'Campus Life Through the Lens',
        'campus-life-through-the-lens',
        '<p>Capturing the everyday moments that make university life special.</p><img src="https://via.placeholder.com/1200x800/082640/dfd0b8?text=Main+Photo" alt="Campus life" /><p>Students gather in the quadrangle during lunch break, sharing stories and laughter.</p><img src="https://via.placeholder.com/600x400/082640/dfd0b8?text=Photo+1" alt="Students in quadrangle" /><img src="https://via.placeholder.com/600x400/082640/dfd0b8?text=Photo+2" alt="Lunch break" /><img src="https://via.placeholder.com/1200x600/082640/dfd0b8?text=Photo+3" alt="Campus activities" /><img src="https://via.placeholder.com/600x400/082640/dfd0b8?text=Photo+4" alt="Student interaction" /><img src="https://via.placeholder.com/600x400/082640/dfd0b8?text=Photo+5" alt="Campus scene" /><img src="https://via.placeholder.com/1440x900/082640/dfd0b8?text=Final+Campus+View" alt="Final campus view" />',
        'A visual story of daily campus life, showcasing the vibrant community at UEP.',
        'PUBLISHED',
        false,
        189,
        writer_user_id,
        photos_cat_id,
        placeholder_media_id,
        '2025-10-18 14:00:00'
    ) ON CONFLICT (slug) DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a.id, kodak_tag_id
    FROM articles a
    WHERE a.slug = 'campus-life-through-the-lens'
    AND NOT EXISTS (SELECT 1 FROM article_tags WHERE article_id = a.id AND tag_id = kodak_tag_id);

    -- Photos Article 3: KODAK
    INSERT INTO articles (title, slug, content, excerpt, status, featured, view_count, author_id, category_id, cover_id, published_at)
    VALUES (
        'Moments of Reflection',
        'moments-of-reflection',
        '<p>In the quiet corners of campus, students find spaces for contemplation and growth.</p><img src="https://via.placeholder.com/1200x800/082640/dfd0b8?text=Reflection+Photo" alt="Student reflection" /><p>May maniningning na kasama—ang sariling katahimikan.</p><img src="https://via.placeholder.com/600x400/082640/dfd0b8?text=Quiet+Space" alt="Quiet study area" /><img src="https://via.placeholder.com/600x400/082640/dfd0b8?text=Library+Scene" alt="Library scene" /><img src="https://via.placeholder.com/1200x600/082640/dfd0b8?text=Study+Area" alt="Study area" /><img src="https://via.placeholder.com/1440x900/082640/dfd0b8?text=Peaceful+Campus" alt="Peaceful campus view" />',
        'Photographs capturing moments of solitude and reflection in campus spaces.',
        'PUBLISHED',
        false,
        156,
        writer_user_id,
        photos_cat_id,
        placeholder_media_id,
        '2025-10-20 16:00:00'
    ) ON CONFLICT (slug) DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a.id, kodak_tag_id
    FROM articles a
    WHERE a.slug = 'moments-of-reflection'
    AND NOT EXISTS (SELECT 1 FROM article_tags WHERE article_id = a.id AND tag_id = kodak_tag_id);

    -- ============================================
    -- VIDEOS ARTICLES (3 articles)
    -- ============================================
    
    -- Video Article 1: Video Report (Featured)
    INSERT INTO articles (title, slug, content, excerpt, status, featured, view_count, author_id, category_id, cover_id, published_at)
    VALUES (
        'UEP Students Share Their Journey: A Video Report',
        'uep-students-share-their-journey-video-report',
        '<p>In this comprehensive video report, UEP students share their personal journeys, challenges, and triumphs throughout their academic careers. The report features interviews with students from various colleges, highlighting their unique experiences and perspectives.</p><p>The video captures candid moments of student life, from early morning classes to late-night study sessions. Students discuss their motivations, aspirations, and the support systems that help them succeed.</p><p>This report aims to inspire incoming students and provide insights into the diverse experiences that make up the UEP community.</p>',
        'A video report featuring UEP students sharing their academic journeys and personal experiences.',
        'PUBLISHED',
        true,
        312,
        writer_user_id,
        videos_cat_id,
        placeholder_media_id,
        '2025-10-12 10:00:00'
    ) ON CONFLICT (slug) DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a.id, video_report_tag_id
    FROM articles a
    WHERE a.slug = 'uep-students-share-their-journey-video-report'
    AND NOT EXISTS (SELECT 1 FROM article_tags WHERE article_id = a.id AND tag_id = video_report_tag_id);

    -- Video Article 2: Interview Video
    INSERT INTO articles (title, slug, content, excerpt, status, featured, view_count, author_id, category_id, cover_id, published_at)
    VALUES (
        'Exclusive Interview: UEP President Discusses Future Plans',
        'exclusive-interview-uep-president-future-plans',
        '<p>In this exclusive interview, the UEP President discusses the university''s vision for the future, upcoming infrastructure projects, and plans for academic excellence. The conversation covers topics ranging from student services to research initiatives.</p><p>The president shares insights into the strategic direction of the university and how it plans to address the evolving needs of students and the community. The interview provides a rare glimpse into the leadership perspective on higher education.</p><p>This interview is part of The Pillar''s commitment to bringing transparency and open communication between the administration and the student body.</p>',
        'An exclusive interview with the UEP President about the university''s future plans and vision.',
        'PUBLISHED',
        false,
        278,
        writer_user_id,
        videos_cat_id,
        placeholder_media_id,
        '2025-10-14 15:00:00'
    ) ON CONFLICT (slug) DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a.id, interview_video_tag_id
    FROM articles a
    WHERE a.slug = 'exclusive-interview-uep-president-future-plans'
    AND NOT EXISTS (SELECT 1 FROM article_tags WHERE article_id = a.id AND tag_id = interview_video_tag_id);

    -- Video Article 3: Video Report
    INSERT INTO articles (title, slug, content, excerpt, status, featured, view_count, author_id, category_id, cover_id, published_at)
    VALUES (
        'Campus Events Roundup: October 2025',
        'campus-events-roundup-october-2025',
        '<p>This video report provides a comprehensive roundup of campus events that took place in October 2025. From cultural celebrations to academic competitions, the report highlights the vibrant campus life at UEP.</p><p>Featured events include the annual cultural festival, inter-college competitions, and various student organization activities. The video captures the energy and enthusiasm of students participating in these events.</p><p>Through interviews with event organizers and participants, the report showcases the collaborative spirit and creativity that defines the UEP community.</p>',
        'A video report covering the major campus events that took place in October 2025.',
        'PUBLISHED',
        false,
        201,
        writer_user_id,
        videos_cat_id,
        placeholder_media_id,
        '2025-10-22 11:00:00'
    ) ON CONFLICT (slug) DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a.id, video_report_tag_id
    FROM articles a
    WHERE a.slug = 'campus-events-roundup-october-2025'
    AND NOT EXISTS (SELECT 1 FROM article_tags WHERE article_id = a.id AND tag_id = video_report_tag_id);

    -- ============================================
    -- CARTOONS & COMICS ARTICLES (4 articles)
    -- ============================================
    
    -- Cartoon Article 1: Cartoons (Featured)
    INSERT INTO articles (title, slug, content, excerpt, status, featured, view_count, author_id, category_id, cover_id, published_at)
    VALUES (
        'KORAPTOBER: ''TAX''',
        'koraptober-tax',
        '<p>Mula sa bahang paulit-ulit na nilulubog ang bayan, hanggang sa galit ng taumbayang sawang-sawa na sa panlilinlang. Mula sa bigat ng buwis na pasan ng karaniwang mamamayan, hanggang sa kintab ng mga luxury car na walang habas kung iparada at ipagyabang.</p>',
        'A political cartoon depicting the burden of taxation on ordinary citizens.',
        'PUBLISHED',
        true,
        423,
        writer_user_id,
        cartoons_cat_id,
        placeholder_media_id,
        '2025-10-10 12:00:00'
    ) ON CONFLICT (slug) DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a.id, cartoons_tag_id
    FROM articles a
    WHERE a.slug = 'koraptober-tax'
    AND NOT EXISTS (SELECT 1 FROM article_tags WHERE article_id = a.id AND tag_id = cartoons_tag_id);

    -- Cartoon Article 2: Cartoons
    INSERT INTO articles (title, slug, content, excerpt, status, featured, view_count, author_id, category_id, cover_id, published_at)
    VALUES (
        'Oktubre Singko',
        'oktubre-singko',
        '<p>A satirical cartoon reflecting on current events and social issues affecting the community.</p>',
        'A satirical cartoon offering commentary on contemporary social issues.',
        'PUBLISHED',
        false,
        267,
        writer_user_id,
        cartoons_cat_id,
        placeholder_media_id,
        '2025-10-05 10:00:00'
    ) ON CONFLICT (slug) DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a.id, cartoons_tag_id
    FROM articles a
    WHERE a.slug = 'oktubre-singko'
    AND NOT EXISTS (SELECT 1 FROM article_tags WHERE article_id = a.id AND tag_id = cartoons_tag_id);

    -- Comic Article 1: Comics
    INSERT INTO articles (title, slug, content, excerpt, status, featured, view_count, author_id, category_id, cover_id, published_at)
    VALUES (
        'What I am, After All',
        'what-i-am-after-all',
        '<p>A thought-provoking comic strip exploring themes of identity and self-discovery through the lens of student life.</p>',
        'A comic strip exploring themes of identity and self-discovery in student life.',
        'PUBLISHED',
        false,
        198,
        writer_user_id,
        cartoons_cat_id,
        placeholder_media_id,
        '2025-10-08 14:00:00'
    ) ON CONFLICT (slug) DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a.id, comics_tag_id
    FROM articles a
    WHERE a.slug = 'what-i-am-after-all'
    AND NOT EXISTS (SELECT 1 FROM article_tags WHERE article_id = a.id AND tag_id = comics_tag_id);

    -- Comic Article 2: Comics
    INSERT INTO articles (title, slug, content, excerpt, status, featured, view_count, author_id, category_id, cover_id, published_at)
    VALUES (
        'Campus Chronicles: A Day in the Life',
        'campus-chronicles-day-in-the-life',
        '<p>A lighthearted comic strip following a day in the life of a typical UEP student, from morning classes to evening activities.</p>',
        'A comic strip depicting a day in the life of a UEP student with humor and relatability.',
        'PUBLISHED',
        false,
        234,
        writer_user_id,
        cartoons_cat_id,
        placeholder_media_id,
        '2025-10-16 16:00:00'
    ) ON CONFLICT (slug) DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a.id, comics_tag_id
    FROM articles a
    WHERE a.slug = 'campus-chronicles-day-in-the-life'
    AND NOT EXISTS (SELECT 1 FROM article_tags WHERE article_id = a.id AND tag_id = comics_tag_id);

END $$;

-- ============================================
-- END OF SEED SCRIPT
-- ============================================

