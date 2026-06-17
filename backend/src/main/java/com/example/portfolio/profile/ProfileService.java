package com.example.portfolio.profile;

import com.example.portfolio.audit.AuditService;
import com.example.portfolio.common.exception.ApiException;
import com.example.portfolio.profile.dto.ProfileAdminResponse;
import com.example.portfolio.profile.dto.ProfileContentRequest;
import com.example.portfolio.profile.dto.ProfileContentResponse;
import com.example.portfolio.profile.dto.ProfileSaveRequest;
import com.example.portfolio.profile.dto.PublicProfileResponse;
import com.example.portfolio.profile.dto.SocialLinkRequest;
import com.example.portfolio.profile.dto.SocialLinkResponse;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Clock;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final ProfileContentRepository contentRepository;
    private final SocialLinkRepository socialLinkRepository;
    private final AuditService auditService;
    private final Clock clock;

    public ProfileService(
            ProfileRepository profileRepository,
            ProfileContentRepository contentRepository,
            SocialLinkRepository socialLinkRepository,
            AuditService auditService,
            Clock clock) {
        this.profileRepository = profileRepository;
        this.contentRepository = contentRepository;
        this.socialLinkRepository = socialLinkRepository;
        this.auditService = auditService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public ProfileAdminResponse getAdminProfile() {
        return profileRepository.findFirstByDeletedAtIsNullOrderByIdAsc()
                .map(this::toAdminResponse)
                .orElseGet(ProfileAdminResponse::empty);
    }

    @Transactional
    public ProfileAdminResponse save(ProfileSaveRequest request) {
        Profile profile = profileRepository.findFirstByDeletedAtIsNullOrderByIdAsc()
                .orElseGet(Profile::new);
        ProfileAdminResponse oldValue = profile.getId() == null ? null : toAdminResponse(profile);
        profile.setDisplayName(request.displayName().trim());
        profile.setEmail(request.email().trim());
        profile.setLocation(blankToNull(request.location()));
        profile.setPrimaryRole(request.primaryRole().trim());
        profile.setCareerDirection(blankToNull(request.careerDirection()));
        profile.setMainTechFocus(blankToNull(request.mainTechFocus()));
        profile.setStatus(request.status());

        Profile savedProfile = profileRepository.save(profile);
        saveContents(savedProfile, request.contents() == null ? List.of() : request.contents());
        saveSocialLinks(savedProfile, request.socialLinks() == null ? List.of() : request.socialLinks());

        ProfileAdminResponse response = toAdminResponse(savedProfile);
        auditService.success("PROFILE_UPDATE", "PROFILE", response.id(), response.displayName(), oldValue, response);
        return response;
    }

    @Transactional(readOnly = true)
    public PublicProfileResponse getPublicProfile(ProfileLanguage language) {
        Profile profile = profileRepository.findFirstByStatusAndDeletedAtIsNullOrderByIdAsc(ProfileStatus.ACTIVE)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Active profile was not found."));
        ProfileContent content = contentRepository
                .findFirstByProfileIdAndLanguageAndStatusAndDeletedAtIsNullOrderByIdAsc(
                        profile.getId(),
                        language,
                        ProfileContentStatus.ACTIVE)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Active profile content was not found."));

        List<SocialLinkResponse> links = socialLinkRepository
                .findByProfileIdAndStatusAndDeletedAtIsNullOrderByDisplayOrderAscIdAsc(
                        profile.getId(),
                        SocialLinkStatus.ACTIVE)
                .stream()
                .map(this::toSocialLinkResponse)
                .toList();

        return new PublicProfileResponse(
                profile.getDisplayName(),
                profile.getEmail(),
                profile.getLocation(),
                profile.getPrimaryRole(),
                profile.getCareerDirection(),
                profile.getMainTechFocus(),
                content.getLanguage(),
                content.getHeadline(),
                content.getSubheadline(),
                content.getShortBio(),
                content.getLongBio(),
                links);
    }

    private void saveContents(Profile profile, List<ProfileContentRequest> requests) {
        Set<ProfileLanguage> seenLanguages = new HashSet<>();
        for (ProfileContentRequest request : requests) {
            if (!seenLanguages.add(request.language())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Duplicate profile content language: " + request.language());
            }
        }

        for (ProfileContentRequest request : requests) {
            ProfileContent content = contentRepository
                    .findFirstByProfileIdAndLanguageAndDeletedAtIsNullOrderByIdAsc(profile.getId(), request.language())
                    .orElseGet(ProfileContent::new);
            content.setProfile(profile);
            content.setLanguage(request.language());
            content.setHeadline(request.headline().trim());
            content.setSubheadline(blankToNull(request.subheadline()));
            content.setShortBio(blankToNull(request.shortBio()));
            content.setLongBio(blankToNull(request.longBio()));
            content.setSeoTitle(blankToNull(request.seoTitle()));
            content.setSeoDescription(blankToNull(request.seoDescription()));
            content.setStatus(request.status());
            contentRepository.save(content);
        }
    }

    private void saveSocialLinks(Profile profile, List<SocialLinkRequest> requests) {
        Set<Long> keptIds = new HashSet<>();
        List<SocialLink> existing = socialLinkRepository
                .findByProfileIdAndDeletedAtIsNullOrderByDisplayOrderAscIdAsc(profile.getId());

        for (SocialLinkRequest request : requests) {
            validateUrl(request.url(), request.platform());
            SocialLink link = request.id() == null
                    ? new SocialLink()
                    : socialLinkRepository.findByIdAndProfileIdAndDeletedAtIsNull(request.id(), profile.getId())
                            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Social link was not found."));
            link.setProfile(profile);
            link.setPlatform(request.platform());
            link.setLabel(request.label().trim());
            link.setUrl(request.url().trim());
            link.setDisplayOrder(request.displayOrder());
            link.setStatus(request.status());
            SocialLink savedLink = socialLinkRepository.save(link);
            keptIds.add(savedLink.getId());
        }

        for (SocialLink link : existing) {
            if (!keptIds.contains(link.getId())) {
                link.setDeletedAt(clock.instant());
                socialLinkRepository.save(link);
            }
        }
    }

    private ProfileAdminResponse toAdminResponse(Profile profile) {
        List<ProfileContentResponse> contents = contentRepository
                .findByProfileIdAndDeletedAtIsNullOrderByLanguageAsc(profile.getId())
                .stream()
                .map(this::toContentResponse)
                .toList();
        List<SocialLinkResponse> socialLinks = socialLinkRepository
                .findByProfileIdAndDeletedAtIsNullOrderByDisplayOrderAscIdAsc(profile.getId())
                .stream()
                .map(this::toSocialLinkResponse)
                .toList();

        return new ProfileAdminResponse(
                profile.getId(),
                profile.getDisplayName(),
                profile.getEmail(),
                profile.getLocation(),
                profile.getPrimaryRole(),
                profile.getCareerDirection(),
                profile.getMainTechFocus(),
                profile.getStatus(),
                contents,
                socialLinks);
    }

    private ProfileContentResponse toContentResponse(ProfileContent content) {
        return new ProfileContentResponse(
                content.getId(),
                content.getLanguage(),
                content.getHeadline(),
                content.getSubheadline(),
                content.getShortBio(),
                content.getLongBio(),
                content.getSeoTitle(),
                content.getSeoDescription(),
                content.getStatus());
    }

    private SocialLinkResponse toSocialLinkResponse(SocialLink link) {
        return new SocialLinkResponse(
                link.getId(),
                link.getPlatform(),
                link.getLabel(),
                link.getUrl(),
                link.getDisplayOrder(),
                link.getStatus());
    }

    private void validateUrl(String rawUrl, SocialLinkPlatform platform) {
        try {
            URI uri = new URI(rawUrl.trim());
            String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
            boolean validScheme = switch (platform) {
                case EMAIL -> scheme.equals("mailto");
                case GITHUB, LINKEDIN, PORTFOLIO, OTHER -> scheme.equals("http") || scheme.equals("https");
            };
            if (!validScheme || (platform != SocialLinkPlatform.EMAIL && uri.getHost() == null)) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Social link URL is invalid.");
            }
        } catch (URISyntaxException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Social link URL is invalid.");
        }
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
