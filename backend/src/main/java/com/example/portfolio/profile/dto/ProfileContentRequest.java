package com.example.portfolio.profile.dto;

import com.example.portfolio.profile.ProfileContentStatus;
import com.example.portfolio.profile.ProfileLanguage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProfileContentRequest(
        Long id,
        @NotNull ProfileLanguage language,
        @NotBlank @Size(max = 255) String headline,
        @Size(max = 255) String subheadline,
        String shortBio,
        String longBio,
        @Size(max = 255) String seoTitle,
        @Size(max = 320) String seoDescription,
        @NotNull ProfileContentStatus status) {
}
