package com.example.portfolio.content.dto;

import com.example.portfolio.content.ContentLanguage;
import com.example.portfolio.content.ContentStatus;
import com.example.portfolio.project.ProjectLifecycleStatus;
import com.example.portfolio.project.ProjectType;
import java.util.List;

public record ProjectRequest(
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
        Integer displayOrder,
        List<Long> technologyIds,
        List<Long> tagIds,
        List<Long> noteIds) {}
