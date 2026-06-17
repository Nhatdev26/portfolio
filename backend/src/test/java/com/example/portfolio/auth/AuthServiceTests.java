package com.example.portfolio.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.portfolio.auth.dto.AuthResponse;
import com.example.portfolio.auth.dto.LoginRequest;
import com.example.portfolio.auth.dto.LogoutResponse;
import com.example.portfolio.common.exception.ApiException;
import com.example.portfolio.user.User;
import com.example.portfolio.user.UserRepository;
import com.example.portfolio.user.UserRole;
import com.example.portfolio.user.UserStatus;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthServiceTests {

    private static final Clock CLOCK = Clock.fixed(Instant.now().plus(Duration.ofDays(1)), ZoneOffset.UTC);

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private TokenService tokenService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        tokenService = new TokenService(testProperties(), CLOCK);
        authService = new AuthService(
                userRepository,
                refreshTokenRepository,
                passwordEncoder,
                tokenService,
                CLOCK);
    }

    @Test
    void loginIssuesAccessTokenAndStoresRefreshTokenHash() {
        User user = activeAdmin("secret");
        when(userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull("admin@example.com")).thenReturn(Optional.of(user));

        AuthResponse response = authService.login(
                new LoginRequest(" admin@example.com ", "secret"),
                "127.0.0.1");

        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.refreshToken()).isNotBlank();
        assertThat(response.expiresIn()).isEqualTo(900);
        assertThat(response.user().email()).isEqualTo("admin@example.com");

        AuthenticatedUser parsed = tokenService.parseAccessToken(response.accessToken());
        assertThat(parsed.id()).isEqualTo(1L);
        assertThat(parsed.role()).isEqualTo(UserRole.ADMIN);

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        RefreshToken stored = captor.getValue();
        assertThat(stored.getTokenHash()).isEqualTo(tokenService.hashRefreshToken(response.refreshToken()));
        assertThat(stored.getTokenHash()).isNotEqualTo(response.refreshToken());
        assertThat(stored.getCreatedByIp()).isEqualTo("127.0.0.1");
        assertThat(stored.getExpiresAt()).isEqualTo(CLOCK.instant().plus(Duration.ofDays(14)));
    }

    @Test
    void loginRejectsInvalidPassword() {
        User user = activeAdmin("secret");
        when(userRepository.findByEmailIgnoreCaseAndDeletedAtIsNull("admin@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(
                new LoginRequest("admin@example.com", "wrong"),
                "127.0.0.1"))
                .isInstanceOfSatisfying(ApiException.class, exception ->
                        assertThat(exception.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED));

        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    void refreshRotatesUsableToken() {
        User user = activeAdmin("secret");
        String rawRefreshToken = "existing-refresh-token";
        RefreshToken existing = new RefreshToken();
        existing.setUser(user);
        existing.setTokenHash(tokenService.hashRefreshToken(rawRefreshToken));
        existing.setExpiresAt(CLOCK.instant().plusSeconds(60));
        when(refreshTokenRepository.findByTokenHash(existing.getTokenHash())).thenReturn(Optional.of(existing));

        AuthResponse response = authService.refresh(rawRefreshToken, "10.0.0.5");

        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.refreshToken()).isNotEqualTo(rawRefreshToken);
        assertThat(existing.getRevokedAt()).isEqualTo(CLOCK.instant());
        assertThat(existing.getRevokedByIp()).isEqualTo("10.0.0.5");

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository, times(2)).save(captor.capture());
        List<RefreshToken> saved = captor.getAllValues();
        assertThat(saved.get(0)).isSameAs(existing);
        assertThat(saved.get(1).getTokenHash()).isEqualTo(tokenService.hashRefreshToken(response.refreshToken()));
    }

    @Test
    void logoutRevokesKnownRefreshToken() {
        String rawRefreshToken = "refresh-token";
        RefreshToken existing = new RefreshToken();
        existing.setTokenHash(tokenService.hashRefreshToken(rawRefreshToken));
        existing.setExpiresAt(CLOCK.instant().plusSeconds(60));
        when(refreshTokenRepository.findByTokenHash(existing.getTokenHash())).thenReturn(Optional.of(existing));

        LogoutResponse response = authService.logout(rawRefreshToken, "10.0.0.6");

        assertThat(response.revoked()).isTrue();
        assertThat(existing.getRevokedAt()).isEqualTo(CLOCK.instant());
        assertThat(existing.getRevokedByIp()).isEqualTo("10.0.0.6");
        verify(refreshTokenRepository).save(existing);
    }

    private AuthProperties testProperties() {
        return new AuthProperties(
                14,
                new AuthProperties.Jwt(
                        "portfolio-cms",
                        "test-secret-with-at-least-thirty-two-bytes",
                        15));
    }

    private User activeAdmin(String password) {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", 1L);
        user.setEmail("admin@example.com");
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(UserRole.ADMIN);
        user.setStatus(UserStatus.ACTIVE);
        return user;
    }
}
