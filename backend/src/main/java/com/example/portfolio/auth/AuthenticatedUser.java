package com.example.portfolio.auth;

import com.example.portfolio.user.UserRole;
import com.example.portfolio.user.UserStatus;

public record AuthenticatedUser(
        Long id,
        String email,
        UserRole role,
        UserStatus status) {
}
