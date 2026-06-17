package com.example.portfolio.profile.dto;

import com.example.portfolio.profile.SocialLinkPlatform;
import com.example.portfolio.profile.SocialLinkStatus;

public record SocialLinkResponse(
        Long id,
        SocialLinkPlatform platform,
        String label,
        String url,
        int displayOrder,
        SocialLinkStatus status) {
}
