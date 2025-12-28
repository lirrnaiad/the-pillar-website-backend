package com.uep.pillar.dto;

import com.uep.pillar.model.enums.ArticleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateArticleInput {
    private String title;
    private String slug;
    private String content;
    private String excerpt;
    private String categoryId;
    private String coverId;
    private String issueId;
    private List<String> tagIds;
    private String metaTitle;
    private String metaDescription;
    private String ogImage;
    private ArticleStatus status;
}
