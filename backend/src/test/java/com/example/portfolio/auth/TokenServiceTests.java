package com.example.portfolio.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.portfolio.auth.TokenService.AccessToken;
import com.example.portfolio.user.User;
import com.example.portfolio.user.UserRole;
import com.example.portfolio.user.UserStatus;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class TokenServiceTests {

    private static final Clock CLOCK = Clock.fixed(Instant.now().plus(Duration.ofDays(1)), ZoneOffset.UTC);

    @Test
    void createsAndParsesAccessToken() {
        TokenService tokenService = new TokenService(testProperties(), CLOCK);
        User user = activeAdmin();

        AccessToken accessToken = tokenService.createAccessToken(user);

        assertThat(accessToken.token()).isNotBlank();
        assertThat(accessToken.expiresIn()).isEqualTo(900);

        AuthenticatedUser parsed = tokenService.parseAccessToken(accessToken.token());
        assertThat(parsed.id()).isEqualTo(1L);
        assertThat(parsed.email()).isEqualTo("admin@example.com");
        assertThat(parsed.role()).isEqualTo(UserRole.ADMIN);
        assertThat(parsed.status()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    void hashesRefreshTokenWithoutKeepingRawValue() {
        TokenService tokenService = new TokenService(testProperties(), CLOCK);

        String rawToken = "refresh-token";
        String hashed = tokenService.hashRefreshToken(rawToken);

        assertThat(hashed).hasSize(64);
        assertThat(hashed).isEqualTo(tokenService.hashRefreshToken(rawToken));
        assertThat(hashed).doesNotContain(rawToken);
    }

    @Test
    void rejectsShortJwtSecret() {
        AuthProperties properties = new AuthProperties(
                14,
                new AuthProperties.Jwt("portfolio-cms", "short", 15));

        assertThatThrownBy(() -> new TokenService(properties, CLOCK))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("AUTH_JWT_SECRET");
    }

    private AuthProperties testProperties() {
        return new AuthProperties(
                14,
                new AuthProperties.Jwt(
                        "portfolio-cms",
                        "test-secret-with-at-least-thirty-two-bytes",
                        15));
    }

    private User activeAdmin() {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", 1L);
        user.setEmail("admin@example.com");
        user.setPasswordHash("hash");
        user.setRole(UserRole.ADMIN);
        user.setStatus(UserStatus.ACTIVE);
        return user;
    }
}
