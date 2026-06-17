package com.example.portfolio.audit;

import com.example.portfolio.audit.dto.AuditLogResponse;
import com.example.portfolio.auth.AuthenticatedUser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.criteria.Predicate;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private static final int DEFAULT_LIMIT = 100;

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public AuditService(AuditLogRepository auditLogRepository, ObjectMapper objectMapper, Clock clock) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void success(String action, String entityType, Object entityId, String entityTitle, Object oldValue, Object newValue) {
        AuthenticatedUser actor = currentActor();
        record(actor == null ? null : actor.id(), actor == null ? null : actor.email(), action, entityType, entityId,
                entityTitle, AuditResult.SUCCESS, oldValue, newValue, null);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void authEvent(Long actorId, String actorEmail, String action, AuditResult result, Object newValue, String ipAddress) {
        record(actorId, actorEmail, action, "AUTH", actorId, actorEmail, result, null, newValue, ipAddress);
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> search(String action, String entityType, String actor, Instant from, Instant to) {
        Specification<AuditLog> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (action != null && !action.isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("action"), action.trim().toUpperCase(Locale.ROOT)));
            }
            if (entityType != null && !entityType.isBlank()) {
                predicates.add(criteriaBuilder.equal(root.get("entityType"), entityType.trim().toUpperCase(Locale.ROOT)));
            }
            if (actor != null && !actor.isBlank()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("actorEmail")),
                        "%" + actor.trim().toLowerCase(Locale.ROOT) + "%"));
            }
            if (from != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), from));
            }
            if (to != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), to));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };

        return auditLogRepository
                .findAll(spec, PageRequest.of(0, DEFAULT_LIMIT, Sort.by(Sort.Direction.DESC, "createdAt", "id")))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private void record(
            Long actorId,
            String actorEmail,
            String action,
            String entityType,
            Object entityId,
            String entityTitle,
            AuditResult result,
            Object oldValue,
            Object newValue,
            String ipAddress) {
        AuditLog auditLog = new AuditLog();
        auditLog.actorId = actorId;
        auditLog.actorEmail = blankToNull(actorEmail);
        auditLog.action = action.trim().toUpperCase(Locale.ROOT);
        auditLog.entityType = entityType.trim().toUpperCase(Locale.ROOT);
        auditLog.entityId = entityId == null ? null : String.valueOf(entityId);
        auditLog.entityTitle = blankToNull(entityTitle);
        auditLog.result = result;
        auditLog.oldValue = sanitize(oldValue);
        auditLog.newValue = sanitize(newValue);
        auditLog.ipAddress = blankToNull(ipAddress);
        auditLog.createdAt = Instant.now(clock);
        auditLogRepository.save(auditLog);
    }

    private JsonNode sanitize(Object value) {
        if (value == null) {
            return null;
        }
        JsonNode node = objectMapper.valueToTree(value);
        redact(node);
        return node;
    }

    private void redact(JsonNode node) {
        if (node instanceof ObjectNode objectNode) {
            List<String> names = new ArrayList<>();
            objectNode.fieldNames().forEachRemaining(names::add);
            for (String name : names) {
                if (isSensitive(name)) {
                    objectNode.put(name, "[REDACTED]");
                } else {
                    redact(objectNode.get(name));
                }
            }
            return;
        }
        if (node instanceof ArrayNode arrayNode) {
            arrayNode.forEach(this::redact);
        }
    }

    private boolean isSensitive(String name) {
        String key = name.toLowerCase(Locale.ROOT);
        return key.contains("password")
                || key.contains("token")
                || key.contains("secret")
                || key.contains("authorization");
    }

    private AuthenticatedUser currentActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            return null;
        }
        return user;
    }

    private AuditLogResponse toResponse(AuditLog auditLog) {
        return new AuditLogResponse(
                auditLog.id,
                auditLog.actorId,
                auditLog.actorEmail,
                auditLog.action,
                auditLog.entityType,
                auditLog.entityId,
                auditLog.entityTitle,
                auditLog.result,
                auditLog.oldValue,
                auditLog.newValue,
                auditLog.createdAt);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
