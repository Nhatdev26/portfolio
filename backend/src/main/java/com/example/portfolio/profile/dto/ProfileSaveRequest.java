package com.example.portfolio.profile.dto;

import com.example.portfolio.profile.ProfileStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ProfileSaveRequest(
        Long id,
        @NotBlank @Size(max = 160) String displayName,
        @NotBlank @Email @Size(max = 320) String email,
        @Size(max = 160) String location,
        @NotBlank @Size(max = 160) String primaryRole,
        @Size(max = 255) String careerDirection,
        @Size(max = 255) String mainTechFocus,
        @NotNull ProfileStatus status,
        @Valid List<ProfileContentRequest> contents,
        @Valid List<SocialLinkRequest> socialLinks) {
}
