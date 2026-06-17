package com.example.portfolio.audit.dto;

import com.example.portfolio.audit.AuditResult;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;

public record AuditLogResponse(
        Long id,
        Long actorId,
        String actorEmail,
        String action,
        String entityType,
        String entityId,
        String entityTitle,
        AuditResult result,
        JsonNode oldValue,
        JsonNode newValue,
        Instant createdAt) {
}
