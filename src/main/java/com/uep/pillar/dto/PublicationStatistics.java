package com.uep.pillar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicationStatistics {
    private int totalIssues;
    private int totalArticles;
    private int totalUsers;
    private int totalMedia;
}
