package com.example.portfolio.note;

import com.example.portfolio.content.ContentLanguage;
import com.example.portfolio.content.ContentStatus;
import com.example.portfolio.taxonomy.Category;
import com.example.portfolio.taxonomy.Tag;
import com.example.portfolio.taxonomy.Technology;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "technical_notes")
public class TechnicalNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false)
    public String title;

    @Column(nullable = false)
    public String slug;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public ContentLanguage language = ContentLanguage.EN;

    @Column(nullable = false, length = 1000)
    public String excerpt;

    @Column(nullable = false, columnDefinition = "text")
    public String content;

    @ManyToOne
    @JoinColumn(name = "category_id")
    public Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public ContentStatus status = ContentStatus.DRAFT;

    @Column(name = "seo_title")
    public String seoTitle;

    @Column(name = "seo_description", length = 1000)
    public String seoDescription;

    @Column(name = "reading_minutes", nullable = false)
    public int readingMinutes = 1;

    @Column(name = "published_at")
    public Instant publishedAt;

    @Column(name = "display_order", nullable = false)
    public int displayOrder;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;

    @Column(name = "deleted_at")
    public Instant deletedAt;

    @ManyToMany
    @JoinTable(
            name = "note_technologies",
            joinColumns = @JoinColumn(name = "note_id"),
            inverseJoinColumns = @JoinColumn(name = "technology_id"))
    public Set<Technology> technologies = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(
            name = "note_tags",
            joinColumns = @JoinColumn(name = "note_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    public Set<Tag> tags = new LinkedHashSet<>();

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
