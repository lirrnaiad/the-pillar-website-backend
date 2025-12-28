package com.uep.pillar.service;

import com.uep.pillar.model.ArticleRevision;
import com.uep.pillar.repository.ArticleRevisionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArticleRevisionService {

    private final ArticleRevisionRepository articleRevisionRepository;

    @Transactional(readOnly = true)
    public List<ArticleRevision> findByArticleId(Long articleId) {
        return articleRevisionRepository.findByArticleId(articleId);
    }
}
