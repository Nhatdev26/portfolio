package com.example.portfolio.taxonomy.dto;

import com.example.portfolio.taxonomy.TaxonomyStatus;
import com.example.portfolio.taxonomy.TechnologyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TechnologyRequest(
        Long id,
        @NotBlank @Size(max = 160) String name,
        @NotBlank @Size(max = 180) String slug,
        @NotNull TechnologyType type,
        @NotNull TaxonomyStatus status,
        String description,
        String howIUseIt,
        boolean core,
        int displayOrder) {
}
