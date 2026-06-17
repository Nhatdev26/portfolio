package com.example.portfolio.profile.dto;

import com.example.portfolio.profile.SocialLinkPlatform;
import com.example.portfolio.profile.SocialLinkStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SocialLinkRequest(
        Long id,
        @NotNull SocialLinkPlatform platform,
        @NotBlank @Size(max = 120) String label,
        @NotBlank @Size(max = 2048) String url,
        int displayOrder,
        @NotNull SocialLinkStatus status) {
}
