package com.example.portfolio.project;

import com.example.portfolio.content.ContentLanguage;
import com.example.portfolio.content.ContentStatus;
import com.example.portfolio.note.TechnicalNote;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "projects")
public class Project {

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
    public String summary;

    @Column(columnDefinition = "text")
    public String description;

    @Column(nullable = false)
    public String role;

    @Enumerated(EnumType.STRING)
    @Column(name = "project_type", nullable = false)
    public ProjectType projectType = ProjectType.FULL_STACK;

    @Enumerated(EnumType.STRING)
    @Column(name = "project_status", nullable = false)
    public ProjectLifecycleStatus projectStatus = ProjectLifecycleStatus.COMPLETED;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_status", nullable = false)
    public ContentStatus contentStatus = ContentStatus.DRAFT;

    @Column(name = "problem_statement", columnDefinition = "text")
    public String problemStatement;

    @Column(name = "solution_overview", columnDefinition = "text")
    public String solutionOverview;

    @Column(name = "backend_highlights", columnDefinition = "text")
    public String backendHighlights;

    @Column(name = "frontend_highlights", columnDefinition = "text")
    public String frontendHighlights;

    @Column(name = "architecture_notes", columnDefinition = "text")
    public String architectureNotes;

    @Column(name = "source_url")
    public String sourceUrl;

    @Column(name = "demo_url")
    public String demoUrl;

    @Column(name = "seo_title")
    public String seoTitle;

    @Column(name = "seo_description", length = 1000)
    public String seoDescription;

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
            name = "project_technologies",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "technology_id"))
    public Set<Technology> technologies = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(
            name = "project_tags",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    public Set<Tag> tags = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(
            name = "project_notes",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "note_id"))
    public Set<TechnicalNote> notes = new LinkedHashSet<>();

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
