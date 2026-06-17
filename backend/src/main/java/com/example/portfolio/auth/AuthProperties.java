package com.example.portfolio.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.auth")
public record AuthProperties(
        int refreshTokenDays,
        Jwt jwt) {

    public record Jwt(
            String issuer,
            String secret,
            long accessTokenMinutes) {
    }
}
