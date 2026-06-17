package com.example.portfolio.taxonomy.dto;

import com.example.portfolio.taxonomy.TaxonomyStatus;
import com.example.portfolio.taxonomy.TechnologyType;

public record TechnologyResponse(
        Long id,
        String name,
        String slug,
        TechnologyType type,
        TaxonomyStatus status,
        String description,
        String howIUseIt,
        boolean core,
        int displayOrder) {
}
