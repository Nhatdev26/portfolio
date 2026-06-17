package com.example.portfolio.media.dto;

import com.example.portfolio.media.MediaAssetStatus;
import com.example.portfolio.media.MediaUsageType;
import com.example.portfolio.media.MediaVisibility;
import java.time.Instant;

public record MediaEntityAssetResponse(
        Long usageId,
        Long mediaAssetId,
        MediaUsageType usageType,
        String originalFilename,
        String contentType,
        String title,
        String altText,
        String caption,
        MediaAssetStatus status,
        MediaVisibility visibility,
        Instant uploadedAt) {
}
