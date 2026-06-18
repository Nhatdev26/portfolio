package com.example.portfolio.media.dto;

import com.example.portfolio.media.MediaEntityType;
import com.example.portfolio.media.MediaUsageType;

public record MediaUsageRequest(
        MediaEntityType entityType,
        Long entityId,
        MediaUsageType usageType) {
}
