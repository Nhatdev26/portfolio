package com.example.portfolio.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuditServiceTests {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);

    @Mock
    private AuditLogRepository auditLogRepository;

    private AuditService auditService;

    @BeforeEach
    void setUp() {
        auditService = new AuditService(auditLogRepository, new ObjectMapper(), CLOCK);
    }

    @Test
    void authEventStoresActorAndTimestamp() {
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        auditService.authEvent(
                1L,
                "admin@example.com",
                "login_success",
                AuditResult.SUCCESS,
                Map.of("email", "admin@example.com"),
                "127.0.0.1");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        AuditLog auditLog = captor.getValue();
        assertThat(auditLog.actorId).isEqualTo(1L);
        assertThat(auditLog.actorEmail).isEqualTo("admin@example.com");
        assertThat(auditLog.action).isEqualTo("LOGIN_SUCCESS");
        assertThat(auditLog.entityType).isEqualTo("AUTH");
        assertThat(auditLog.result).isEqualTo(AuditResult.SUCCESS);
        assertThat(auditLog.createdAt).isEqualTo(CLOCK.instant());
    }

    @Test
    void redactsSensitiveKeysRecursively() {
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        auditService.authEvent(
                null,
                "admin@example.com",
                "login_failure",
                AuditResult.FAILURE,
                Map.of(
                        "password", "secret",
                        "nested", Map.of("refreshToken", "raw-token", "safe", "visible")),
                "127.0.0.1");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        AuditLog auditLog = captor.getValue();
        assertThat(auditLog.newValue.get("password").asText()).isEqualTo("[REDACTED]");
        assertThat(auditLog.newValue.get("nested").get("refreshToken").asText()).isEqualTo("[REDACTED]");
        assertThat(auditLog.newValue.get("nested").get("safe").asText()).isEqualTo("visible");
    }
}
