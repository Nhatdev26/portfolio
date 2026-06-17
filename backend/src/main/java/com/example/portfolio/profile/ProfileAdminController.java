package com.example.portfolio.profile;

import com.example.portfolio.profile.dto.ProfileAdminResponse;
import com.example.portfolio.profile.dto.ProfileSaveRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/profile")
public class ProfileAdminController {

    private final ProfileService profileService;

    public ProfileAdminController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ProfileAdminResponse getProfile() {
        return profileService.getAdminProfile();
    }

    @PutMapping
    public ProfileAdminResponse saveProfile(@Valid @RequestBody ProfileSaveRequest request) {
        return profileService.save(request);
    }
}
