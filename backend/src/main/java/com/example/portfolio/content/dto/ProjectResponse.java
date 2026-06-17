package com.example.portfolio.content.dto;

import com.example.portfolio.content.ContentLanguage;
import com.example.portfolio.content.ContentStatus;
import com.example.portfolio.project.ProjectLifecycleStatus;
import com.example.portfolio.project.ProjectType;
import com.example.portfolio.media.dto.MediaEntityAssetResponse;
import com.example.portfolio.taxonomy.dto.TagResponse;
import com.example.portfolio.taxonomy.dto.TechnologyResponse;
import java.time.Instant;
import java.util.List;

public record ProjectResponse(
        Long id,
        String title,
        String slug,
        ContentLanguage language,
        String summary,
        String description,
        String role,
        ProjectType projectType,
        ProjectLifecycleStatus projectStatus,
        ContentStatus contentStatus,
        String problemStatement,
        String solutionOverview,
        String backendHighlights,
        String frontendHighlights,
        String architectureNotes,
        String sourceUrl,
        String demoUrl,
        String seoTitle,
        String seoDescription,
        Instant publishedAt,
        int displayOrder,
        List<TechnologyResponse> technologies,
        List<TagResponse> tags,
        List<NoteSummaryResponse> notes,
        List<MediaEntityAssetResponse> media) {}
