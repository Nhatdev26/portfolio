package com.example.portfolio.profile.dto;

import com.example.portfolio.profile.ProfileLanguage;
import java.util.List;

public record PublicProfileResponse(
        String displayName,
        String email,
        String location,
        String primaryRole,
        String careerDirection,
        String mainTechFocus,
        ProfileLanguage language,
        String headline,
        String subheadline,
        String shortBio,
        String longBio,
        List<SocialLinkResponse> socialLinks) {
}
