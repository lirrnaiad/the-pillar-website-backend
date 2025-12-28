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
public class UpdateArticleInput {
    private String id;
    private String title;
    private String slug;
    private String content;
    private String excerpt;
    private ArticleStatus status;
    private Boolean featured;
    private String categoryId;
    private String coverId;
    private String issueId;
    private List<String> tagIds;
    private String metaTitle;
    private String metaDescription;
    private String ogImage;
}
