package com.example.portfolio.media;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "media_assets")
public class MediaAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "original_filename", nullable = false)
    public String originalFilename;

    @Column(name = "content_type", nullable = false)
    public String contentType;

    @Column(name = "file_size", nullable = false)
    public long fileSize;

    @Column(name = "file_data", nullable = false, columnDefinition = "bytea")
    public byte[] fileData;

    @Column(nullable = false)
    public String title;

    @Column(name = "alt_text")
    public String altText;

    @Column
    public String caption;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public MediaAssetStatus status = MediaAssetStatus.READY;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public MediaVisibility visibility = MediaVisibility.PRIVATE;

    @Column(name = "uploaded_at", nullable = false)
    public Instant uploadedAt;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;

    @Column(name = "deleted_at")
    public Instant deletedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (uploadedAt == null) {
            uploadedAt = now;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
