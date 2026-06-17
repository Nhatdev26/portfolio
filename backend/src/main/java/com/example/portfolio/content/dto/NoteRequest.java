package com.example.portfolio.content.dto;

import com.example.portfolio.content.ContentLanguage;
import com.example.portfolio.content.ContentStatus;
import java.util.List;

public record NoteRequest(
        String title,
        String slug,
        ContentLanguage language,
        String excerpt,
        String content,
        Long categoryId,
        ContentStatus status,
        String seoTitle,
        String seoDescription,
        Integer readingMinutes,
        Integer displayOrder,
        List<Long> technologyIds,
        List<Long> tagIds) {}
