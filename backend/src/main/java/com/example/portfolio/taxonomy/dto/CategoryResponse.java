package com.example.portfolio.taxonomy.dto;

import com.example.portfolio.taxonomy.TaxonomyStatus;

public record CategoryResponse(
        Long id,
        String name,
        String slug,
        String description,
        TaxonomyStatus status,
        int displayOrder) {
}
