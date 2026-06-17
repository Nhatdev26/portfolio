package com.example.portfolio.taxonomy.dto;

import com.example.portfolio.taxonomy.TaxonomyStatus;

public record TagResponse(
        Long id,
        String name,
        String slug,
        TaxonomyStatus status,
        int displayOrder) {
}
