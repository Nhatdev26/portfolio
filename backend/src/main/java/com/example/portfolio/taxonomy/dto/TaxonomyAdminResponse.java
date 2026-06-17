package com.example.portfolio.taxonomy.dto;

import java.util.List;

public record TaxonomyAdminResponse(
        List<CategoryResponse> categories,
        List<TagResponse> tags,
        List<TechnologyResponse> technologies,
        List<SkillGroupResponse> skillGroups) {
}
