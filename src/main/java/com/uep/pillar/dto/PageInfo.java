package com.uep.pillar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Pagination information for cursor-based pagination.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageInfo {
    private Boolean hasNextPage;
    private Boolean hasPreviousPage;
    private String startCursor;
    private String endCursor;
}

