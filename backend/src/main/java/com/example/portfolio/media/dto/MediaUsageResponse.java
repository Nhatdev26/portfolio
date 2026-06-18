package com.example.portfolio.media.dto;

import com.example.portfolio.media.MediaEntityType;
import com.example.portfolio.media.MediaUsageType;
import java.time.Instant;

public record MediaUsageResponse(
        Long id,
        MediaEntityType entityType,
        Long entityId,
        MediaUsageType usageType,
        Instant createdAt) {
}
