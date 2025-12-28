package com.uep.pillar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePublicationIssueInput {
    private String title;
    private String slug;
    private String coverUrl;
    private String description;
    private LocalDateTime publishedAt;
}
