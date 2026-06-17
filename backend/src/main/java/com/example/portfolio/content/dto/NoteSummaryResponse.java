package com.example.portfolio.content.dto;

import com.example.portfolio.content.ContentLanguage;
import java.time.Instant;

public record NoteSummaryResponse(
        Long id,
        String title,
        String slug,
        ContentLanguage language,
        String excerpt,
        Instant publishedAt) {}
