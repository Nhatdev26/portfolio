package com.example.portfolio.cv;

import com.example.portfolio.content.ContentLanguage;
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
@Table(name = "cv_files")
public class CvFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public ContentLanguage language = ContentLanguage.EN;

    @Column(name = "target_role", nullable = false)
    public String targetRole;

    @Column(nullable = false)
    public String version;

    @Column(name = "original_filename", nullable = false)
    public String originalFilename;

    @Column(name = "content_type", nullable = false)
    public String contentType;

    @Column(name = "file_size", nullable = false)
    public long fileSize;

    @Column(name = "file_data", nullable = false, columnDefinition = "bytea")
    public byte[] fileData;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public CvFileStatus status = CvFileStatus.DRAFT;

    @Column(name = "uploaded_at", nullable = false)
    public Instant uploadedAt;

    @Column(name = "activated_at")
    public Instant activatedAt;

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
