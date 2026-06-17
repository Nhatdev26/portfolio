package com.example.portfolio.taxonomy.dto;

import com.example.portfolio.taxonomy.TaxonomyStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record SkillGroupRequest(
        Long id,
        @NotBlank @Size(max = 160) String name,
        @NotBlank @Size(max = 180) String slug,
        String description,
        @NotNull TaxonomyStatus status,
        int displayOrder,
        List<Long> technologyIds) {
}
