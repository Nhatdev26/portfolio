package com.example.portfolio.media;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public/media-assets")
public class PublicMediaController {

    private final MediaService mediaService;

    public PublicMediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @GetMapping("/{id}/content")
    public ResponseEntity<byte[]> content(@PathVariable Long id) {
        return MediaAdminController.responseFor(mediaService.getPublicDownload(id));
    }
}
