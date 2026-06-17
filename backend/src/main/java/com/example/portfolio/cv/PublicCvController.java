package com.example.portfolio.cv;

import com.example.portfolio.content.ContentLanguage;
import com.example.portfolio.cv.dto.CvDownload;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public/cv")
public class PublicCvController {

    private final CvFileService cvFileService;

    public PublicCvController(CvFileService cvFileService) {
        this.cvFileService = cvFileService;
    }

    @GetMapping("/download")
    public ResponseEntity<ByteArrayResource> download(
            @RequestParam(defaultValue = "EN") ContentLanguage language,
            @RequestParam(defaultValue = "backend-developer") String targetRole) {
        CvDownload download = cvFileService.getPublicDownload(language, targetRole);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(download.fileSize())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(download.filename())
                        .build()
                        .toString())
                .body(new ByteArrayResource(download.bytes()));
    }
}
