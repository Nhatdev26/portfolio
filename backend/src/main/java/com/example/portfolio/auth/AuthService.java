package com.example.portfolio.auth;

import com.example.portfolio.auth.TokenService.AccessToken;
import com.example.portfolio.auth.dto.AuthResponse;
import com.example.portfolio.auth.dto.CurrentUserResponse;
import com.example.portfolio.auth.dto.LoginRequest;
import com.example.portfolio.auth.dto.LogoutResponse;
import com.example.portfolio.audit.AuditResult;
import com.example.portfolio.audit.AuditService;
import com.example.portfolio.common.exception.ApiException;
import com.example.portfolio.user.User;
import com.example.portfolio.user.UserRepository;
import com.example.portfolio.user.UserStatus;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final String INVALID_CREDENTIALS = "Invalid email or password.";
    private static final String INVALID_REFRESH_TOKEN = "Invalid refresh token.";

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final AuditService auditService;
    private final Clock clock;

    public AuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            TokenService tokenService,
            AuditService auditService,
            Clock clock) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.auditService = auditService;
        this.clock = clock;
    }

    @Transactional
    public AuthResponse login(LoginRequest request, String clientIp) {
        String email = request.email().trim();
        Optional<User> authenticated = userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(email)
                .filter(candidate -> candidate.getStatus() == UserStatus.ACTIVE)
                .filter(candidate -> passwordEncoder.matches(request.password(), candidate.getPasswordHash()));
        if (authenticated.isEmpty()) {
            auditService.authEvent(null, email, "LOGIN_FAILURE", AuditResult.FAILURE,
                    Map.of("reason", "invalid_credentials"), clientIp);
            throw new ApiException(HttpStatus.UNAUTHORIZED, INVALID_CREDENTIALS);
        }

        User user = authenticated.get();
        auditService.authEvent(user.getId(), user.getEmail(), "LOGIN_SUCCESS", AuditResult.SUCCESS,
                Map.of("email", user.getEmail()), clientIp);
        return issueSession(user, clientIp);
    }

    @Transactional
    public AuthResponse refresh(String rawRefreshToken, String clientIp) {
        RefreshToken refreshToken = refreshTokenRepository
                .findByTokenHash(tokenService.hashRefreshToken(rawRefreshToken))
                .filter(this::isUsable)
                .filter(token -> isActiveUser(token.getUser()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, INVALID_REFRESH_TOKEN));

        revoke(refreshToken, clientIp);
        return issueSession(refreshToken.getUser(), clientIp);
    }

    @Transactional
    public LogoutResponse logout(String rawRefreshToken, String clientIp) {
        return refreshTokenRepository.findByTokenHash(tokenService.hashRefreshToken(rawRefreshToken))
                .map(token -> {
                    if (token.getRevokedAt() == null) {
                        revoke(token, clientIp);
                        User user = token.getUser();
                        auditService.authEvent(
                                user == null ? null : user.getId(),
                                user == null ? null : user.getEmail(),
                                "LOGOUT",
                                AuditResult.SUCCESS,
                                Map.of("revoked", true),
                                clientIp);
                        return new LogoutResponse(true);
                    }
                    User user = token.getUser();
                    auditService.authEvent(
                            user == null ? null : user.getId(),
                            user == null ? null : user.getEmail(),
                            "LOGOUT",
                            AuditResult.FAILURE,
                            Map.of("reason", "already_revoked"),
                            clientIp);
                    return new LogoutResponse(false);
                })
                .orElseGet(() -> {
                    auditService.authEvent(null, null, "LOGOUT", AuditResult.FAILURE,
                            Map.of("reason", "unknown_refresh_token"), clientIp);
                    return new LogoutResponse(false);
                });
    }

    public CurrentUserResponse currentUser(AuthenticatedUser user) {
        return new CurrentUserResponse(user.id(), user.email(), user.role(), user.status());
    }

    private AuthResponse issueSession(User user, String clientIp) {
        AccessToken accessToken = tokenService.createAccessToken(user);
        String rawRefreshToken = tokenService.newRefreshToken();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(tokenService.hashRefreshToken(rawRefreshToken));
        refreshToken.setExpiresAt(tokenService.refreshTokenExpiresAt());
        refreshToken.setCreatedByIp(clientIp);
        refreshTokenRepository.save(refreshToken);

        return new AuthResponse(
                accessToken.token(),
                rawRefreshToken,
                "Bearer",
                accessToken.expiresIn(),
                toResponse(user));
    }

    private boolean isUsable(RefreshToken refreshToken) {
        Instant now = clock.instant();
        return refreshToken.getRevokedAt() == null && refreshToken.getExpiresAt().isAfter(now);
    }

    private boolean isActiveUser(User user) {
        return user.getDeletedAt() == null && user.getStatus() == UserStatus.ACTIVE;
    }

    private void revoke(RefreshToken refreshToken, String clientIp) {
        refreshToken.setRevokedAt(clock.instant());
        refreshToken.setRevokedByIp(clientIp);
        refreshTokenRepository.save(refreshToken);
    }

    private CurrentUserResponse toResponse(User user) {
        return new CurrentUserResponse(user.getId(), user.getEmail(), user.getRole(), user.getStatus());
    }
}
