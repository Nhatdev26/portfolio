package com.example.portfolio.taxonomy.dto;

import com.example.portfolio.taxonomy.TaxonomyStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TagRequest(
        Long id,
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Size(max = 140) String slug,
        @NotNull TaxonomyStatus status,
        int displayOrder) {
}
