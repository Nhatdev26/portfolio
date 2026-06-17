package com.example.portfolio.profile;

import com.example.portfolio.profile.dto.PublicProfileResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public/profile")
public class PublicProfileController {

    private final ProfileService profileService;

    public PublicProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public PublicProfileResponse getProfile(
            @RequestParam(defaultValue = "EN") ProfileLanguage language) {
        return profileService.getPublicProfile(language);
    }
}
