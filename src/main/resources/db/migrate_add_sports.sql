-- ============================================
-- Migration Script: Add Sports Category and Articles
-- Run this on an existing database to add Sports category and articles
-- Safe to run multiple times (idempotent)
-- ============================================

-- ============================================
-- 1. ADD SPORTS CATEGORY
-- ============================================
INSERT INTO categories (name, slug, description, color)
VALUES ('Sports', 'sports', 'Campus sports and athletics', '#f39c12')
ON CONFLICT (slug) DO NOTHING;

-- ============================================
-- 2. ADD SPORTS TAGS
-- ============================================
INSERT INTO tags (name, slug) VALUES
('Campus Sports', 'campus-sports'),
('Intercollegiate', 'intercollegiate')
ON CONFLICT (slug) DO NOTHING;

-- ============================================
-- 3. ADD SPORTS ARTICLES
-- ============================================
DO $$
DECLARE
    sports_cat_id INT;
    writer_user_id BIGINT;
    campus_sports_tag_id INT;
    intercollegiate_tag_id INT;
BEGIN
    -- Get Sports category ID
    SELECT id INTO sports_cat_id FROM categories WHERE slug = 'sports';
    
    -- Get writer user ID (should already exist from seed_articles.sql)
    SELECT id INTO writer_user_id FROM users WHERE email = 'writer@uep.edu.ph' LIMIT 1;
    
    -- If writer doesn't exist, create one (fallback)
    IF writer_user_id IS NULL THEN
        INSERT INTO users (email, password, first_name, last_name, role_id)
        VALUES ('writer@uep.edu.ph', '$2a$10$rKqHXzKXJxKXJxKXJxKXeOQrKqHXzKXJxKXJxKXJxKXJxKXJxKXJxKXJxK', 'John', 'Doe', 3)
        RETURNING id INTO writer_user_id;
    END IF;
    
    -- Get tag IDs
    SELECT id INTO campus_sports_tag_id FROM tags WHERE slug = 'campus-sports';
    SELECT id INTO intercollegiate_tag_id FROM tags WHERE slug = 'intercollegiate';

    -- ============================================
    -- SPORTS ARTICLES (10 articles)
    -- ============================================

    -- Sports Article 1: Featured
    INSERT INTO articles (title, slug, content, excerpt, status, featured, view_count, author_id, category_id, published_at)
    VALUES (
        'UEP Sports Director, officials tackle scheduling issues in UEPAA Games ''25 prep meeting',
        'uep-sports-director-officials-tackle-scheduling-issues-in-uepaa-games-25-prep-meeting',
        '<p>The UEP Sports Director and key officials convened for a preparatory meeting to address scheduling challenges for the upcoming UEPAA Games 2025. The meeting focused on coordinating events, managing venue availability, and ensuring smooth execution of the multi-day athletic competition.</p><p>Officials discussed the competition schedule, venue assignments, and logistical arrangements needed to accommodate athletes from various colleges. The meeting emphasized the importance of proper planning to avoid conflicts and ensure all participants have fair opportunities to compete.</p><p>Representatives from different colleges provided input on their specific needs and concerns. The Sports Director assured participants that all scheduling issues would be resolved before the games begin, emphasizing the commitment to a successful and well-organized athletic event.</p>',
        'UEP Sports Director and officials meet to resolve scheduling challenges for the upcoming UEPAA Games 2025.',
        'PUBLISHED',
        true,
        267,
        writer_user_id,
        sports_cat_id,
        '2025-09-16 10:00:00'
    ) ON CONFLICT (slug) DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a.id, intercollegiate_tag_id
    FROM articles a
    WHERE a.slug = 'uep-sports-director-officials-tackle-scheduling-issues-in-uepaa-games-25-prep-meeting'
    AND NOT EXISTS (SELECT 1 FROM article_tags WHERE article_id = a.id AND tag_id = intercollegiate_tag_id);

    -- Sports Article 2: Campus Sports
    INSERT INTO articles (title, slug, content, excerpt, status, featured, view_count, author_id, category_id, published_at)
    VALUES (
        'Intramural basketball tournament kicks off with record participation',
        'intramural-basketball-tournament-kicks-off-with-record-participation',
        '<p>The annual intramural basketball tournament at UEP has begun with unprecedented student participation. Teams from various colleges have registered, making this year''s tournament one of the largest in recent history.</p><p>The opening ceremony featured team introductions, tournament rules explanation, and a ceremonial tip-off. Students expressed excitement about the competition and the opportunity to showcase their basketball skills while building camaraderie with peers from different programs.</p><p>The tournament is scheduled to run over several weeks, with matches taking place in the university gymnasium. Organizers have emphasized fair play and sportsmanship, encouraging all participants to compete with integrity and respect for their opponents.</p>',
        'The annual intramural basketball tournament begins with record-breaking student participation across all colleges.',
        'PUBLISHED',
        true,
        234,
        writer_user_id,
        sports_cat_id,
        '2025-09-20 14:00:00'
    ) ON CONFLICT (slug) DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a.id, campus_sports_tag_id
    FROM articles a
    WHERE a.slug = 'intramural-basketball-tournament-kicks-off-with-record-participation'
    AND NOT EXISTS (SELECT 1 FROM article_tags WHERE article_id = a.id AND tag_id = campus_sports_tag_id);

    -- Sports Article 3: Intercollegiate
    INSERT INTO articles (title, slug, content, excerpt, status, featured, view_count, author_id, category_id, published_at)
    VALUES (
        'UEP athletes prepare for regional intercollegiate championships',
        'uep-athletes-prepare-for-regional-intercollegiate-championships',
        '<p>UEP student-athletes are intensifying their training in preparation for the upcoming regional intercollegiate championships. The university will be represented in multiple sports including track and field, volleyball, and swimming.</p><p>Coaches have implemented rigorous training schedules, focusing on skill development, physical conditioning, and strategic preparation. Student-athletes are balancing their academic responsibilities with demanding practice sessions, demonstrating remarkable dedication and time management.</p><p>The university community has rallied behind the athletes, with students and faculty expressing support and encouragement. The championships represent an opportunity for UEP to showcase athletic excellence and compete against other universities in the region.</p>',
        'UEP student-athletes intensify training for regional intercollegiate championships across multiple sports disciplines.',
        'PUBLISHED',
        false,
        189,
        writer_user_id,
        sports_cat_id,
        '2025-09-22 11:00:00'
    ) ON CONFLICT (slug) DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a.id, intercollegiate_tag_id
    FROM articles a
    WHERE a.slug = 'uep-athletes-prepare-for-regional-intercollegiate-championships'
    AND NOT EXISTS (SELECT 1 FROM article_tags WHERE article_id = a.id AND tag_id = intercollegiate_tag_id);

    -- Sports Article 4: Campus Sports
    INSERT INTO articles (title, slug, content, excerpt, status, featured, view_count, author_id, category_id, published_at)
    VALUES (
        'New sports facilities enhance campus athletic programs',
        'new-sports-facilities-enhance-campus-athletic-programs',
        '<p>Recently completed upgrades to UEP''s sports facilities have significantly enhanced the campus athletic experience. The improvements include renovated courts, upgraded equipment, and improved lighting systems that allow for extended practice hours.</p><p>Students and coaches have praised the new facilities, noting that the improvements create a more professional training environment. The enhanced facilities support various sports programs and provide better opportunities for skill development and competition preparation.</p><p>The university administration emphasized its commitment to supporting student athletics and providing quality facilities that enable athletes to reach their full potential. The investment in sports infrastructure reflects the importance placed on physical education and athletic development.</p>',
        'Newly upgraded sports facilities at UEP provide enhanced training environments for campus athletic programs.',
        'PUBLISHED',
        false,
        156,
        writer_user_id,
        sports_cat_id,
        '2025-09-25 13:00:00'
    ) ON CONFLICT (slug) DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a.id, campus_sports_tag_id
    FROM articles a
    WHERE a.slug = 'new-sports-facilities-enhance-campus-athletic-programs'
    AND NOT EXISTS (SELECT 1 FROM article_tags WHERE article_id = a.id AND tag_id = campus_sports_tag_id);

    -- Sports Article 5: Intercollegiate
    INSERT INTO articles (title, slug, content, excerpt, status, featured, view_count, author_id, category_id, published_at)
    VALUES (
        'UEP volleyball team secures victory in intercollegiate match',
        'uep-volleyball-team-secures-victory-in-intercollegiate-match',
        '<p>The UEP volleyball team achieved a decisive victory in their recent intercollegiate match, demonstrating strong teamwork and strategic play. The match showcased the team''s months of preparation and dedication to the sport.</p><p>Players executed their game plan effectively, with strong serves, precise sets, and powerful spikes that kept their opponents on the defensive. The victory was a team effort, with contributions from all players on the court.</p><p>Coach and team members expressed satisfaction with the performance while acknowledging that there is always room for improvement. The victory boosts team morale as they continue their season and prepare for upcoming competitions.</p>',
        'UEP volleyball team celebrates a decisive victory in intercollegiate competition through strong teamwork and strategic play.',
        'PUBLISHED',
        false,
        198,
        writer_user_id,
        sports_cat_id,
        '2025-09-28 15:00:00'
    ) ON CONFLICT (slug) DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a.id, intercollegiate_tag_id
    FROM articles a
    WHERE a.slug = 'uep-volleyball-team-secures-victory-in-intercollegiate-match'
    AND NOT EXISTS (SELECT 1 FROM article_tags WHERE article_id = a.id AND tag_id = intercollegiate_tag_id);

    -- Sports Article 6: Campus Sports
    INSERT INTO articles (title, slug, content, excerpt, status, featured, view_count, author_id, category_id, published_at)
    VALUES (
        'Annual fun run promotes health and wellness on campus',
        'annual-fun-run-promotes-health-and-wellness-on-campus',
        '<p>The annual campus fun run brought together students, faculty, and staff for a morning of physical activity and community building. The event, organized by the Physical Education Department, emphasized the importance of regular exercise and healthy living.</p><p>Participants of all fitness levels joined the run, with some completing the full course and others walking at their own pace. The event created a festive atmosphere, with music, refreshments, and a sense of camaraderie among participants.</p><p>Organizers highlighted the event''s success in promoting physical wellness and bringing the campus community together. The fun run has become a beloved annual tradition that encourages healthy habits and strengthens university spirit.</p>',
        'The annual campus fun run brings together the UEP community to promote health, wellness, and campus unity.',
        'PUBLISHED',
        false,
        145,
        writer_user_id,
        sports_cat_id,
        '2025-10-01 07:00:00'
    ) ON CONFLICT (slug) DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a.id, campus_sports_tag_id
    FROM articles a
    WHERE a.slug = 'annual-fun-run-promotes-health-and-wellness-on-campus'
    AND NOT EXISTS (SELECT 1 FROM article_tags WHERE article_id = a.id AND tag_id = campus_sports_tag_id);

    -- Sports Article 7: Intercollegiate
    INSERT INTO articles (title, slug, content, excerpt, status, featured, view_count, author_id, category_id, published_at)
    VALUES (
        'UEP track and field athletes set new personal records',
        'uep-track-and-field-athletes-set-new-personal-records',
        '<p>Several UEP track and field athletes achieved new personal records during recent competitions, demonstrating their progress and dedication to training. The achievements span multiple events including sprints, distance running, and field events.</p><p>Coaches praised the athletes for their commitment to improvement and their ability to perform under pressure. The personal records reflect months of focused training and the athletes'' determination to push their limits.</p><p>The achievements have inspired other team members and highlighted the potential for continued success. As the season progresses, athletes are setting new goals and working toward even better performances in upcoming competitions.</p>',
        'UEP track and field athletes achieve new personal records across multiple events, showcasing their training progress.',
        'PUBLISHED',
        false,
        167,
        writer_user_id,
        sports_cat_id,
        '2025-10-03 10:00:00'
    ) ON CONFLICT (slug) DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a.id, intercollegiate_tag_id
    FROM articles a
    WHERE a.slug = 'uep-track-and-field-athletes-set-new-personal-records'
    AND NOT EXISTS (SELECT 1 FROM article_tags WHERE article_id = a.id AND tag_id = intercollegiate_tag_id);

    -- Sports Article 8: Campus Sports
    INSERT INTO articles (title, slug, content, excerpt, status, featured, view_count, author_id, category_id, published_at)
    VALUES (
        'Badminton club hosts friendly tournament for students',
        'badminton-club-hosts-friendly-tournament-for-students',
        '<p>The UEP Badminton Club organized a friendly tournament open to all students, creating an opportunity for recreational players and enthusiasts to compete in a supportive environment. The tournament attracted participants from various colleges and skill levels.</p><p>Matches were played in a round-robin format, allowing players to compete against multiple opponents. The friendly atmosphere encouraged participation and helped build connections among students who share an interest in badminton.</p><p>Club organizers expressed satisfaction with the turnout and the positive response from participants. The tournament served as both a competitive event and a social gathering, strengthening the badminton community on campus.</p>',
        'The UEP Badminton Club hosts a friendly tournament welcoming students of all skill levels to compete and connect.',
        'PUBLISHED',
        false,
        123,
        writer_user_id,
        sports_cat_id,
        '2025-09-27 16:00:00'
    ) ON CONFLICT (slug) DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a.id, campus_sports_tag_id
    FROM articles a
    WHERE a.slug = 'badminton-club-hosts-friendly-tournament-for-students'
    AND NOT EXISTS (SELECT 1 FROM article_tags WHERE article_id = a.id AND tag_id = campus_sports_tag_id);

    -- Sports Article 9: Intercollegiate
    INSERT INTO articles (title, slug, content, excerpt, status, featured, view_count, author_id, category_id, published_at)
    VALUES (
        'UEP swimming team competes in regional championships',
        'uep-swimming-team-competes-in-regional-championships',
        '<p>The UEP swimming team represented the university at the regional swimming championships, competing against athletes from universities across the region. The team participated in various events including freestyle, backstroke, breaststroke, and relay competitions.</p><p>Swimmers demonstrated strong technique and competitive spirit throughout the championships. Team members supported each other and showed resilience in facing tough competition from other institutions.</p><p>While the competition was challenging, the experience provided valuable learning opportunities for the athletes. The team gained insights into their strengths and areas for improvement, which will guide their training for future competitions.</p>',
        'UEP swimming team competes in regional championships, showcasing skill and determination across multiple swimming events.',
        'PUBLISHED',
        false,
        178,
        writer_user_id,
        sports_cat_id,
        '2025-09-30 12:00:00'
    ) ON CONFLICT (slug) DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a.id, intercollegiate_tag_id
    FROM articles a
    WHERE a.slug = 'uep-swimming-team-competes-in-regional-championships'
    AND NOT EXISTS (SELECT 1 FROM article_tags WHERE article_id = a.id AND tag_id = intercollegiate_tag_id);

    -- Sports Article 10: Campus Sports
    INSERT INTO articles (title, slug, content, excerpt, status, featured, view_count, author_id, category_id, published_at)
    VALUES (
        'Table tennis enthusiasts form campus league',
        'table-tennis-enthusiasts-form-campus-league',
        '<p>Table tennis enthusiasts at UEP have organized an informal campus league, bringing together players who share a passion for the sport. The league provides regular opportunities for friendly competition and skill development.</p><p>Matches are scheduled weekly, allowing participants to compete regularly while balancing their academic commitments. The league welcomes players of all skill levels, from beginners learning the basics to experienced players seeking competitive matches.</p><p>Organizers hope the league will grow and eventually become an official campus sports activity. The initiative demonstrates student initiative in creating opportunities for athletic engagement and community building through sports.</p>',
        'Table tennis enthusiasts organize a campus league, creating regular opportunities for friendly competition and skill development.',
        'PUBLISHED',
        false,
        134,
        writer_user_id,
        sports_cat_id,
        '2025-10-02 14:00:00'
    ) ON CONFLICT (slug) DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a.id, campus_sports_tag_id
    FROM articles a
    WHERE a.slug = 'table-tennis-enthusiasts-form-campus-league'
    AND NOT EXISTS (SELECT 1 FROM article_tags WHERE article_id = a.id AND tag_id = campus_sports_tag_id);

END $$;

-- ============================================
-- MIGRATION COMPLETE
-- ============================================
-- Verify the migration:
-- SELECT COUNT(*) FROM categories WHERE slug = 'sports';  -- Should return 1
-- SELECT COUNT(*) FROM tags WHERE slug IN ('campus-sports', 'intercollegiate');  -- Should return 2
-- SELECT COUNT(*) FROM articles WHERE category_id = (SELECT id FROM categories WHERE slug = 'sports');  -- Should return 10

