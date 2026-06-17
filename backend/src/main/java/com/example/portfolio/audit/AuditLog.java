package com.example.portfolio.audit;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "actor_id")
    public Long actorId;

    @Column(name = "actor_email", length = 320)
    public String actorEmail;

    @Column(nullable = false, length = 80)
    public String action;

    @Column(name = "entity_type", nullable = false, length = 80)
    public String entityType;

    @Column(name = "entity_id", length = 80)
    public String entityId;

    @Column(name = "entity_title")
    public String entityTitle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    public AuditResult result;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "old_value", columnDefinition = "jsonb")
    public JsonNode oldValue;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "new_value", columnDefinition = "jsonb")
    public JsonNode newValue;

    @Column(name = "ip_address", length = 64)
    public String ipAddress;

    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;
}
