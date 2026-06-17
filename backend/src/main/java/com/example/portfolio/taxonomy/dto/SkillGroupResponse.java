package com.example.portfolio.taxonomy.dto;

import com.example.portfolio.taxonomy.TaxonomyStatus;
import java.util.List;

public record SkillGroupResponse(
        Long id,
        String name,
        String slug,
        String description,
        TaxonomyStatus status,
        int displayOrder,
        List<TechnologyResponse> technologies) {
}
