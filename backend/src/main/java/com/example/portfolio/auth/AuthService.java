package com.example.portfolio.auth;

import com.example.portfolio.auth.TokenService.AccessToken;
import com.example.portfolio.auth.dto.AuthResponse;
import com.example.portfolio.auth.dto.CurrentUserResponse;
import com.example.portfolio.auth.dto.LoginRequest;
import com.example.portfolio.auth.dto.LogoutResponse;
import com.example.portfolio.common.exception.ApiException;
import com.example.portfolio.user.User;
import com.example.portfolio.user.UserRepository;
import com.example.portfolio.user.UserStatus;
import java.time.Clock;
import java.time.Instant;
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
    private final Clock clock;

    public AuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            TokenService tokenService,
            Clock clock) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.clock = clock;
    }

    @Transactional
    public AuthResponse login(LoginRequest request, String clientIp) {
        User user = userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull(request.email().trim())
                .filter(candidate -> candidate.getStatus() == UserStatus.ACTIVE)
                .filter(candidate -> passwordEncoder.matches(request.password(), candidate.getPasswordHash()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, INVALID_CREDENTIALS));

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
                        return new LogoutResponse(true);
                    }
                    return new LogoutResponse(false);
                })
                .orElseGet(() -> new LogoutResponse(false));
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
