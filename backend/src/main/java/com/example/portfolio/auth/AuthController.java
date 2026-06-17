package com.example.portfolio.auth;

import com.example.portfolio.auth.dto.AuthResponse;
import com.example.portfolio.auth.dto.CurrentUserResponse;
import com.example.portfolio.auth.dto.LoginRequest;
import com.example.portfolio.auth.dto.LogoutRequest;
import com.example.portfolio.auth.dto.LogoutResponse;
import com.example.portfolio.auth.dto.RefreshRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        return authService.login(request, clientIp(servletRequest));
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest request, HttpServletRequest servletRequest) {
        return authService.refresh(request.refreshToken(), clientIp(servletRequest));
    }

    @PostMapping("/logout")
    public LogoutResponse logout(@Valid @RequestBody LogoutRequest request, HttpServletRequest servletRequest) {
        return authService.logout(request.refreshToken(), clientIp(servletRequest));
    }

    @GetMapping("/me")
    public CurrentUserResponse me(@AuthenticationPrincipal AuthenticatedUser user) {
        return authService.currentUser(user);
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
