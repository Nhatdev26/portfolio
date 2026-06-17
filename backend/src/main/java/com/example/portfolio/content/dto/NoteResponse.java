package com.example.portfolio.content.dto;

import com.example.portfolio.content.ContentLanguage;
import com.example.portfolio.content.ContentStatus;
import com.example.portfolio.taxonomy.dto.CategoryResponse;
import com.example.portfolio.taxonomy.dto.TagResponse;
import com.example.portfolio.taxonomy.dto.TechnologyResponse;
import java.time.Instant;
import java.util.List;

public record NoteResponse(
        Long id,
        String title,
        String slug,
        ContentLanguage language,
        String excerpt,
        String content,
        CategoryResponse category,
        ContentStatus status,
        String seoTitle,
        String seoDescription,
        int readingMinutes,
        Instant publishedAt,
        int displayOrder,
        List<TechnologyResponse> technologies,
        List<TagResponse> tags) {}
