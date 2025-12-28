package com.uep.pillar.service;

import com.uep.pillar.dto.SearchHighlight;
import com.uep.pillar.model.Article;
import com.uep.pillar.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final ArticleRepository articleRepository;

    /**
     * Full-text search across published articles using PostgreSQL tsvector.
     * Ranked by ts_rank and ordered DESC.
     */
    @Transactional(readOnly = true)
    public Page<Article> searchPublished(String query, Pageable pageable) {
        return articleRepository.search(query, pageable);
    }

    /**
     * Full-text search with highlighted snippets for preview cards.
     */
    @Transactional(readOnly = true)
    public List<SearchHighlight> searchWithHighlights(String query, int limit) {
        List<Object[]> rows = articleRepository.searchWithHighlights(query, Math.max(limit, 1));
        List<SearchHighlight> results = new ArrayList<>(rows.size());
        for (Object[] r : rows) {
            Long id = r[0] != null ? ((Number) r[0]).longValue() : null;
            String title = (String) r[1];
            String snippet = (String) r[2];
            results.add(SearchHighlight.builder().id(id).title(title).snippet(snippet).build());
        }
        return results;
    }
}
