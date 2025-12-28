package com.uep.pillar.service;

import com.uep.pillar.dto.ArticleStatistics;
import com.uep.pillar.dto.PublicationStatistics;
import com.uep.pillar.model.enums.ArticleStatus;
import com.uep.pillar.repository.ArticleRepository;
import com.uep.pillar.repository.MediaRepository;
import com.uep.pillar.repository.PublicationIssueRepository;
import com.uep.pillar.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final ArticleRepository articleRepository;
    private final PublicationIssueRepository publicationIssueRepository;
    private final UserRepository userRepository;
    private final MediaRepository mediaRepository;

    @Transactional(readOnly = true)
    public ArticleStatistics getArticleStatistics() {
        long totalArticles = articleRepository.count();
        long publishedArticles = articleRepository.countByStatus(ArticleStatus.PUBLISHED);
        long draftArticles = articleRepository.countByStatus(ArticleStatus.DRAFT);
        long totalViews = articleRepository.sumViewCount();

        float avgViewsPerArticle = totalArticles > 0
                ? (float) totalViews / totalArticles
                : 0f;

        return ArticleStatistics.builder()
                .totalArticles((int) totalArticles)
                .publishedArticles((int) publishedArticles)
                .draftArticles((int) draftArticles)
                .totalViews(totalViews)
                .avgViewsPerArticle(avgViewsPerArticle)
                .build();
    }

    @Transactional(readOnly = true)
    public PublicationStatistics getPublicationStatistics() {
        long totalIssues = publicationIssueRepository.count();
        long totalArticles = articleRepository.count();
        long totalUsers = userRepository.count();
        long totalMedia = mediaRepository.count();

        return PublicationStatistics.builder()
                .totalIssues((int) totalIssues)
                .totalArticles((int) totalArticles)
                .totalUsers((int) totalUsers)
                .totalMedia((int) totalMedia)
                .build();
    }
}
