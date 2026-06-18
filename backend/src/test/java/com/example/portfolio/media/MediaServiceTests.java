package com.example.portfolio.media;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.portfolio.audit.AuditService;
import com.example.portfolio.common.exception.ApiException;
import com.example.portfolio.media.dto.MediaAssetUpdateRequest;
import com.example.portfolio.media.dto.MediaUsageRequest;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
class MediaServiceTests {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);

    @Mock
    private MediaAssetRepository mediaAssetRepository;

    @Mock
    private MediaUsageRepository mediaUsageRepository;

    @Mock
    private AuditService auditService;

    private MediaService mediaService;

    @BeforeEach
    void setUp() {
        mediaService = new MediaService(mediaAssetRepository, mediaUsageRepository, auditService, CLOCK);
        lenient().when(mediaUsageRepository.findByMediaAssetIdOrderByCreatedAtDescIdDesc(anyLong())).thenReturn(List.of());
    }

    @Test
    void uploadAcceptsPngAndCreatesReadyPrivateAsset() {
        MockMultipartFile file = pngFile();
        when(mediaAssetRepository.save(any(MediaAsset.class))).thenAnswer(invocation -> {
            MediaAsset mediaAsset = invocation.getArgument(0);
            mediaAsset.id = 10L;
            return mediaAsset;
        });

        var response = mediaService.upload(file, "", "Cover alt", "Hero cover", null);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.originalFilename()).isEqualTo("cover.png");
        assertThat(response.title()).isEqualTo("cover");
        assertThat(response.altText()).isEqualTo("Cover alt");
        assertThat(response.status()).isEqualTo(MediaAssetStatus.READY);
        assertThat(response.visibility()).isEqualTo(MediaVisibility.PRIVATE);
        verify(auditService).success(any(), any(), any(), any(), any(), any());
    }

    @Test
    void uploadRejectsUnsupportedFileType() {
        MockMultipartFile file = new MockMultipartFile("file", "notes.txt", "text/plain", "hello".getBytes());

        assertThatThrownBy(() -> mediaService.upload(file, "Notes", null, null, MediaVisibility.PRIVATE))
                .isInstanceOfSatisfying(ApiException.class, exception ->
                        assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void publicDownloadRequiresReadyPublicAsset() {
        MediaAsset asset = mediaAsset(20L);
        asset.visibility = MediaVisibility.PUBLIC;
        when(mediaAssetRepository.findByIdAndStatusAndVisibilityAndDeletedAtIsNull(
                20L,
                MediaAssetStatus.READY,
                MediaVisibility.PUBLIC))
                .thenReturn(Optional.of(asset));

        var download = mediaService.getPublicDownload(20L);

        assertThat(download.filename()).isEqualTo("cover.png");
        assertThat(download.contentType()).isEqualTo("image/png");
        assertThat(download.bytes()).isEqualTo(asset.fileData);
    }

    @Test
    void deleteBlocksAssetWithUsages() {
        MediaAsset asset = mediaAsset(30L);
        when(mediaAssetRepository.findByIdAndDeletedAtIsNull(30L)).thenReturn(Optional.of(asset));
        when(mediaUsageRepository.findByMediaAssetIdOrderByCreatedAtDescIdDesc(30L)).thenReturn(List.of(usage(3L, 30L)));

        assertThatThrownBy(() -> mediaService.delete(30L))
                .isInstanceOfSatisfying(ApiException.class, exception ->
                        assertThat(exception.getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void updateMetadataChangesEditorialFields() {
        MediaAsset asset = mediaAsset(40L);
        when(mediaAssetRepository.findByIdAndDeletedAtIsNull(40L)).thenReturn(Optional.of(asset));
        when(mediaAssetRepository.save(asset)).thenReturn(asset);

        var response = mediaService.update(40L, new MediaAssetUpdateRequest(
                "New title",
                "New alt",
                "New caption",
                MediaVisibility.PUBLIC));

        assertThat(response.title()).isEqualTo("New title");
        assertThat(response.altText()).isEqualTo("New alt");
        assertThat(response.visibility()).isEqualTo(MediaVisibility.PUBLIC);
    }

    @Test
    void attachUsageRequiresReadyAssetAndAudits() {
        MediaAsset asset = mediaAsset(50L);
        when(mediaAssetRepository.findByIdAndDeletedAtIsNull(50L)).thenReturn(Optional.of(asset));
        when(mediaUsageRepository.findByMediaAssetIdAndEntityTypeAndEntityIdAndUsageType(
                50L,
                MediaEntityType.PROJECT,
                1L,
                MediaUsageType.COVER_IMAGE))
                .thenReturn(Optional.empty());
        when(mediaUsageRepository.save(any(MediaUsage.class))).thenAnswer(invocation -> {
            MediaUsage usage = invocation.getArgument(0);
            usage.id = 9L;
            usage.createdAt = CLOCK.instant();
            return usage;
        });

        var response = mediaService.attachUsage(50L, new MediaUsageRequest(
                MediaEntityType.PROJECT,
                1L,
                MediaUsageType.COVER_IMAGE));

        assertThat(response.id()).isEqualTo(9L);
        assertThat(response.entityType()).isEqualTo(MediaEntityType.PROJECT);
        verify(auditService).success(any(), any(), any(), any(), any(), any());
    }

    @Test
    void detachUsageDeletesUsageAndAudits() {
        MediaAsset asset = mediaAsset(60L);
        MediaUsage usage = usage(12L, 60L);
        when(mediaAssetRepository.findByIdAndDeletedAtIsNull(60L)).thenReturn(Optional.of(asset));
        when(mediaUsageRepository.findByIdAndMediaAssetId(12L, 60L)).thenReturn(Optional.of(usage));

        mediaService.detachUsage(60L, 12L);

        verify(mediaUsageRepository).delete(usage);
        verify(auditService).success(any(), any(), any(), any(), any(), any());
    }

    private MockMultipartFile pngFile() {
        return new MockMultipartFile(
                "file",
                "cover.png",
                "image/png",
                new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A});
    }

    private MediaAsset mediaAsset(Long id) {
        MediaAsset asset = new MediaAsset();
        asset.id = id;
        asset.originalFilename = "cover.png";
        asset.contentType = "image/png";
        asset.fileSize = 6;
        asset.fileData = new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A};
        asset.title = "Cover";
        asset.status = MediaAssetStatus.READY;
        asset.visibility = MediaVisibility.PRIVATE;
        asset.uploadedAt = CLOCK.instant();
        return asset;
    }

    private MediaUsage usage(Long id, Long mediaAssetId) {
        MediaUsage usage = new MediaUsage();
        usage.id = id;
        usage.mediaAssetId = mediaAssetId;
        usage.entityType = MediaEntityType.PROJECT;
        usage.entityId = 1L;
        usage.usageType = MediaUsageType.COVER_IMAGE;
        usage.createdAt = CLOCK.instant();
        return usage;
    }
}
