package com.example.portfolio.auth.dto;

import com.example.portfolio.user.UserRole;
import com.example.portfolio.user.UserStatus;

public record CurrentUserResponse(
        Long id,
        String email,
        UserRole role,
        UserStatus status) {
}
