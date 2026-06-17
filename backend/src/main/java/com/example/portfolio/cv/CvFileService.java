package com.example.portfolio.cv;

import com.example.portfolio.common.exception.ApiException;
import com.example.portfolio.content.ContentLanguage;
import com.example.portfolio.cv.dto.CvDownload;
import com.example.portfolio.cv.dto.CvFileResponse;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CvFileService {

    public static final long MAX_FILE_SIZE_BYTES = 5L * 1024L * 1024L;

    private final CvFileRepository cvFileRepository;
    private final Clock clock;

    public CvFileService(CvFileRepository cvFileRepository, Clock clock) {
        this.cvFileRepository = cvFileRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<CvFileResponse> listAdmin() {
        return cvFileRepository.findByDeletedAtIsNullOrderByUploadedAtDescIdDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public CvFileResponse upload(
            MultipartFile file,
            ContentLanguage language,
            String targetRole,
            String version) {
        validatePdf(file);
        CvFile cvFile = new CvFile();
        cvFile.language = language == null ? ContentLanguage.EN : language;
        cvFile.targetRole = normalizeTargetRole(targetRole);
        cvFile.version = required(version, "CV version is required.");
        cvFile.originalFilename = cleanFilename(file.getOriginalFilename());
        cvFile.contentType = "application/pdf";
        cvFile.fileSize = file.getSize();
        cvFile.status = CvFileStatus.DRAFT;
        cvFile.uploadedAt = Instant.now(clock);
        try {
            cvFile.fileData = file.getBytes();
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "CV file could not be read.");
        }
        return toResponse(cvFileRepository.save(cvFile));
    }

    @Transactional
    public CvFileResponse activate(Long id) {
        CvFile cvFile = cvFileRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "CV file was not found."));
        Instant now = Instant.now(clock);
        cvFileRepository.findByLanguageAndTargetRoleAndStatusAndDeletedAtIsNull(
                        cvFile.language,
                        cvFile.targetRole,
                        CvFileStatus.ACTIVE)
                .forEach(existing -> {
                    if (!existing.id.equals(cvFile.id)) {
                        existing.status = CvFileStatus.ARCHIVED;
                        existing.updatedAt = now;
                        cvFileRepository.save(existing);
                    }
                });
        cvFile.status = CvFileStatus.ACTIVE;
        cvFile.activatedAt = now;
        return toResponse(cvFileRepository.save(cvFile));
    }

    @Transactional
    public CvFileResponse archive(Long id) {
        CvFile cvFile = cvFileRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "CV file was not found."));
        cvFile.status = CvFileStatus.ARCHIVED;
        return toResponse(cvFileRepository.save(cvFile));
    }

    @Transactional
    public void delete(Long id) {
        CvFile cvFile = cvFileRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "CV file was not found."));
        cvFile.deletedAt = Instant.now(clock);
        cvFileRepository.save(cvFile);
    }

    @Transactional(readOnly = true)
    public CvDownload getPublicDownload(ContentLanguage language, String targetRole) {
        ContentLanguage normalizedLanguage = language == null ? ContentLanguage.EN : language;
        String normalizedRole = normalizeTargetRole(targetRole == null || targetRole.isBlank()
                ? "backend-developer"
                : targetRole);
        CvFile cvFile = cvFileRepository
                .findFirstByLanguageAndTargetRoleAndStatusAndDeletedAtIsNullOrderByActivatedAtDescIdDesc(
                        normalizedLanguage,
                        normalizedRole,
                        CvFileStatus.ACTIVE)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Active CV was not found."));
        return new CvDownload(
                cvFile.originalFilename,
                cvFile.contentType,
                cvFile.fileSize,
                cvFile.fileData);
    }

    private void validatePdf(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "PDF CV file is required.");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "CV file must be 5 MB or smaller.");
        }
        String filename = cleanFilename(file.getOriginalFilename());
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        boolean acceptableType = "application/pdf".equals(contentType) || "application/octet-stream".equals(contentType);
        if (!filename.toLowerCase(Locale.ROOT).endsWith(".pdf") || !acceptableType || !hasPdfSignature(file)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Only PDF CV files are accepted.");
        }
    }

    private boolean hasPdfSignature(MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            return bytes.length >= 4
                    && bytes[0] == '%'
                    && bytes[1] == 'P'
                    && bytes[2] == 'D'
                    && bytes[3] == 'F';
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "CV file could not be read.");
        }
    }

    private CvFileResponse toResponse(CvFile cvFile) {
        return new CvFileResponse(
                cvFile.id,
                cvFile.language,
                cvFile.targetRole,
                cvFile.version,
                cvFile.originalFilename,
                cvFile.contentType,
                cvFile.fileSize,
                cvFile.status,
                cvFile.uploadedAt,
                cvFile.activatedAt);
    }

    private String normalizeTargetRole(String targetRole) {
        String value = required(targetRole, "Target role is required.")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-+|-+$)", "");
        if (value.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Target role is required.");
        }
        return value;
    }

    private String cleanFilename(String filename) {
        String value = required(filename, "CV filename is required.").trim();
        return value.replace("\\", "/").replaceAll(".*/", "");
    }

    private String required(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, message);
        }
        return value.trim();
    }
}
