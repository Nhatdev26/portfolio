package com.example.portfolio.profile.dto;

import com.example.portfolio.profile.ProfileContentStatus;
import com.example.portfolio.profile.ProfileLanguage;

public record ProfileContentResponse(
        Long id,
        ProfileLanguage language,
        String headline,
        String subheadline,
        String shortBio,
        String longBio,
        String seoTitle,
        String seoDescription,
        ProfileContentStatus status) {
}
