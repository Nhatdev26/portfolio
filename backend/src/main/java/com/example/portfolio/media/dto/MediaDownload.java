package com.example.portfolio.media.dto;

public record MediaDownload(
        String filename,
        String contentType,
        long fileSize,
        byte[] bytes) {
}
