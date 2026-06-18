package com.example.portfolio.media.dto;

import com.example.portfolio.media.MediaVisibility;

public record MediaAssetUpdateRequest(
        String title,
        String altText,
        String caption,
        MediaVisibility visibility) {
}
