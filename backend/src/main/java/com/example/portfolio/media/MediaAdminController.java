package com.example.portfolio.media;

import com.example.portfolio.media.dto.MediaAssetResponse;
import com.example.portfolio.media.dto.MediaAssetUpdateRequest;
import com.example.portfolio.media.dto.MediaDownload;
import com.example.portfolio.media.dto.MediaUsageRequest;
import com.example.portfolio.media.dto.MediaUsageResponse;
import java.util.List;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/media-assets")
public class MediaAdminController {

    private final MediaService mediaService;

    public MediaAdminController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @GetMapping
    public List<MediaAssetResponse> list() {
        return mediaService.listAdmin();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MediaAssetResponse upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String altText,
            @RequestParam(required = false) String caption,
            @RequestParam(defaultValue = "PRIVATE") MediaVisibility visibility) {
        return mediaService.upload(file, title, altText, caption, visibility);
    }

    @PutMapping("/{id}")
    public MediaAssetResponse update(@PathVariable Long id, @RequestBody MediaAssetUpdateRequest request) {
        return mediaService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        mediaService.delete(id);
    }

    @GetMapping("/{id}/content")
    public ResponseEntity<byte[]> content(@PathVariable Long id) {
        return responseFor(mediaService.getAdminDownload(id));
    }

    @PostMapping("/{id}/usages")
    @ResponseStatus(HttpStatus.CREATED)
    public MediaUsageResponse attachUsage(@PathVariable Long id, @RequestBody MediaUsageRequest request) {
        return mediaService.attachUsage(id, request);
    }

    @DeleteMapping("/{id}/usages/{usageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void detachUsage(@PathVariable Long id, @PathVariable Long usageId) {
        mediaService.detachUsage(id, usageId);
    }

    static ResponseEntity<byte[]> responseFor(MediaDownload download) {
        ContentDisposition disposition = ContentDisposition.inline()
                .filename(download.filename())
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(download.fileSize()))
                .header(HttpHeaders.CONTENT_TYPE, download.contentType())
                .body(download.bytes());
    }
}
