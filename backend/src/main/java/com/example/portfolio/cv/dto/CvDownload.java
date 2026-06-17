package com.example.portfolio.cv.dto;

public record CvDownload(
        String filename,
        String contentType,
        long fileSize,
        byte[] bytes) {}
