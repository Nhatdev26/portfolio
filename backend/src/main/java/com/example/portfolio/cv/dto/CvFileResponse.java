package com.example.portfolio.cv.dto;

import com.example.portfolio.content.ContentLanguage;
import com.example.portfolio.cv.CvFileStatus;
import java.time.Instant;

public record CvFileResponse(
        Long id,
        ContentLanguage language,
        String targetRole,
        String version,
        String originalFilename,
        String contentType,
        long fileSize,
        CvFileStatus status,
        Instant uploadedAt,
        Instant activatedAt) {}
