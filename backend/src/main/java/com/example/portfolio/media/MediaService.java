package com.example.portfolio.media;

import com.example.portfolio.audit.AuditService;
import com.example.portfolio.common.exception.ApiException;
import com.example.portfolio.media.dto.MediaAssetResponse;
import com.example.portfolio.media.dto.MediaAssetUpdateRequest;
import com.example.portfolio.media.dto.MediaDownload;
import com.example.portfolio.media.dto.MediaEntityAssetResponse;
import com.example.portfolio.media.dto.MediaUsageRequest;
import com.example.portfolio.media.dto.MediaUsageResponse;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MediaService {

    public static final long MAX_FILE_SIZE_BYTES = 10L * 1024L * 1024L;

    private final MediaAssetRepository mediaAssetRepository;
    private final MediaUsageRepository mediaUsageRepository;
    private final AuditService auditService;
    private final Clock clock;

    public MediaService(
            MediaAssetRepository mediaAssetRepository,
            MediaUsageRepository mediaUsageRepository,
            AuditService auditService,
            Clock clock) {
        this.mediaAssetRepository = mediaAssetRepository;
        this.mediaUsageRepository = mediaUsageRepository;
        this.auditService = auditService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<MediaAssetResponse> listAdmin() {
        return mediaAssetRepository.findByDeletedAtIsNullOrderByUploadedAtDescIdDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public MediaAssetResponse upload(
            MultipartFile file,
            String title,
            String altText,
            String caption,
            MediaVisibility visibility) {
        ValidatedMediaFile validatedFile = validateFile(file);
        MediaAsset mediaAsset = new MediaAsset();
        mediaAsset.originalFilename = validatedFile.filename();
        mediaAsset.contentType = validatedFile.contentType();
        mediaAsset.fileSize = validatedFile.bytes().length;
        mediaAsset.fileData = validatedFile.bytes();
        mediaAsset.title = blankToNull(title) == null ? titleFromFilename(validatedFile.filename()) : title.trim();
        mediaAsset.altText = blankToNull(altText);
        mediaAsset.caption = blankToNull(caption);
        mediaAsset.visibility = visibility == null ? MediaVisibility.PRIVATE : visibility;
        mediaAsset.status = MediaAssetStatus.READY;
        mediaAsset.uploadedAt = Instant.now(clock);
        MediaAssetResponse response = toResponse(mediaAssetRepository.save(mediaAsset));
        auditService.success("MEDIA_UPLOAD", "MEDIA_ASSET", response.id(), response.title(), null, auditPayload(response));
        return response;
    }

    @Transactional
    public MediaAssetResponse update(Long id, MediaAssetUpdateRequest request) {
        MediaAsset mediaAsset = findAdminAsset(id);
        MediaAssetResponse oldValue = toResponse(mediaAsset);
        mediaAsset.title = required(request.title(), "Media title is required.");
        mediaAsset.altText = blankToNull(request.altText());
        mediaAsset.caption = blankToNull(request.caption());
        mediaAsset.visibility = request.visibility() == null ? MediaVisibility.PRIVATE : request.visibility();
        MediaAssetResponse response = toResponse(mediaAssetRepository.save(mediaAsset));
        auditService.success("MEDIA_UPDATE", "MEDIA_ASSET", response.id(), response.title(), auditPayload(oldValue), auditPayload(response));
        return response;
    }

    @Transactional
    public void delete(Long id) {
        MediaAsset mediaAsset = findAdminAsset(id);
        List<MediaUsageResponse> usages = usagesFor(mediaAsset.id);
        if (!usages.isEmpty()) {
            throw new ApiException(HttpStatus.CONFLICT, "Media asset is used and cannot be deleted. Usages: " + usages.size());
        }
        MediaAssetResponse oldValue = toResponse(mediaAsset);
        mediaAsset.status = MediaAssetStatus.DELETED;
        mediaAsset.deletedAt = Instant.now(clock);
        mediaAssetRepository.save(mediaAsset);
        auditService.success("MEDIA_DELETE", "MEDIA_ASSET", mediaAsset.id, mediaAsset.title, auditPayload(oldValue), Map.of("deletedAt", mediaAsset.deletedAt));
    }

    @Transactional
    public MediaUsageResponse attachUsage(Long mediaAssetId, MediaUsageRequest request) {
        MediaAsset mediaAsset = findAdminAsset(mediaAssetId);
        if (mediaAsset.status != MediaAssetStatus.READY) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only READY media assets can be attached.");
        }
        MediaEntityType entityType = required(request.entityType(), "Media usage entity type is required.");
        Long entityId = request.entityId();
        if (entityId == null || entityId <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Media usage entity id is required.");
        }
        MediaUsageType usageType = required(request.usageType(), "Media usage type is required.");
        MediaUsage usage = mediaUsageRepository
                .findByMediaAssetIdAndEntityTypeAndEntityIdAndUsageType(mediaAssetId, entityType, entityId, usageType)
                .orElseGet(MediaUsage::new);
        usage.mediaAssetId = mediaAssetId;
        usage.entityType = entityType;
        usage.entityId = entityId;
        usage.usageType = usageType;
        MediaUsageResponse response = toUsageResponse(mediaUsageRepository.save(usage));
        auditService.success("MEDIA_USAGE_ATTACH", "MEDIA_ASSET", mediaAsset.id, mediaAsset.title, null, response);
        return response;
    }

    @Transactional(readOnly = true)
    public List<MediaEntityAssetResponse> listEntityMedia(MediaEntityType entityType, Long entityId, boolean publicOnly) {
        if (entityId == null || entityId <= 0) {
            return List.of();
        }
        List<MediaUsage> usages = mediaUsageRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDescIdDesc(
                required(entityType, "Media usage entity type is required."),
                entityId);
        if (usages.isEmpty()) {
            return List.of();
        }
        List<Long> mediaAssetIds = usages.stream()
                .map(usage -> usage.mediaAssetId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new))
                .stream()
                .toList();
        Map<Long, MediaAsset> assets = mediaAssetRepository.findByIdInAndDeletedAtIsNull(mediaAssetIds).stream()
                .collect(Collectors.toMap(asset -> asset.id, Function.identity()));
        return usages.stream()
                .map(usage -> toEntityAssetResponse(usage, assets.get(usage.mediaAssetId)))
                .filter(Objects::nonNull)
                .filter(response -> !publicOnly || (response.status() == MediaAssetStatus.READY
                        && response.visibility() == MediaVisibility.PUBLIC))
                .toList();
    }

    @Transactional
    public void detachUsage(Long mediaAssetId, Long usageId) {
        MediaAsset mediaAsset = findAdminAsset(mediaAssetId);
        MediaUsage usage = mediaUsageRepository.findByIdAndMediaAssetId(usageId, mediaAssetId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Media usage was not found."));
        MediaUsageResponse oldValue = toUsageResponse(usage);
        mediaUsageRepository.delete(usage);
        auditService.success("MEDIA_USAGE_DETACH", "MEDIA_ASSET", mediaAsset.id, mediaAsset.title, oldValue, null);
    }

    @Transactional(readOnly = true)
    public MediaDownload getAdminDownload(Long id) {
        MediaAsset mediaAsset = findAdminAsset(id);
        return toDownload(mediaAsset);
    }

    @Transactional(readOnly = true)
    public MediaDownload getPublicDownload(Long id) {
        MediaAsset mediaAsset = mediaAssetRepository
                .findByIdAndStatusAndVisibilityAndDeletedAtIsNull(id, MediaAssetStatus.READY, MediaVisibility.PUBLIC)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Public media asset was not found."));
        return toDownload(mediaAsset);
    }

    private MediaAsset findAdminAsset(Long id) {
        return mediaAssetRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Media asset was not found."));
    }

    private ValidatedMediaFile validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Media file is required.");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Media file must be 10 MB or smaller.");
        }
        String filename = cleanFilename(file.getOriginalFilename());
        byte[] bytes = readBytes(file);
        String contentType = normalizeContentType(file.getContentType());
        if (!isAllowed(filename, contentType, bytes)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only JPEG, PNG, WebP, GIF, SVG, and PDF media files are accepted.");
        }
        return new ValidatedMediaFile(filename, contentType, bytes);
    }

    private byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Media file could not be read.");
        }
    }

    private boolean isAllowed(String filename, String contentType, byte[] bytes) {
        String lowerName = filename.toLowerCase(Locale.ROOT);
        return switch (contentType) {
            case "image/png" -> lowerName.endsWith(".png") && hasPrefix(bytes, 0x89, 0x50, 0x4E, 0x47);
            case "image/jpeg" -> (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) && hasPrefix(bytes, 0xFF, 0xD8, 0xFF);
            case "image/gif" -> lowerName.endsWith(".gif") && (hasAsciiPrefix(bytes, "GIF87a") || hasAsciiPrefix(bytes, "GIF89a"));
            case "image/webp" -> lowerName.endsWith(".webp") && hasAsciiPrefix(bytes, "RIFF") && bytes.length >= 12 && asciiAt(bytes, 8, "WEBP");
            case "image/svg+xml" -> lowerName.endsWith(".svg") && containsSvg(bytes);
            case "application/pdf" -> lowerName.endsWith(".pdf") && hasAsciiPrefix(bytes, "%PDF");
            default -> false;
        };
    }

    private boolean hasPrefix(byte[] bytes, int... expected) {
        if (bytes.length < expected.length) {
            return false;
        }
        for (int index = 0; index < expected.length; index += 1) {
            if ((bytes[index] & 0xFF) != expected[index]) {
                return false;
            }
        }
        return true;
    }

    private boolean hasAsciiPrefix(byte[] bytes, String expected) {
        return asciiAt(bytes, 0, expected);
    }

    private boolean asciiAt(byte[] bytes, int offset, String expected) {
        if (bytes.length < offset + expected.length()) {
            return false;
        }
        for (int index = 0; index < expected.length(); index += 1) {
            if (bytes[offset + index] != expected.charAt(index)) {
                return false;
            }
        }
        return true;
    }

    private boolean containsSvg(byte[] bytes) {
        String sample = new String(bytes, 0, Math.min(bytes.length, 512)).toLowerCase(Locale.ROOT);
        return sample.contains("<svg");
    }

    private String normalizeContentType(String contentType) {
        String value = required(contentType, "Media content type is required.").toLowerCase(Locale.ROOT);
        int semicolon = value.indexOf(';');
        return semicolon >= 0 ? value.substring(0, semicolon).trim() : value.trim();
    }

    private MediaDownload toDownload(MediaAsset mediaAsset) {
        return new MediaDownload(mediaAsset.originalFilename, mediaAsset.contentType, mediaAsset.fileSize, mediaAsset.fileData);
    }

    private MediaAssetResponse toResponse(MediaAsset mediaAsset) {
        return new MediaAssetResponse(
                mediaAsset.id,
                mediaAsset.originalFilename,
                mediaAsset.contentType,
                mediaAsset.fileSize,
                mediaAsset.title,
                mediaAsset.altText,
                mediaAsset.caption,
                mediaAsset.status,
                mediaAsset.visibility,
                mediaAsset.uploadedAt,
                usagesFor(mediaAsset.id));
    }

    private List<MediaUsageResponse> usagesFor(Long mediaAssetId) {
        return mediaUsageRepository.findByMediaAssetIdOrderByCreatedAtDescIdDesc(mediaAssetId).stream()
                .map(this::toUsageResponse)
                .toList();
    }

    private MediaUsageResponse toUsageResponse(MediaUsage usage) {
        return new MediaUsageResponse(usage.id, usage.entityType, usage.entityId, usage.usageType, usage.createdAt);
    }

    private MediaEntityAssetResponse toEntityAssetResponse(MediaUsage usage, MediaAsset asset) {
        if (asset == null) {
            return null;
        }
        return new MediaEntityAssetResponse(
                usage.id,
                asset.id,
                usage.usageType,
                asset.originalFilename,
                asset.contentType,
                asset.title,
                asset.altText,
                asset.caption,
                asset.status,
                asset.visibility,
                asset.uploadedAt);
    }

    private Map<String, Object> auditPayload(MediaAssetResponse response) {
        return Map.of(
                "id", response.id(),
                "originalFilename", response.originalFilename(),
                "contentType", response.contentType(),
                "fileSize", response.fileSize(),
                "title", response.title(),
                "visibility", response.visibility(),
                "status", response.status(),
                "usageCount", response.usages().size());
    }

    private String cleanFilename(String filename) {
        String value = required(filename, "Media filename is required.").trim();
        return value.replace("\\", "/").replaceAll(".*/", "");
    }

    private String titleFromFilename(String filename) {
        String base = filename.replaceFirst("\\.[^.]+$", "");
        String title = base.replaceAll("[_-]+", " ").trim();
        return title.isBlank() ? filename : title;
    }

    private String required(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, message);
        }
        return value.trim();
    }

    private <T> T required(T value, String message) {
        if (value == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, message);
        }
        return value;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private record ValidatedMediaFile(String filename, String contentType, byte[] bytes) {
    }
}
