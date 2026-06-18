package com.example.portfolio.media.dto;

import com.example.portfolio.media.MediaAssetStatus;
import com.example.portfolio.media.MediaVisibility;
import java.time.Instant;
import java.util.List;

public record MediaAssetResponse(
        Long id,
        String originalFilename,
        String contentType,
        long fileSize,
        String title,
        String altText,
        String caption,
        MediaAssetStatus status,
        MediaVisibility visibility,
        Instant uploadedAt,
        List<MediaUsageResponse> usages) {
}
