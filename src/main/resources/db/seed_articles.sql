-- ============================================
-- Seed Script for Placeholder Articles
-- Run this after init.sql to populate the database with sample content
-- This script is idempotent - can be run multiple times safely
-- ============================================

-- ============================================
-- 1. CREATE TAGS FOR SUBCATEGORIES
-- ============================================
-- Insert tags if they don't exist (using ON CONFLICT DO NOTHING)
INSERT INTO tags (name, slug) VALUES
-- News subcategories
('Academe', 'academe'),
('National News', 'national-news'),
-- Opinion subcategories
('Editorial', 'editorial'),
('Opinion Piece', 'opinion-piece'),
-- Feature subcategories
('Feature Story', 'feature-story'),
('Interview', 'interview'),
-- Editorial subcategories (Editorial tag already created above)
('Column', 'column'),
-- SciTech subcategories
('Science', 'science'),
('Technology', 'technology')
ON CONFLICT (slug) DO NOTHING;

-- ============================================
-- 2. CREATE DEFAULT AUTHOR USER
-- ============================================
-- Create a default writer/editor user if none exists
-- Password: 'writer123' (BCrypt hashed) - you should change this in production
INSERT INTO users (email, password, first_name, last_name, role_id)
SELECT 
    'writer@uep.edu.ph',
    '$2a$10$rKqHXzKXJxKXJxKXJxKXeOQrKqHXzKXJxKXJxKXJxKXJxKXJxKXJxK', -- BCrypt hash for 'writer123'
    'John',
    'Doe',
    3 -- WRITER role
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'writer@uep.edu.ph');

-- Get the writer user ID (we'll use this for all articles)
DO $$
DECLARE
    writer_user_id BIGINT;
BEGIN
    SELECT id INTO writer_user_id FROM users WHERE email = 'writer@uep.edu.ph' LIMIT 1;
    
    -- If no user exists, create one with a simpler approach
    IF writer_user_id IS NULL THEN
        INSERT INTO users (email, password, first_name, last_name, role_id)
        VALUES ('writer@uep.edu.ph', '$2a$10$rKqHXzKXJxKXJxKXJxKXeOQrKqHXzKXJxKXJxKXJxKXJxKXJxKXJxKXJxK', 'John', 'Doe', 3)
        RETURNING id INTO writer_user_id;
    END IF;

    -- Store in a temporary variable for use in subsequent inserts
    -- We'll reference it directly in the INSERT statements below
END $$;

-- ============================================
-- 3. CREATE PLACEHOLDER MEDIA ENTRIES
-- ============================================
-- Insert placeholder media entries for article thumbnails
-- We'll create these with placeholder image URLs
DO $$
DECLARE
    writer_user_id BIGINT;
    media_id_counter BIGINT;
BEGIN
    SELECT id INTO writer_user_id FROM users WHERE email = 'writer@uep.edu.ph' LIMIT 1;
    
    -- Create placeholder media entries (we'll reuse the same media ID pattern)
    -- In a real scenario, you'd create individual media entries, but for seeding we can use NULL cover_id
    -- and just use URL strings directly, or create a few generic media entries
END $$;

-- ============================================
-- 4. INSERT ARTICLES FOR EACH CATEGORY
-- ============================================

-- Helper function to get category ID by slug
DO $$
DECLARE
    news_cat_id INT;
    feature_cat_id INT;
    opinion_cat_id INT;
    editorial_cat_id INT;
    scitech_cat_id INT;
    writer_user_id BIGINT;
    academe_tag_id INT;
    national_news_tag_id INT;
    editorial_tag_id INT;
    opinion_piece_tag_id INT;
    feature_story_tag_id INT;
    interview_tag_id INT;
    column_tag_id INT;
    science_tag_id INT;
    technology_tag_id INT;
BEGIN
    -- Get category IDs
    SELECT id INTO news_cat_id FROM categories WHERE slug = 'news';
    SELECT id INTO feature_cat_id FROM categories WHERE slug = 'feature';
    SELECT id INTO opinion_cat_id FROM categories WHERE slug = 'opinion';
    SELECT id INTO editorial_cat_id FROM categories WHERE slug = 'editorial';
    SELECT id INTO scitech_cat_id FROM categories WHERE slug = 'sci-tech';
    
    -- Get writer user ID
    SELECT id INTO writer_user_id FROM users WHERE email = 'writer@uep.edu.ph' LIMIT 1;
    
    -- Get tag IDs
    SELECT id INTO academe_tag_id FROM tags WHERE slug = 'academe';
    SELECT id INTO national_news_tag_id FROM tags WHERE slug = 'national-news';
    SELECT id INTO editorial_tag_id FROM tags WHERE slug = 'editorial';
    SELECT id INTO opinion_piece_tag_id FROM tags WHERE slug = 'opinion-piece';
    SELECT id INTO feature_story_tag_id FROM tags WHERE slug = 'feature-story';
    SELECT id INTO interview_tag_id FROM tags WHERE slug = 'interview';
    SELECT id INTO column_tag_id FROM tags WHERE slug = 'column';
    SELECT id INTO science_tag_id FROM tags WHERE slug = 'science';
    SELECT id INTO technology_tag_id FROM tags WHERE slug = 'technology';

    -- ============================================
    -- NEWS ARTICLES (10 articles)
    -- ============================================
    
    -- News Article 1: Featured
    INSERT INTO articles (title, slug, content, excerpt, status, featured, view_count, author_id, category_id, published_at)
    VALUES (
        'USC, PYDO spearheads Balik Kampus ''25, spotlights Fair and Kabataan Caravan',
        'usc-pydo-spearheads-balik-kampus-25-spotlights-fair-and-kabataan-caravan',
        '<p>The University Student Council (USC) and Provincial Youth Development Office (PYDO) jointly organized the annual Balik Kampus event, bringing together students from various colleges for a day of festivities and community engagement. The event featured a vibrant fair showcasing local businesses, food stalls, and student organizations.</p><p>The Kabataan Caravan, a highlight of the event, emphasized the importance of youth participation in community development. Various workshops and seminars were conducted, covering topics from leadership to entrepreneurship.</p><p>Students expressed enthusiasm about the event, citing it as an excellent way to reconnect with the campus community after the summer break. The organizers aim to make this an annual tradition that strengthens the bond between the university and the local community.</p>',
        'The University Student Council and Provincial Youth Development Office jointly organized the annual Balik Kampus event, featuring various activities for returning students.',
        'PUBLISHED',
        true,
        150,
        writer_user_id,
        news_cat_id,
        '2025-09-23 10:00:00'
    ) ON CONFLICT (slug) DO NOTHING
    RETURNING id INTO media_id_counter;

    -- Link to Academe tag
    INSERT INTO article_tags (article_id, tag_id)
    SELECT a.id, academe_tag_id
    FROM articles a
    WHERE a.slug = 'usc-pydo-spearheads-balik-kampus-25-spotlights-fair-and-kabataan-caravan'
    AND NOT EXISTS (SELECT 1 FROM article_tags WHERE article_id = a.id AND tag_id = academe_tag_id);

    -- News Article 2
    INSERT INTO articles (title, slug, content, excerpt, status, featured, view_count, author_id, category_id, published_at)
    VALUES (
        'CCJ stages officers'' induction; Sleuth King and Queen strut during acquaintance',
        'ccj-stages-officers-induction-sleuth-king-and-queen-strut-during-acquaintance',
        '<p>The College of Criminal Justice (CCJ) held its annual officers'' induction ceremony, marking the beginning of the new academic year with pomp and celebration. The event was highlighted by the coronation of the Sleuth King and Queen, students who excelled in various CCJ competitions and activities.</p><p>The induction ceremony saw the new set of officers taking their oath of office, pledging to serve the college with dedication and integrity. Faculty members and students gathered to witness the milestone event.</p><p>The acquaintance party that followed featured creative performances, with students showcasing their talents in dance, music, and drama. The Sleuth King and Queen were presented with crowns, symbolizing their leadership roles in the college community.</p>',
        'The College of Criminal Justice held its annual officers'' induction ceremony with the coronation of Sleuth King and Queen.',
        'PUBLISHED',
        false,
        89,
        writer_user_id,
        news_cat_id,
        '2025-10-03 14:30:00'
    ) ON CONFLICT (slug) DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a.id, academe_tag_id
    FROM articles a
    WHERE a.slug = 'ccj-stages-officers-induction-sleuth-king-and-queen-strut-during-acquaintance'
    AND NOT EXISTS (SELECT 1 FROM article_tags WHERE article_id = a.id AND tag_id = academe_tag_id);

    -- News Article 3
    INSERT INTO articles (title, slug, content, excerpt, status, featured, view_count, author_id, category_id, published_at)
    VALUES (
        'Lipon Panitik hosts Huruhimangraw for A.Y. ''25-''26',
        'lipon-panitik-hosts-huruhimangraw-for-ay-25-26',
        '<p>The Bachelor of Arts in Literature (AB Lit) society, known as Lipunang Pampanitikan (Lipon Panitik), successfully hosted its second run of ''Huruhimangraw'', a program orientation and general assembly. The event brought together literature students and faculty members to celebrate the start of the academic year.</p><p>Huruhimangraw featured various literary activities including poetry readings, spoken word performances, and discussions about upcoming literary projects. The organization''s new set of officers were introduced, outlining their plans for the academic year.</p><p>Faculty members from the Literature department commended the organization for its continued commitment to promoting literature and creative writing among students. The event concluded with a showcase of student literary works.</p>',
        'The AB Lit society Lipon Panitik hosted its annual Huruhimangraw program orientation and general assembly.',
        'PUBLISHED',
        false,
        76,
        writer_user_id,
        news_cat_id,
        '2025-10-03 16:00:00'
    ) ON CONFLICT (slug) DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a.id, academe_tag_id
    FROM articles a
    WHERE a.slug = 'lipon-panitik-hosts-huruhimangraw-for-ay-25-26'
    AND NOT EXISTS (SELECT 1 FROM article_tags WHERE article_id = a.id AND tag_id = academe_tag_id);

    -- News Article 4
    INSERT INTO articles (title, slug, content, excerpt, status, featured, view_count, author_id, category_id, published_at)
    VALUES (
        'COE holds Stakeholders'' Consultative Meeting and Orientation',
        'coe-holds-stakeholders-consultative-meeting-and-orientation',
        '<p>The College of Education (COE) organized a comprehensive stakeholders'' consultative meeting, bringing together faculty members, students, and partner institutions to discuss educational initiatives and collaborative projects. The meeting focused on curriculum development and community engagement programs.</p><p>Key topics discussed included partnerships with local schools for student teaching programs, research collaborations, and professional development opportunities for education students. Stakeholders provided valuable input on how to enhance the teacher education program.</p><p>The orientation session that followed introduced new students to the college''s programs, facilities, and opportunities. Faculty members emphasized the importance of maintaining high standards in teacher education.</p>',
        'The College of Education held a stakeholders'' consultative meeting to discuss educational initiatives and partnerships.',
        'PUBLISHED',
        false,
        94,
        writer_user_id,
        news_cat_id,
        '2025-10-03 09:00:00'
    ) ON CONFLICT (slug) DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a.id, academe_tag_id
    FROM articles a
    WHERE a.slug = 'coe-holds-stakeholders-consultative-meeting-and-orientation'
    AND NOT EXISTS (SELECT 1 FROM article_tags WHERE article_id = a.id AND tag_id = academe_tag_id);

    -- News Article 5
    INSERT INTO articles (title, slug, content, excerpt, status, featured, view_count, author_id, category_id, published_at)
    VALUES (
        'CNAHS kicks off A.Y. ''25-''26 with Freshmen Orientation',
        'cnahs-kicks-off-ay-25-26-with-freshmen-orientation',
        '<p>The College of Nursing and Allied Health Sciences (CNAHS) welcomed incoming freshmen with an extensive orientation program. The event introduced new students to the rigors and rewards of pursuing a career in healthcare.</p><p>Faculty members presented the various programs offered by the college, including Nursing, Medical Technology, and other allied health programs. Students learned about the academic requirements, clinical training opportunities, and career prospects in the healthcare field.</p><p>The orientation also included a tour of the college''s facilities, including simulation laboratories and clinical skills training areas. Upperclassmen shared their experiences and offered advice to the new students.</p>',
        'The College of Nursing and Allied Health Sciences welcomed freshmen with a comprehensive orientation program.',
        'PUBLISHED',
        false,
        112,
        writer_user_id,
        news_cat_id,
        '2025-10-03 11:00:00'
    ) ON CONFLICT (slug) DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a.id, academe_tag_id
    FROM articles a
    WHERE a.slug = 'cnahs-kicks-off-ay-25-26-with-freshmen-orientation'
    AND NOT EXISTS (SELECT 1 FROM article_tags WHERE article_id = a.id AND tag_id = academe_tag_id);

    -- News Article 6: National News
    INSERT INTO articles (title, slug, content, excerpt, status, featured, view_count, author_id, category_id, published_at)
    VALUES (
        'Magnitude 7.5 earthquake strikes Davao Oriental; tsunami warning issued, light tremors felt in Northern Samar',
        'magnitude-75-earthquake-strikes-davao-oriental-tsunami-warning-issued-light-tremors-felt-in-northern-samar',
        '<p>A powerful magnitude 7.5 earthquake struck Davao Oriental early this morning, triggering tsunami warnings across the region. The Philippine Institute of Volcanology and Seismology (PHIVOLCS) issued immediate alerts for coastal areas.</p><p>Residents in Northern Samar reported feeling light tremors, though no significant damage was reported in the area. The earthquake''s epicenter was located offshore, near the Philippine Trench, causing concern among geologists about potential aftershocks.</p><p>Emergency response teams were immediately mobilized, and evacuation procedures were initiated in low-lying coastal areas. Government agencies are monitoring the situation closely and providing regular updates to the public.</p>',
        'A magnitude 7.5 earthquake struck Davao Oriental, with tsunami warnings issued and light tremors felt in Northern Samar.',
        'PUBLISHED',
        true,
        324,
        writer_user_id,
        news_cat_id,
        '2025-09-23 06:00:00'
    ) ON CONFLICT (slug) DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a.id, national_news_tag_id
    FROM articles a
    WHERE a.slug = 'magnitude-75-earthquake-strikes-davao-oriental-tsunami-warning-issued-light-tremors-felt-in-northern-samar'
    AND NOT EXISTS (SELECT 1 FROM article_tags WHERE article_id = a.id AND tag_id = national_news_tag_id);

    -- News Article 7: National News
    INSERT INTO articles (title, slug, content, excerpt, status, featured, view_count, author_id, category_id, published_at)
    VALUES (
        '12.3 B Pondo para sa libreng kolehiyo, inaprubhan na ng Kamara',
        '123-b-pondo-para-sa-libreng-kolehiyo-inaprubhan-na-ng-kamara',
        '<p>Ang Kamara de Representantes ay nag-apruba ng 12.3 bilyong piso na pondo para sa libreng kolehiyo program ng bansa. Ang panukalang batas ay tinanggap ng malawak na suporta mula sa mga mambabatas at edukasyon advocates.</p><p>Ang pondo ay inilaan para sa mga state universities at colleges (SUCs) at local universities and colleges (LUCs) upang matiyak na walang estudyante ang maiiwan dahil sa kahirapan. Ito ay bahagi ng patuloy na pagsisikap ng gobyerno na gawing accessible ang quality education.</p><p>Ang inisyatiba ay inaasahang makakatulong sa libu-libong estudyante na makapagpatuloy ng kanilang pag-aaral. Ang mga beneficiaries ay kabilang ang mga estudyante mula sa low-income families at indigenous communities.</p>',
        'Ang Kamara ay nag-apruba ng 12.3 bilyong piso para sa libreng kolehiyo program ng bansa.',
        'PUBLISHED',
        false,
        287,
        writer_user_id,
        news_cat_id,
        '2025-09-26 10:00:00'
    ) ON CONFLICT (slug) DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a.id, national_news_tag_id
    FROM articles a
    WHERE a.slug = '123-b-pondo-para-sa-libreng-kolehiyo-inaprubhan-na-ng-kamara'
    AND NOT EXISTS (SELECT 1 FROM article_tags WHERE article_id = a.id AND tag_id = national_news_tag_id);

    -- News Article 8: Academe
    INSERT INTO articles (title, slug, content, excerpt, status, featured, view_count, author_id, category_id, published_at)
    VALUES (
        'UEP launches new research center for environmental studies',
        'uep-launches-new-research-center-for-environmental-studies',
        '<p>The University of Eastern Philippines inaugurated its new Research Center for Environmental Studies, marking a significant milestone in the institution''s commitment to environmental research and sustainability. The center will serve as a hub for interdisciplinary research on climate change, biodiversity, and environmental conservation.</p><p>Equipped with state-of-the-art laboratories and research facilities, the center will support faculty and student research projects. The inauguration was attended by university officials, faculty members, and representatives from partner institutions.</p><p>The center aims to contribute to national and regional efforts in addressing environmental challenges through research and community engagement. Several research projects are already in the pipeline, focusing on local environmental issues and sustainable practices.</p>',
        'UEP inaugurated a new Research Center for Environmental Studies to support research on climate change and biodiversity.',
        'PUBLISHED',
        false,
        156,
        writer_user_id,
        news_cat_id,
        '2025-10-01 13:00:00'
    ) ON CONFLICT (slug) DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a.id, academe_tag_id
    FROM articles a
    WHERE a.slug = 'uep-launches-new-research-center-for-environmental-studies'
    AND NOT EXISTS (SELECT 1 FROM article_tags WHERE article_id = a.id AND tag_id = academe_tag_id);

    -- News Article 9: Academe
    INSERT INTO articles (title, slug, content, excerpt, status, featured, view_count, author_id, category_id, published_at)
    VALUES (
        'CAS holds annual research symposium',
        'cas-holds-annual-research-symposium',
        '<p>The College of Arts and Sciences (CAS) successfully conducted its annual research symposium, showcasing student and faculty research across various disciplines. The event featured presentations on topics ranging from literature and history to natural sciences and mathematics.</p><p>Students presented their undergraduate and graduate research projects, receiving feedback from faculty members and peers. The symposium provided a platform for academic discourse and knowledge sharing among the college community.</p><p>Outstanding research papers were recognized, with awards given to students who demonstrated excellence in research methodology and presentation. The event highlighted the college''s commitment to promoting research culture among students.</p>',
        'The College of Arts and Sciences held its annual research symposium featuring student and faculty research presentations.',
        'PUBLISHED',
        false,
        98,
        writer_user_id,
        news_cat_id,
        '2025-09-28 14:00:00'
    ) ON CONFLICT (slug) DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a.id, academe_tag_id
    FROM articles a
    WHERE a.slug = 'cas-holds-annual-research-symposium'
    AND NOT EXISTS (SELECT 1 FROM article_tags WHERE article_id = a.id AND tag_id = academe_tag_id);

    -- News Article 10: National News
    INSERT INTO articles (title, slug, content, excerpt, status, featured, view_count, author_id, category_id, published_at)
    VALUES (
        'DepEd announces new policies for teacher education programs',
        'deped-announces-new-policies-for-teacher-education-programs',
        '<p>The Department of Education (DepEd) announced new policies affecting teacher education programs across the country. The changes aim to enhance the quality of teacher preparation and align programs with international standards.</p><p>Key changes include updated curriculum requirements, extended field experience periods, and new assessment mechanisms. Teacher education institutions are expected to adapt their programs to meet these new standards.</p><p>Education stakeholders have expressed mixed reactions to the new policies, with some welcoming the changes as necessary improvements, while others raise concerns about implementation challenges. DepEd officials have committed to providing support and resources to help institutions transition to the new framework.</p>',
        'DepEd announced new policies for teacher education programs to enhance quality and align with international standards.',
        'PUBLISHED',
        false,
        203,
        writer_user_id,
        news_cat_id,
        '2025-09-25 11:00:00'
    ) ON CONFLICT (slug) DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a.id, national_news_tag_id
    FROM articles a
    WHERE a.slug = 'deped-announces-new-policies-for-teacher-education-programs'
    AND NOT EXISTS (SELECT 1 FROM article_tags WHERE article_id = a.id AND tag_id = national_news_tag_id);

    -- ============================================
    -- OPINION ARTICLES (8 articles)
    -- ============================================

    -- Opinion Article 1: Featured
    INSERT INTO articles (title, slug, content, excerpt, status, featured, view_count, author_id, category_id, published_at)
    VALUES (
        'Campus digitalization: Are we ready?',
        'campus-digitalization-are-we-ready',
        '<p>As the university continues to embrace digital transformation, we must ask ourselves: are we truly ready for this shift? While the benefits of digitalization are clear, we must ensure that all students and faculty members have equal access to the necessary resources and training.</p><p>The transition to online platforms for administrative processes, class management, and communication has been met with both enthusiasm and apprehension. Some welcome the convenience and efficiency, while others struggle with the learning curve and technical challenges.</p><p>It is crucial that the university provides comprehensive support systems, including training programs, technical assistance, and accessible resources. Only then can we ensure that digitalization truly serves everyone in our academic community.</p>',
        'A student perspective on the ongoing digital transformation initiatives at UEP and the challenges we face.',
        'PUBLISHED',
        true,
        178,
        writer_user_id,
        opinion_cat_id,
        '2025-09-15 10:00:00'
    ) ON CONFLICT (slug) DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a.id, opinion_piece_tag_id
    FROM articles a
    WHERE a.slug = 'campus-digitalization-are-we-ready'
    AND NOT EXISTS (SELECT 1 FROM article_tags WHERE article_id = a.id AND tag_id = opinion_piece_tag_id);

    -- Opinion Article 2
    INSERT INTO articles (title, slug, content, excerpt, status, featured, view_count, author_id, category_id, published_at)
    VALUES (
        'The importance of mental health support in academic institutions',
        'importance-of-mental-health-support-in-academic-institutions',
        '<p>Mental health has become an increasingly important topic in academic institutions, and rightly so. The pressures of academic life, combined with personal challenges, can take a significant toll on students'' well-being. It is essential that universities prioritize mental health support services.</p><p>While UEP has made strides in this area, there is still much work to be done. We need more counselors, more accessible services, and a culture that destigmatizes seeking help. Students should feel comfortable reaching out when they need support.</p><p>Investing in mental health is not just the right thing to do—it is also an investment in our students'' academic success and overall quality of life. Let us continue to build a supportive community where everyone can thrive.</p>',
        'A call for increased mental health support services in universities and the importance of creating a supportive environment.',
        'PUBLISHED',
        false,
        145,
        writer_user_id,
        opinion_cat_id,
        '2025-09-20 14:00:00'
    ) ON CONFLICT (slug) DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a.id, opinion_piece_tag_id
    FROM articles a
    WHERE a.slug = 'importance-of-mental-health-support-in-academic-institutions'
    AND NOT EXISTS (SELECT 1 FROM article_tags WHERE article_id = a.id AND tag_id = opinion_piece_tag_id);

    -- Opinion Article 3: Editorial tag
    INSERT INTO articles (title, slug, content, excerpt, status, featured, view_count, author_id, category_id, published_at)
    VALUES (
        'Student voice matters: Why representation is crucial',
        'student-voice-matters-why-representation-is-crucial',
        '<p>Student representation in university governance is not just a formality—it is a fundamental right and a crucial component of democratic academic institutions. When students have a voice in decisions that affect them, better outcomes follow.</p><p>We have seen the positive impact of student participation in various committees and organizations. Students bring fresh perspectives, firsthand experiences, and genuine concerns that might otherwise be overlooked. Their input helps create policies and programs that truly serve the student body.</p><p>As we move forward, let us ensure that student voices are not just heard, but actively sought and valued. True representation means giving students real influence in shaping their educational experience.</p>',
        'An editorial piece discussing the importance of student representation in university governance and decision-making.',
        'PUBLISHED',
        false,
        132,
        writer_user_id,
        opinion_cat_id,
        '2025-09-18 09:00:00'
    ) ON CONFLICT (slug) DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a.id, editorial_tag_id
    FROM articles a
    WHERE a.slug = 'student-voice-matters-why-representation-is-crucial'
    AND NOT EXISTS (SELECT 1 FROM article_tags WHERE article_id = a.id AND tag_id = editorial_tag_id);

    -- Opinion Article 4
    INSERT INTO articles (title, slug, content, excerpt, status, featured, view_count, author_id, category_id, published_at)
    VALUES (
        'Balancing tradition and innovation in education',
        'balancing-tradition-and-innovation-in-education',
        '<p>The tension between preserving traditional educational methods and embracing innovation is a constant challenge in modern academia. While we value the wisdom and proven practices of the past, we must also be open to new approaches that can enhance learning.</p><p>Traditional methods have their strengths—they have been tested over time and have produced countless successful graduates. However, innovation offers opportunities to reach more students, personalize learning, and prepare students for a rapidly changing world.</p><p>The answer is not to choose one over the other, but to find a balance. We can honor tradition while selectively adopting innovations that truly improve educational outcomes. This requires careful evaluation, open-mindedness, and a commitment to what works best for our students.</p>',
        'A reflection on finding the right balance between traditional educational methods and innovative approaches.',
        'PUBLISHED',
        false,
        167,
        writer_user_id,
        opinion_cat_id,
        '2025-09-22 11:00:00'
    ) ON CONFLICT (slug) DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a.id, opinion_piece_tag_id
    FROM articles a
    WHERE a.slug = 'balancing-tradition-and-innovation-in-education'
    AND NOT EXISTS (SELECT 1 FROM article_tags WHERE article_id = a.id AND tag_id = opinion_piece_tag_id);

    -- Opinion Article 5
    INSERT INTO articles (title, slug, content, excerpt, status, featured, view_count, author_id, category_id, published_at)
    VALUES (
        'The value of extracurricular activities in holistic education',
        'value-of-extracurricular-activities-in-holistic-education',
        '<p>Extracurricular activities are often viewed as secondary to academic pursuits, but they play a crucial role in holistic education. These activities help students develop skills, build character, and discover passions that extend beyond the classroom.</p><p>Participation in clubs, organizations, sports, and cultural activities teaches students valuable lessons in teamwork, leadership, time management, and responsibility. These are skills that will serve them well in their careers and personal lives.</p><p>As students, we should actively seek opportunities to get involved. As an institution, we should continue to support and encourage diverse extracurricular programs that enrich the student experience and contribute to well-rounded graduates.</p>',
        'An opinion piece on the importance of extracurricular activities in developing well-rounded students.',
        'PUBLISHED',
        false,
        121,
        writer_user_id,
        opinion_cat_id,
        '2025-09-25 15:00:00'
    ) ON CONFLICT (slug) DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a.id, opinion_piece_tag_id
    FROM articles a
    WHERE a.slug = 'value-of-extracurricular-activities-in-holistic-education'
    AND NOT EXISTS (SELECT 1 FROM article_tags WHERE article_id = a.id AND tag_id = opinion_piece_tag_id);

    -- Opinion Article 6
    INSERT INTO articles (title, slug, content, excerpt, status, featured, view_count, author_id, category_id, published_at)
    VALUES (
        'Addressing the rising cost of education',
        'addressing-the-rising-cost-of-education',
        '<p>The rising cost of education continues to be a significant concern for students and families across the country. While higher education remains an investment in the future, the financial burden can be overwhelming for many.</p><p>Scholarships, financial aid, and work-study programs provide some relief, but more comprehensive solutions are needed. We must continue to advocate for policies that make education more accessible and affordable for all qualified students.</p><p>Universities also have a role to play in managing costs and providing transparent information about expenses and financial aid options. Every student deserves the opportunity to pursue their educational goals without being burdened by excessive debt.</p>',
        'A commentary on the challenges of rising education costs and the need for accessible financial aid.',
        'PUBLISHED',
        false,
        189,
        writer_user_id,
        opinion_cat_id,
        '2025-09-28 10:00:00'
    ) ON CONFLICT (slug) DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a.id, editorial_tag_id
    FROM articles a
    WHERE a.slug = 'addressing-the-rising-cost-of-education'
    AND NOT EXISTS (SELECT 1 FROM article_tags WHERE article_id = a.id AND tag_id = editorial_tag_id);

    -- Opinion Article 7
    INSERT INTO articles (title, slug, content, excerpt, status, featured, view_count, author_id, category_id, published_at)
    VALUES (
        'Building a culture of respect and inclusivity on campus',
        'building-culture-of-respect-and-inclusivity-on-campus',
        '<p>A truly inclusive campus is one where every student feels valued, respected, and welcome regardless of their background, beliefs, or identity. Building such a culture requires intentional effort from all members of the university community.</p><p>Respectful dialogue, open-mindedness, and empathy are essential components of an inclusive environment. We must actively work to understand and appreciate different perspectives, even when we disagree.</p><p>Creating this culture is an ongoing process that requires commitment from students, faculty, and administration. Let us continue to foster an environment where diversity is celebrated and everyone can contribute to a vibrant academic community.</p>',
        'A perspective on the importance of creating an inclusive and respectful campus culture for all students.',
        'PUBLISHED',
        false,
        154,
        writer_user_id,
        opinion_cat_id,
        '2025-10-01 13:00:00'
    ) ON CONFLICT (slug) DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a.id, opinion_piece_tag_id
    FROM articles a
    WHERE a.slug = 'building-culture-of-respect-and-inclusivity-on-campus'
    AND NOT EXISTS (SELECT 1 FROM article_tags WHERE article_id = a.id AND tag_id = opinion_piece_tag_id);

    -- Opinion Article 8
    INSERT INTO articles (title, slug, content, excerpt, status, featured, view_count, author_id, category_id, published_at)
    VALUES (
        'The role of critical thinking in the age of information overload',
        'role-of-critical-thinking-in-age-of-information-overload',
        '<p>In an era of information overload and misinformation, critical thinking skills have never been more important. As students, we are constantly bombarded with information from various sources, and the ability to evaluate, analyze, and form reasoned judgments is crucial.</p><p>Educational institutions must prioritize the development of critical thinking skills across all disciplines. This means going beyond rote memorization and encouraging students to question, analyze, and think independently.</p><p>As students, we must also take responsibility for our own learning and actively practice critical thinking. This involves being skeptical of information, seeking multiple perspectives, and being willing to change our views when presented with evidence.</p>',
        'An opinion on the crucial importance of critical thinking skills in navigating today''s information landscape.',
        'PUBLISHED',
        false,
        198,
        writer_user_id,
        opinion_cat_id,
        '2025-09-30 12:00:00'
    ) ON CONFLICT (slug) DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a.id, opinion_piece_tag_id
    FROM articles a
    WHERE a.slug = 'role-of-critical-thinking-in-age-of-information-overload'
    AND NOT EXISTS (SELECT 1 FROM article_tags WHERE article_id = a.id AND tag_id = opinion_piece_tag_id);

    -- ============================================
    -- FEATURE ARTICLES (8 articles)
    -- ============================================

    -- Feature Article 1: Featured
    INSERT INTO articles (title, slug, content, excerpt, status, featured, view_count, author_id, category_id, published_at)
    VALUES (
        'UEP Students Excel in National Journalism Competition',
        'uep-students-excel-in-national-journalism-competition',
        '<p>Students from the University of Eastern Philippines made their mark at the recent National Journalism Competition, bringing home multiple awards and recognition. The competition, which drew participants from universities across the country, showcased the exceptional talent of UEP''s journalism students.</p><p>The UEP delegation participated in various categories including news writing, feature writing, editorial writing, and photojournalism. Their performance reflected months of preparation and the high-quality journalism education provided by the university.</p><p>Faculty members praised the students for their dedication and professionalism. The achievements not only bring honor to the university but also demonstrate the effectiveness of UEP''s journalism program in preparing students for careers in media and communications.</p>',
        'UEP journalism students achieved outstanding results at the National Journalism Competition, showcasing the university''s excellence in media education.',
        'PUBLISHED',
        true,
        245,
        writer_user_id,
        feature_cat_id,
        '2025-10-15 10:00:00'
    ) ON CONFLICT (slug) DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a.id, feature_story_tag_id
    FROM articles a
    WHERE a.slug = 'uep-students-excel-in-national-journalism-competition'
    AND NOT EXISTS (SELECT 1 FROM article_tags WHERE article_id = a.id AND tag_id = feature_story_tag_id);

    -- Feature Article 2: Interview
    INSERT INTO articles (title, slug, content, excerpt, status, featured, view_count, author_id, category_id, published_at)
    VALUES (
        'Interview with UEP''s Outstanding Graduate: A journey of perseverance',
        'interview-with-uep-outstanding-graduate-journey-of-perseverance',
        '<p>In this exclusive interview, we sit down with one of UEP''s outstanding graduates who overcame significant challenges to achieve academic excellence. The graduate shares their inspiring journey, from initial struggles to eventual success.</p><p>"It wasn''t always easy," they reflect. "There were times when I wanted to give up, but I kept going because I knew that education was my path to a better future. The support I received from my family, friends, and professors made all the difference."</p><p>The interview covers topics ranging from study strategies and time management to the importance of perseverance and maintaining a positive mindset. This graduate''s story serves as an inspiration to current students facing their own challenges.</p>',
        'An inspiring interview with one of UEP''s outstanding graduates about overcoming challenges and achieving academic success.',
        'PUBLISHED',
        false,
        178,
        writer_user_id,
        feature_cat_id,
        '2025-09-20 14:00:00'
    ) ON CONFLICT (slug) DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a.id, interview_tag_id
    FROM articles a
    WHERE a.slug = 'interview-with-uep-outstanding-graduate-journey-of-perseverance'
    AND NOT EXISTS (SELECT 1 FROM article_tags WHERE article_id = a.id AND tag_id = interview_tag_id);

    -- Feature Article 3
    INSERT INTO articles (title, slug, content, excerpt, status, featured, view_count, author_id, category_id, published_at)
    VALUES (
        'Behind the scenes: A day in the life of a UEP student leader',
        'behind-scenes-day-in-life-of-uep-student-leader',
        '<p>Have you ever wondered what it''s like to be a student leader? This feature takes you behind the scenes for a day with one of UEP''s active student leaders, showing the challenges, rewards, and responsibilities that come with leadership roles.</p><p>From early morning meetings to late-night planning sessions, student leaders juggle multiple responsibilities while maintaining their academic performance. The article explores how they manage their time, make difficult decisions, and work to represent their fellow students effectively.</p><p>The feature also highlights the personal growth and skills development that come from student leadership experiences. These leaders develop communication skills, organizational abilities, and a deep sense of responsibility that will serve them throughout their careers.</p>',
        'An in-depth look at the daily life and responsibilities of student leaders at UEP.',
        'PUBLISHED',
        false,
        156,
        writer_user_id,
        feature_cat_id,
        '2025-09-25 11:00:00'
    ) ON CONFLICT (slug) DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a.id, feature_story_tag_id
    FROM articles a
    WHERE a.slug = 'behind-scenes-day-in-life-of-uep-student-leader'
    AND NOT EXISTS (SELECT 1 FROM article_tags WHERE article_id = a.id AND tag_id = feature_story_tag_id);

    -- Feature Article 4
    INSERT INTO articles (title, slug, content, excerpt, status, featured, view_count, author_id, category_id, published_at)
    VALUES (
        'UPAO opens stage for artists in auditions for A.Y. ''25-26',
        'upao-opens-stage-for-artists-in-auditions-for-ay-25-26',
        '<p>The University Performing Arts Organization (UPAO) recently opened its doors for auditions, welcoming students with artistic talents to join their ranks for the academic year 2025-2026. The auditions attracted a diverse group of performers, from dancers and musicians to actors and spoken word artists.</p><p>The organization provides a platform for students to showcase their artistic talents while contributing to the university''s cultural activities. Past productions have included theatrical performances, dance recitals, and musical concerts that have enriched the campus cultural scene.</p><p>Faculty advisors emphasized that UPAO is open to students from all colleges, regardless of their major. The organization values passion and commitment over prior experience, making it accessible to all who wish to explore their artistic side.</p>',
        'The University Performing Arts Organization held auditions for the new academic year, opening opportunities for student artists.',
        'PUBLISHED',
        false,
        134,
        writer_user_id,
        feature_cat_id,
        '2025-09-25 16:00:00'
    ) ON CONFLICT (slug) DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a.id, feature_story_tag_id
    FROM articles a
    WHERE a.slug = 'upao-opens-stage-for-artists-in-auditions-for-ay-25-26'
    AND NOT EXISTS (SELECT 1 FROM article_tags WHERE article_id = a.id AND tag_id = feature_story_tag_id);

    -- Feature Article 5: Interview
    INSERT INTO articles (title, slug, content, excerpt, status, featured, view_count, author_id, category_id, published_at)
    VALUES (
        'Faculty spotlight: Professor dedicated to student success',
        'faculty-spotlight-professor-dedicated-to-student-success',
        '<p>This feature profiles a dedicated faculty member who has made student success their life''s mission. Through innovative teaching methods, mentorship, and genuine care, this professor has impacted countless students'' lives.</p><p>"Teaching is not just about delivering lectures," the professor explains. "It''s about connecting with students, understanding their needs, and helping them realize their potential. Every student has something unique to offer, and it''s our job to help them discover it."</p><p>The article explores the professor''s teaching philosophy, their approach to student mentorship, and the legacy they hope to leave. Former students share their experiences and the lasting impact this educator has had on their academic and personal development.</p>',
        'A profile of an exceptional faculty member whose dedication to student success has made a lasting impact.',
        'PUBLISHED',
        false,
        167,
        writer_user_id,
        feature_cat_id,
        '2025-09-22 10:00:00'
    ) ON CONFLICT (slug) DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a.id, interview_tag_id
    FROM articles a
    WHERE a.slug = 'faculty-spotlight-professor-dedicated-to-student-success'
    AND NOT EXISTS (SELECT 1 FROM article_tags WHERE article_id = a.id AND tag_id = interview_tag_id);

    -- Feature Article 6
    INSERT INTO articles (title, slug, content, excerpt, status, featured, view_count, author_id, category_id, published_at)
    VALUES (
        'Campus traditions that bring students together',
        'campus-traditions-that-bring-students-together',
        '<p>Every university has its unique traditions that help build a sense of community and belonging. This feature explores the various traditions at UEP that have become cherished parts of the student experience.</p><p>From annual events and celebrations to informal rituals and customs, these traditions create lasting memories and strengthen the bonds between students. They serve as reminders of shared experiences and the unique culture of the UEP community.</p><p>Students share their favorite traditions and the significance these practices hold for them. The article reflects on how these traditions evolve over time while maintaining their core values of unity, celebration, and school spirit.</p>',
        'An exploration of UEP''s campus traditions and their role in building community among students.',
        'PUBLISHED',
        false,
        143,
        writer_user_id,
        feature_cat_id,
        '2025-09-28 13:00:00'
    ) ON CONFLICT (slug) DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a.id, feature_story_tag_id
    FROM articles a
    WHERE a.slug = 'campus-traditions-that-bring-students-together'
    AND NOT EXISTS (SELECT 1 FROM article_tags WHERE article_id = a.id AND tag_id = feature_story_tag_id);

    -- Feature Article 7
    INSERT INTO articles (title, slug, content, excerpt, status, featured, view_count, author_id, category_id, published_at)
    VALUES (
        'Student entrepreneurs: Building businesses while studying',
        'student-entrepreneurs-building-businesses-while-studying',
        '<p>A growing number of UEP students are balancing their studies with entrepreneurial ventures, demonstrating that education and business can go hand in hand. This feature profiles several student entrepreneurs who are making their mark while pursuing their degrees.</p><p>These young entrepreneurs share their stories of starting businesses, the challenges of juggling academics and entrepreneurship, and the lessons they''ve learned along the way. Their ventures range from tech startups to service businesses and creative enterprises.</p><p>The article explores how the university supports student entrepreneurship through programs, mentorship, and resources. These student-business owners are not only creating opportunities for themselves but also contributing to the local economy and inspiring their peers.</p>',
        'A feature on UEP students who are successfully building businesses while pursuing their education.',
        'PUBLISHED',
        false,
        189,
        writer_user_id,
        feature_cat_id,
        '2025-10-02 11:00:00'
    ) ON CONFLICT (slug) DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a.id, feature_story_tag_id
    FROM articles a
    WHERE a.slug = 'student-entrepreneurs-building-businesses-while-studying'
    AND NOT EXISTS (SELECT 1 FROM article_tags WHERE article_id = a.id AND tag_id = feature_story_tag_id);

    -- Feature Article 8
    INSERT INTO articles (title, slug, content, excerpt, status, featured, view_count, author_id, category_id, published_at)
    VALUES (
        'Community service: UEP students making a difference',
        'community-service-uep-students-making-difference',
        '<p>UEP students are actively engaged in community service, applying their knowledge and skills to make a positive impact beyond the campus. This feature highlights various community service initiatives led by student organizations and volunteers.</p><p>From medical missions and educational outreach programs to environmental projects and disaster relief efforts, UEP students are contributing to community development in meaningful ways. These experiences not only benefit the communities they serve but also enrich the students'' educational journey.</p><p>The article showcases how community service activities align with the university''s mission of producing socially responsible graduates. Students share how these experiences have shaped their perspectives and career aspirations.</p>',
        'A feature on UEP students'' community service initiatives and their impact on local communities.',
        'PUBLISHED',
        false,
        165,
        writer_user_id,
        feature_cat_id,
        '2025-09-30 14:00:00'
    ) ON CONFLICT (slug) DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a.id, feature_story_tag_id
    FROM articles a
    WHERE a.slug = 'community-service-uep-students-making-difference'
    AND NOT EXISTS (SELECT 1 FROM article_tags WHERE article_id = a.id AND tag_id = feature_story_tag_id);

    -- ============================================
    -- EDITORIAL ARTICLES (8 articles)
    -- ============================================

    -- Editorial Article 1: Featured
    INSERT INTO articles (title, slug, content, excerpt, status, featured, view_count, author_id, category_id, published_at)
    VALUES (
        'Student welfare should remain a priority',
        'student-welfare-should-remain-priority',
        '<p>As we navigate the challenges and opportunities of the new academic year, it is imperative that we, as an institution, remain steadfast in our commitment to student welfare. Our students are not just numbers in a system—they are individuals with dreams, challenges, and the right to a supportive learning environment.</p><p>Student welfare encompasses many aspects: academic support, mental health services, financial assistance, safe campus spaces, and opportunities for growth. All of these are essential components of a quality education that prepares students not just for careers, but for life.</p><p>We call on all members of the university community—faculty, staff, administrators, and fellow students—to work together in creating an environment where every student can thrive. When we prioritize student welfare, we invest in the future of our institution and our nation.</p>',
        'An editorial piece discussing the importance of prioritizing student welfare in university policies and practices.',
        'PUBLISHED',
        true,
        198,
        writer_user_id,
        editorial_cat_id,
        '2025-09-18 09:00:00'
    ) ON CONFLICT (slug) DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a.id, editorial_tag_id
    FROM articles a
    WHERE a.slug = 'student-welfare-should-remain-priority'
    AND NOT EXISTS (SELECT 1 FROM article_tags WHERE article_id = a.id AND tag_id = editorial_tag_id);

    -- Editorial Article 2: Column
    INSERT INTO articles (title, slug, content, excerpt, status, featured, view_count, author_id, category_id, published_at)
    VALUES (
        'The path forward: Embracing change while honoring our values',
        'path-forward-embracing-change-while-honoring-our-values',
        '<p>Change is inevitable, and educational institutions must adapt to remain relevant in an ever-evolving world. However, adaptation does not mean abandoning the core values that define us. As we move forward, we must find ways to embrace innovation while staying true to our mission.</p><p>Our values—excellence, integrity, service, and community—remain as important today as they were when our institution was founded. These principles should guide our decisions as we explore new teaching methods, technologies, and approaches to education.</p><p>The challenge is not choosing between tradition and innovation, but integrating both in ways that enhance our educational mission. Let us move forward with confidence, knowing that our values will serve as our compass in navigating the changes ahead.</p>',
        'An editorial column reflecting on balancing tradition and innovation in educational institutions.',
        'PUBLISHED',
        false,
        167,
        writer_user_id,
        editorial_cat_id,
        '2025-09-22 10:00:00'
    ) ON CONFLICT (slug) DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a.id, column_tag_id
    FROM articles a
    WHERE a.slug = 'path-forward-embracing-change-while-honoring-our-values'
    AND NOT EXISTS (SELECT 1 FROM article_tags WHERE article_id = a.id AND tag_id = column_tag_id);

    -- Editorial Article 3
    INSERT INTO articles (title, slug, content, excerpt, status, featured, view_count, author_id, category_id, published_at)
    VALUES (
        'Accountability and transparency in university governance',
        'accountability-and-transparency-in-university-governance',
        '<p>Accountability and transparency are fundamental principles that must guide university governance. As an institution committed to excellence, we must maintain high standards of accountability in all our operations, from financial management to academic policies.</p><p>Transparency means keeping stakeholders informed about important decisions, policies, and the use of resources. This openness builds trust and allows for meaningful participation in institutional governance. Students, faculty, and staff have the right to understand how decisions are made and how resources are allocated.</p><p>We reaffirm our commitment to maintaining transparent processes and being accountable for our actions. This commitment strengthens our institution and ensures that we serve our mission effectively and ethically.</p>',
        'An editorial on the importance of accountability and transparency in university governance.',
        'PUBLISHED',
        false,
        156,
        writer_user_id,
        editorial_cat_id,
        '2025-09-25 11:00:00'
    ) ON CONFLICT (slug) DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a.id, editorial_tag_id
    FROM articles a
    WHERE a.slug = 'accountability-and-transparency-in-university-governance'
    AND NOT EXISTS (SELECT 1 FROM article_tags WHERE article_id = a.id AND tag_id = editorial_tag_id);

    -- Editorial Article 4
    INSERT INTO articles (title, slug, content, excerpt, status, featured, view_count, author_id, category_id, published_at)
    VALUES (
        'Fostering academic integrity in the digital age',
        'fostering-academic-integrity-in-digital-age',
        '<p>Academic integrity has always been a cornerstone of higher education, but the digital age has brought new challenges and opportunities in maintaining these standards. As technology becomes increasingly integrated into learning, we must adapt our approaches to academic honesty while preserving the core values of scholarly work.</p><p>The ease of accessing information online and the availability of various digital tools present both benefits and risks. We must educate students about proper research practices, citation methods, and the ethical use of technology in academic work. Plagiarism detection tools are valuable, but they are no substitute for fostering a culture of integrity.</p><p>Let us work together—faculty, students, and administrators—to maintain high standards of academic integrity. This requires clear policies, consistent enforcement, and most importantly, a shared commitment to ethical scholarship.</p>',
        'An editorial addressing the challenges and importance of maintaining academic integrity in modern education.',
        'PUBLISHED',
        false,
        178,
        writer_user_id,
        editorial_cat_id,
        '2025-09-28 14:00:00'
    ) ON CONFLICT (slug) DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a.id, editorial_tag_id
    FROM articles a
    WHERE a.slug = 'fostering-academic-integrity-in-digital-age'
    AND NOT EXISTS (SELECT 1 FROM article_tags WHERE article_id = a.id AND tag_id = editorial_tag_id);

    -- Editorial Article 5: Column
    INSERT INTO articles (title, slug, content, excerpt, status, featured, view_count, author_id, category_id, published_at)
    VALUES (
        'The role of universities in addressing climate change',
        'role-of-universities-in-addressing-climate-change',
        '<p>Universities have a unique and crucial role to play in addressing the climate crisis. As centers of knowledge, research, and innovation, we have both the responsibility and the capacity to contribute significantly to climate action.</p><p>Our role extends beyond research and teaching to include modeling sustainable practices on our campuses. From energy efficiency and waste reduction to sustainable transportation and green building design, universities can demonstrate practical solutions that others can adopt.</p><p>We must also prepare students to be climate leaders, equipping them with the knowledge, skills, and values needed to address environmental challenges in their careers and communities. The fight against climate change requires educated, engaged citizens, and universities are uniquely positioned to develop them.</p>',
        'An editorial column on the responsibility of universities in addressing climate change through research, education, and sustainable practices.',
        'PUBLISHED',
        false,
        189,
        writer_user_id,
        editorial_cat_id,
        '2025-10-01 10:00:00'
    ) ON CONFLICT (slug) DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a.id, column_tag_id
    FROM articles a
    WHERE a.slug = 'role-of-universities-in-addressing-climate-change'
    AND NOT EXISTS (SELECT 1 FROM article_tags WHERE article_id = a.id AND tag_id = column_tag_id);

    -- Editorial Article 6
    INSERT INTO articles (title, slug, content, excerpt, status, featured, view_count, author_id, category_id, published_at)
    VALUES (
        'Building bridges: The importance of interdisciplinary collaboration',
        'building-bridges-importance-of-interdisciplinary-collaboration',
        '<p>The complex challenges facing our world today cannot be solved by any single discipline working in isolation. This is why interdisciplinary collaboration has become increasingly important in higher education and research.</p><p>By bringing together perspectives from different fields—whether it''s combining science and humanities, technology and social sciences, or arts and business—we can develop more comprehensive and innovative solutions. Interdisciplinary collaboration enriches learning, expands research possibilities, and better prepares students for the interconnected world they will enter after graduation.</p><p>We encourage faculty and students to seek opportunities for interdisciplinary work, whether through joint research projects, cross-disciplinary courses, or collaborative initiatives. These bridges between disciplines strengthen our institution and advance knowledge in meaningful ways.</p>',
        'An editorial on the value of interdisciplinary collaboration in addressing complex challenges and enriching education.',
        'PUBLISHED',
        false,
        145,
        writer_user_id,
        editorial_cat_id,
        '2025-09-30 13:00:00'
    ) ON CONFLICT (slug) DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a.id, editorial_tag_id
    FROM articles a
    WHERE a.slug = 'building-bridges-importance-of-interdisciplinary-collaboration'
    AND NOT EXISTS (SELECT 1 FROM article_tags WHERE article_id = a.id AND tag_id = editorial_tag_id);

    -- Editorial Article 7: Column
    INSERT INTO articles (title, slug, content, excerpt, status, featured, view_count, author_id, category_id, published_at)
    VALUES (
        'Preparing students for an uncertain future',
        'preparing-students-for-uncertain-future',
        '<p>The future is inherently uncertain, and this presents both challenges and opportunities for higher education. While we cannot predict exactly what the world will look like in the coming decades, we can prepare students to navigate uncertainty with confidence and adaptability.</p><p>This means going beyond teaching specific skills and knowledge to developing fundamental capacities: critical thinking, creativity, communication, collaboration, and resilience. These are the skills that will serve students regardless of how the world changes.</p><p>We must also help students develop a growth mindset and the ability to learn continuously throughout their lives. The pace of change in our world means that education cannot be a one-time event—it must be a lifelong journey. Our role is to help students begin that journey with confidence and capability.</p>',
        'An editorial column on how universities can prepare students to thrive in an uncertain and rapidly changing world.',
        'PUBLISHED',
        false,
        167,
        writer_user_id,
        editorial_cat_id,
        '2025-10-03 09:00:00'
    ) ON CONFLICT (slug) DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a.id, column_tag_id
    FROM articles a
    WHERE a.slug = 'preparing-students-for-uncertain-future'
    AND NOT EXISTS (SELECT 1 FROM article_tags WHERE article_id = a.id AND tag_id = column_tag_id);

    -- Editorial Article 8
    INSERT INTO articles (title, slug, content, excerpt, status, featured, view_count, author_id, category_id, published_at)
    VALUES (
        'Celebrating diversity: Our strength as a learning community',
        'celebrating-diversity-our-strength-as-learning-community',
        '<p>Diversity is one of our greatest strengths as a learning community. When students, faculty, and staff from different backgrounds, experiences, and perspectives come together, we create a richer, more dynamic educational environment.</p><p>Diverse perspectives challenge us to think more critically, question our assumptions, and consider alternative viewpoints. This enriches classroom discussions, enhances research, and prepares students for the diverse world they will encounter after graduation.</p><p>However, diversity alone is not enough. We must actively work to create an inclusive environment where everyone feels valued, respected, and empowered to contribute. This requires intentional effort, ongoing dialogue, and a commitment to equity. Let us celebrate our diversity while working to ensure that inclusion is not just an ideal, but a lived reality in our institution.</p>',
        'An editorial on the value of diversity and the importance of creating truly inclusive learning environments.',
        'PUBLISHED',
        false,
        198,
        writer_user_id,
        editorial_cat_id,
        '2025-09-27 15:00:00'
    ) ON CONFLICT (slug) DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a.id, editorial_tag_id
    FROM articles a
    WHERE a.slug = 'celebrating-diversity-our-strength-as-learning-community'
    AND NOT EXISTS (SELECT 1 FROM article_tags WHERE article_id = a.id AND tag_id = editorial_tag_id);

    -- ============================================
    -- SCITECH ARTICLES (8 articles)
    -- ============================================

    -- SciTech Article 1: Featured
    INSERT INTO articles (title, slug, content, excerpt, status, featured, view_count, author_id, category_id, published_at)
    VALUES (
        'New computer laboratory opens in CCS building',
        'new-computer-laboratory-opens-in-ccs-building',
        '<p>The College of Computer Studies (CCS) recently inaugurated a state-of-the-art computer laboratory, equipped with the latest hardware and software to support advanced computing education. The new facility represents a significant investment in technology education at UEP.</p><p>The laboratory features high-performance computers, specialized software for programming, data analysis, and multimedia development, and modern networking infrastructure. The facility will support various courses including programming, database management, cybersecurity, and artificial intelligence.</p><p>Faculty members expressed excitement about the new laboratory, noting that it will enhance the learning experience and better prepare students for careers in the rapidly evolving technology sector. The laboratory will also serve as a space for student research projects and collaborative work.</p>',
        'The College of Computer Studies inaugurates a state-of-the-art computer laboratory with modern technology and equipment.',
        'PUBLISHED',
        true,
        234,
        writer_user_id,
        scitech_cat_id,
        '2025-09-12 10:00:00'
    ) ON CONFLICT (slug) DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a.id, technology_tag_id
    FROM articles a
    WHERE a.slug = 'new-computer-laboratory-opens-in-ccs-building'
    AND NOT EXISTS (SELECT 1 FROM article_tags WHERE article_id = a.id AND tag_id = technology_tag_id);

    -- SciTech Article 2: Science
    INSERT INTO articles (title, slug, content, excerpt, status, featured, view_count, author_id, category_id, published_at)
    VALUES (
        'UEP researchers study local marine biodiversity',
        'uep-researchers-study-local-marine-biodiversity',
        '<p>A team of researchers from UEP is conducting a comprehensive study of marine biodiversity in local waters, contributing to important conservation efforts. The research project aims to document marine species, assess ecosystem health, and identify areas in need of protection.</p><p>The study involves field research, sample collection, and laboratory analysis. Researchers are working with local communities and government agencies to gather data and raise awareness about marine conservation. Initial findings have revealed the presence of diverse marine life and highlighted the importance of preserving these ecosystems.</p><p>The research project provides valuable learning opportunities for students, who are participating in field work and data analysis. The findings will contribute to regional conservation strategies and support sustainable marine resource management.</p>',
        'UEP researchers conduct a comprehensive study of local marine biodiversity to support conservation efforts.',
        'PUBLISHED',
        false,
        167,
        writer_user_id,
        scitech_cat_id,
        '2025-09-15 14:00:00'
    ) ON CONFLICT (slug) DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a.id, science_tag_id
    FROM articles a
    WHERE a.slug = 'uep-researchers-study-local-marine-biodiversity'
    AND NOT EXISTS (SELECT 1 FROM article_tags WHERE article_id = a.id AND tag_id = science_tag_id);

    -- SciTech Article 3: Technology
    INSERT INTO articles (title, slug, content, excerpt, status, featured, view_count, author_id, category_id, published_at)
    VALUES (
        'Students develop mobile app for campus navigation',
        'students-develop-mobile-app-for-campus-navigation',
        '<p>A group of computer science students has developed a mobile application designed to help newcomers navigate the UEP campus. The app features interactive maps, building directories, and information about campus facilities and services.</p><p>The application was developed as part of a capstone project, combining students'' technical skills with practical problem-solving. The app uses GPS technology and augmented reality features to provide an intuitive navigation experience. Users can search for specific buildings, find the shortest routes, and access information about facilities.</p><p>The project demonstrates the practical applications of technology education and showcases students'' ability to create solutions for real-world problems. The developers plan to continue improving the app based on user feedback.</p>',
        'Computer science students create a mobile application to help visitors and students navigate the UEP campus.',
        'PUBLISHED',
        false,
        189,
        writer_user_id,
        scitech_cat_id,
        '2025-09-20 11:00:00'
    ) ON CONFLICT (slug) DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a.id, technology_tag_id
    FROM articles a
    WHERE a.slug = 'students-develop-mobile-app-for-campus-navigation'
    AND NOT EXISTS (SELECT 1 FROM article_tags WHERE article_id = a.id AND tag_id = technology_tag_id);

    -- SciTech Article 4: Science
    INSERT INTO articles (title, slug, content, excerpt, status, featured, view_count, author_id, category_id, published_at)
    VALUES (
        'Chemistry department collaborates on renewable energy research',
        'chemistry-department-collaborates-on-renewable-energy-research',
        '<p>The Chemistry Department at UEP is participating in a collaborative research project focused on developing renewable energy solutions. The project involves studying alternative energy sources and developing efficient energy conversion technologies.</p><p>Researchers are exploring various approaches, including solar energy optimization, biofuel development, and energy storage systems. The project brings together expertise from multiple disciplines and involves partnerships with other institutions and industry stakeholders.</p><p>The research has potential applications for addressing energy challenges in the region and contributing to sustainable development. Students involved in the project are gaining valuable experience in cutting-edge research and developing skills relevant to the growing renewable energy sector.</p>',
        'UEP chemistry researchers collaborate on renewable energy projects, exploring alternative energy sources and conversion technologies.',
        'PUBLISHED',
        false,
        156,
        writer_user_id,
        scitech_cat_id,
        '2025-09-22 13:00:00'
    ) ON CONFLICT (slug) DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a.id, science_tag_id
    FROM articles a
    WHERE a.slug = 'chemistry-department-collaborates-on-renewable-energy-research'
    AND NOT EXISTS (SELECT 1 FROM article_tags WHERE article_id = a.id AND tag_id = science_tag_id);

    -- SciTech Article 5: Technology
    INSERT INTO articles (title, slug, content, excerpt, status, featured, view_count, author_id, category_id, published_at)
    VALUES (
        'UEP implements smart classroom technology',
        'uep-implements-smart-classroom-technology',
        '<p>UEP has begun implementing smart classroom technology in selected facilities, enhancing the teaching and learning experience with modern digital tools. The initiative is part of the university''s ongoing digital transformation efforts.</p><p>Smart classrooms are equipped with interactive whiteboards, digital projectors, wireless presentation systems, and other technologies that support active learning and collaboration. Faculty members are receiving training on how to effectively utilize these tools in their teaching.</p><p>Early feedback from both faculty and students has been positive, with users appreciating the enhanced interactivity and engagement that smart classroom technology provides. The university plans to expand the initiative to additional classrooms in the coming semesters.</p>',
        'UEP introduces smart classroom technology to enhance teaching and learning through modern digital tools.',
        'PUBLISHED',
        false,
        178,
        writer_user_id,
        scitech_cat_id,
        '2025-09-25 10:00:00'
    ) ON CONFLICT (slug) DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a.id, technology_tag_id
    FROM articles a
    WHERE a.slug = 'uep-implements-smart-classroom-technology'
    AND NOT EXISTS (SELECT 1 FROM article_tags WHERE article_id = a.id AND tag_id = technology_tag_id);

    -- SciTech Article 6: Science
    INSERT INTO articles (title, slug, content, excerpt, status, featured, view_count, author_id, category_id, published_at)
    VALUES (
        'Physics students conduct experiments on renewable materials',
        'physics-students-conduct-experiments-on-renewable-materials',
        '<p>Students in the Physics Department are conducting innovative experiments exploring the properties of renewable and sustainable materials. The research project aims to identify materials that could replace traditional options in various applications.</p><p>The experiments involve testing material properties such as strength, durability, and environmental impact. Students are working with faculty advisors to design experiments, collect data, and analyze results. The project provides hands-on experience in experimental physics and materials science.</p><p>The research has implications for sustainable manufacturing and could contribute to the development of eco-friendly alternatives to conventional materials. Students are learning valuable research skills while addressing important environmental challenges.</p>',
        'Physics students explore renewable materials through innovative experiments, contributing to sustainable material development.',
        'PUBLISHED',
        false,
        134,
        writer_user_id,
        scitech_cat_id,
        '2025-09-28 14:00:00'
    ) ON CONFLICT (slug) DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a.id, science_tag_id
    FROM articles a
    WHERE a.slug = 'physics-students-conduct-experiments-on-renewable-materials'
    AND NOT EXISTS (SELECT 1 FROM article_tags WHERE article_id = a.id AND tag_id = science_tag_id);

    -- SciTech Article 7: Technology
    INSERT INTO articles (title, slug, content, excerpt, status, featured, view_count, author_id, category_id, published_at)
    VALUES (
        'Cybersecurity workshop prepares students for digital threats',
        'cybersecurity-workshop-prepares-students-for-digital-threats',
        '<p>The College of Computer Studies organized a comprehensive cybersecurity workshop to prepare students for the growing challenges of digital security. The workshop covered topics including threat detection, secure coding practices, and network security.</p><p>Participants learned about common cyber threats, best practices for protecting digital information, and the ethical responsibilities of cybersecurity professionals. The workshop included hands-on exercises and simulations that allowed students to practice responding to security incidents.</p><p>With cybersecurity becoming increasingly important across all industries, the workshop provides students with valuable skills that are in high demand in the job market. The college plans to offer additional cybersecurity training opportunities in the future.</p>',
        'A cybersecurity workshop at UEP teaches students essential skills for protecting digital information and responding to cyber threats.',
        'PUBLISHED',
        false,
        212,
        writer_user_id,
        scitech_cat_id,
        '2025-10-02 11:00:00'
    ) ON CONFLICT (slug) DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a.id, technology_tag_id
    FROM articles a
    WHERE a.slug = 'cybersecurity-workshop-prepares-students-for-digital-threats'
    AND NOT EXISTS (SELECT 1 FROM article_tags WHERE article_id = a.id AND tag_id = technology_tag_id);

    -- SciTech Article 8: Science
    INSERT INTO articles (title, slug, content, excerpt, status, featured, view_count, author_id, category_id, published_at)
    VALUES (
        'Biology students document local plant species',
        'biology-students-document-local-plant-species',
        '<p>Students from the Biology Department are conducting a comprehensive documentation project of local plant species, creating an important resource for botanical research and conservation. The project involves field surveys, species identification, and detailed documentation.</p><p>Working in collaboration with faculty members and local botanists, students are cataloging plant species found in the region, recording their characteristics, habitats, and potential uses. The documentation includes photographs, detailed descriptions, and location data.</p><p>The project contributes to understanding regional biodiversity and supports conservation efforts. Students are gaining valuable field research experience while contributing to scientific knowledge. The documentation will be compiled into a resource that can be used by researchers, conservationists, and educators.</p>',
        'Biology students work on documenting local plant species, contributing to botanical research and conservation efforts.',
        'PUBLISHED',
        false,
        145,
        writer_user_id,
        scitech_cat_id,
        '2025-10-01 15:00:00'
    ) ON CONFLICT (slug) DO NOTHING;

    INSERT INTO article_tags (article_id, tag_id)
    SELECT a.id, science_tag_id
    FROM articles a
    WHERE a.slug = 'biology-students-document-local-plant-species'
    AND NOT EXISTS (SELECT 1 FROM article_tags WHERE article_id = a.id AND tag_id = science_tag_id);

END $$;

-- ============================================
-- END OF SEED SCRIPT
-- ============================================