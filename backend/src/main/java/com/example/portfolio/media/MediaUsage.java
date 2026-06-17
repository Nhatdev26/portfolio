package com.example.portfolio.media;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "media_usages")
public class MediaUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "media_asset_id", nullable = false)
    public Long mediaAssetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false)
    public MediaEntityType entityType;

    @Column(name = "entity_id", nullable = false)
    public Long entityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "usage_type", nullable = false)
    public MediaUsageType usageType;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
