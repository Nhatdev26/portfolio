package com.example.portfolio.profile.dto;

import com.example.portfolio.profile.ProfileStatus;
import java.util.List;

public record ProfileAdminResponse(
        Long id,
        String displayName,
        String email,
        String location,
        String primaryRole,
        String careerDirection,
        String mainTechFocus,
        ProfileStatus status,
        List<ProfileContentResponse> contents,
        List<SocialLinkResponse> socialLinks) {

    public static ProfileAdminResponse empty() {
        return new ProfileAdminResponse(null, "", "", "", "", "", "", ProfileStatus.DRAFT, List.of(), List.of());
    }
}
