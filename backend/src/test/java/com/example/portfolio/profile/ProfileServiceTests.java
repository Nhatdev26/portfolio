package com.example.portfolio.profile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.portfolio.audit.AuditService;
import com.example.portfolio.common.exception.ApiException;
import com.example.portfolio.profile.dto.ProfileContentRequest;
import com.example.portfolio.profile.dto.ProfileSaveRequest;
import com.example.portfolio.profile.dto.PublicProfileResponse;
import com.example.portfolio.profile.dto.SocialLinkRequest;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTests {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private ProfileContentRepository contentRepository;

    @Mock
    private SocialLinkRepository socialLinkRepository;

    @Mock
    private AuditService auditService;

    private ProfileService profileService;

    @BeforeEach
    void setUp() {
        profileService = new ProfileService(
                profileRepository,
                contentRepository,
                socialLinkRepository,
                auditService,
                CLOCK);
    }

    @Test
    void saveCreatesProfileContentAndSocialLinks() {
        List<ProfileContent> savedContents = new ArrayList<>();
        List<SocialLink> savedLinks = new ArrayList<>();
        when(profileRepository.findFirstByDeletedAtIsNullOrderByIdAsc()).thenReturn(Optional.empty());
        when(profileRepository.save(any(Profile.class))).thenAnswer(invocation -> {
            Profile profile = invocation.getArgument(0);
            ReflectionTestUtils.setField(profile, "id", 10L);
            return profile;
        });
        when(contentRepository.findFirstByProfileIdAndLanguageAndDeletedAtIsNullOrderByIdAsc(10L, ProfileLanguage.EN))
                .thenReturn(Optional.empty());
        when(contentRepository.save(any(ProfileContent.class))).thenAnswer(invocation -> {
            ProfileContent content = invocation.getArgument(0);
            ReflectionTestUtils.setField(content, "id", 20L);
            savedContents.add(content);
            return content;
        });
        when(contentRepository.findByProfileIdAndDeletedAtIsNullOrderByLanguageAsc(10L)).thenReturn(savedContents);
        when(socialLinkRepository.findByProfileIdAndDeletedAtIsNullOrderByDisplayOrderAscIdAsc(10L))
                .thenReturn(List.of())
                .thenReturn(savedLinks);
        when(socialLinkRepository.save(any(SocialLink.class))).thenAnswer(invocation -> {
            SocialLink link = invocation.getArgument(0);
            ReflectionTestUtils.setField(link, "id", 30L);
            savedLinks.add(link);
            return link;
        });

        ProfileSaveRequest request = new ProfileSaveRequest(
                null,
                " Nhat Nguyen ",
                " admin@example.com ",
                " Da Nang ",
                " Backend Engineer ",
                " Architecture ",
                " Spring Boot ",
                ProfileStatus.ACTIVE,
                List.of(activeContent(ProfileLanguage.EN, " Building reliable APIs ")),
                List.of(activeLink(SocialLinkPlatform.GITHUB, "GitHub", "https://github.com/Nhatdev26")));

        var response = profileService.save(request);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.displayName()).isEqualTo("Nhat Nguyen");
        assertThat(response.email()).isEqualTo("admin@example.com");
        assertThat(response.location()).isEqualTo("Da Nang");
        assertThat(response.primaryRole()).isEqualTo("Backend Engineer");
        assertThat(response.status()).isEqualTo(ProfileStatus.ACTIVE);
        assertThat(response.contents()).hasSize(1);
        assertThat(response.contents().getFirst().headline()).isEqualTo("Building reliable APIs");
        assertThat(response.socialLinks()).hasSize(1);
        assertThat(response.socialLinks().getFirst().url()).isEqualTo("https://github.com/Nhatdev26");
    }

    @Test
    void saveRejectsDuplicateContentLanguages() {
        when(profileRepository.findFirstByDeletedAtIsNullOrderByIdAsc()).thenReturn(Optional.empty());
        when(profileRepository.save(any(Profile.class))).thenAnswer(invocation -> {
            Profile profile = invocation.getArgument(0);
            ReflectionTestUtils.setField(profile, "id", 10L);
            return profile;
        });

        ProfileSaveRequest request = new ProfileSaveRequest(
                null,
                "Nhat Nguyen",
                "admin@example.com",
                "",
                "Backend Engineer",
                "",
                "",
                ProfileStatus.ACTIVE,
                List.of(
                        activeContent(ProfileLanguage.EN, "First headline"),
                        activeContent(ProfileLanguage.EN, "Second headline")),
                List.of());

        assertThatThrownBy(() -> profileService.save(request))
                .isInstanceOfSatisfying(ApiException.class, exception ->
                        assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));

        verify(contentRepository, never()).save(any(ProfileContent.class));
        verify(socialLinkRepository, never()).save(any(SocialLink.class));
    }

    @Test
    void saveRejectsInvalidSocialUrlForPlatform() {
        when(profileRepository.findFirstByDeletedAtIsNullOrderByIdAsc()).thenReturn(Optional.empty());
        when(profileRepository.save(any(Profile.class))).thenAnswer(invocation -> {
            Profile profile = invocation.getArgument(0);
            ReflectionTestUtils.setField(profile, "id", 10L);
            return profile;
        });
        when(contentRepository.findFirstByProfileIdAndLanguageAndDeletedAtIsNullOrderByIdAsc(10L, ProfileLanguage.EN))
                .thenReturn(Optional.empty());
        when(socialLinkRepository.findByProfileIdAndDeletedAtIsNullOrderByDisplayOrderAscIdAsc(10L))
                .thenReturn(List.of());

        ProfileSaveRequest request = new ProfileSaveRequest(
                null,
                "Nhat Nguyen",
                "admin@example.com",
                "",
                "Backend Engineer",
                "",
                "",
                ProfileStatus.ACTIVE,
                List.of(activeContent(ProfileLanguage.EN, "Backend APIs")),
                List.of(activeLink(SocialLinkPlatform.GITHUB, "GitHub", "mailto:admin@example.com")));

        assertThatThrownBy(() -> profileService.save(request))
                .isInstanceOfSatisfying(ApiException.class, exception ->
                        assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));

        verify(socialLinkRepository, never()).save(any(SocialLink.class));
    }

    @Test
    void getPublicProfileReturnsActiveLocalizedContentAndActiveLinks() {
        Profile profile = profile(10L, ProfileStatus.ACTIVE);
        ProfileContent content = content(profile, 20L, ProfileLanguage.EN, ProfileContentStatus.ACTIVE);
        SocialLink link = socialLink(profile, 30L, SocialLinkPlatform.LINKEDIN, "LinkedIn", "https://linkedin.com/in/nhat");
        when(profileRepository.findFirstByStatusAndDeletedAtIsNullOrderByIdAsc(ProfileStatus.ACTIVE))
                .thenReturn(Optional.of(profile));
        when(contentRepository.findFirstByProfileIdAndLanguageAndStatusAndDeletedAtIsNullOrderByIdAsc(
                10L,
                ProfileLanguage.EN,
                ProfileContentStatus.ACTIVE))
                .thenReturn(Optional.of(content));
        when(socialLinkRepository.findByProfileIdAndStatusAndDeletedAtIsNullOrderByDisplayOrderAscIdAsc(
                10L,
                SocialLinkStatus.ACTIVE))
                .thenReturn(List.of(link));

        PublicProfileResponse response = profileService.getPublicProfile(ProfileLanguage.EN);

        assertThat(response.displayName()).isEqualTo("Nhat Nguyen");
        assertThat(response.language()).isEqualTo(ProfileLanguage.EN);
        assertThat(response.headline()).isEqualTo("Backend APIs");
        assertThat(response.socialLinks()).hasSize(1);
        assertThat(response.socialLinks().getFirst().status()).isEqualTo(SocialLinkStatus.ACTIVE);
    }

    @Test
    void getPublicProfileReturnsNotFoundWithoutActiveContent() {
        Profile profile = profile(10L, ProfileStatus.ACTIVE);
        when(profileRepository.findFirstByStatusAndDeletedAtIsNullOrderByIdAsc(ProfileStatus.ACTIVE))
                .thenReturn(Optional.of(profile));
        when(contentRepository.findFirstByProfileIdAndLanguageAndStatusAndDeletedAtIsNullOrderByIdAsc(
                10L,
                ProfileLanguage.VI,
                ProfileContentStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> profileService.getPublicProfile(ProfileLanguage.VI))
                .isInstanceOfSatisfying(ApiException.class, exception ->
                        assertThat(exception.getStatus()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    private ProfileContentRequest activeContent(ProfileLanguage language, String headline) {
        return new ProfileContentRequest(
                null,
                language,
                headline,
                "Systems and product engineering",
                "Short bio",
                "Long bio",
                "SEO title",
                "SEO description",
                ProfileContentStatus.ACTIVE);
    }

    private SocialLinkRequest activeLink(SocialLinkPlatform platform, String label, String url) {
        return new SocialLinkRequest(
                null,
                platform,
                label,
                url,
                1,
                SocialLinkStatus.ACTIVE);
    }

    private Profile profile(Long id, ProfileStatus status) {
        Profile profile = new Profile();
        ReflectionTestUtils.setField(profile, "id", id);
        profile.setDisplayName("Nhat Nguyen");
        profile.setEmail("admin@example.com");
        profile.setLocation("Da Nang");
        profile.setPrimaryRole("Backend Engineer");
        profile.setCareerDirection("Architecture");
        profile.setMainTechFocus("Spring Boot");
        profile.setStatus(status);
        return profile;
    }

    private ProfileContent content(
            Profile profile,
            Long id,
            ProfileLanguage language,
            ProfileContentStatus status) {
        ProfileContent content = new ProfileContent();
        ReflectionTestUtils.setField(content, "id", id);
        content.setProfile(profile);
        content.setLanguage(language);
        content.setHeadline("Backend APIs");
        content.setSubheadline("Systems and product engineering");
        content.setShortBio("Short bio");
        content.setLongBio("Long bio");
        content.setSeoTitle("SEO title");
        content.setSeoDescription("SEO description");
        content.setStatus(status);
        return content;
    }

    private SocialLink socialLink(
            Profile profile,
            Long id,
            SocialLinkPlatform platform,
            String label,
            String url) {
        SocialLink link = new SocialLink();
        ReflectionTestUtils.setField(link, "id", id);
        link.setProfile(profile);
        link.setPlatform(platform);
        link.setLabel(label);
        link.setUrl(url);
        link.setDisplayOrder(1);
        link.setStatus(SocialLinkStatus.ACTIVE);
        return link;
    }
}
