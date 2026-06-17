package com.example.portfolio.cv;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.portfolio.audit.AuditService;
import com.example.portfolio.common.exception.ApiException;
import com.example.portfolio.content.ContentLanguage;
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
class CvFileServiceTests {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);

    @Mock
    private CvFileRepository cvFileRepository;

    @Mock
    private AuditService auditService;

    private CvFileService cvFileService;

    @BeforeEach
    void setUp() {
        cvFileService = new CvFileService(cvFileRepository, auditService, CLOCK);
    }

    @Test
    void uploadAcceptsPdfAndCreatesDraft() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "nhat-cv.pdf",
                "application/pdf",
                "%PDF-1.4".getBytes());
        when(cvFileRepository.save(any(CvFile.class))).thenAnswer(invocation -> {
            CvFile cvFile = invocation.getArgument(0);
            cvFile.id = 10L;
            return cvFile;
        });

        var response = cvFileService.upload(file, ContentLanguage.EN, "Backend Developer", "2026.01");

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.targetRole()).isEqualTo("backend-developer");
        assertThat(response.status()).isEqualTo(CvFileStatus.DRAFT);
        assertThat(response.fileSize()).isEqualTo(file.getSize());
    }

    @Test
    void uploadRejectsNonPdf() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "nhat-cv.txt",
                "text/plain",
                "not a pdf".getBytes());

        assertThatThrownBy(() -> cvFileService.upload(file, ContentLanguage.EN, "Backend", "1"))
                .isInstanceOfSatisfying(ApiException.class, exception ->
                        assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void uploadAcceptsPdfSentAsOctetStreamWhenSignatureMatches() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "nhat-cv.pdf",
                "application/octet-stream",
                "%PDF-1.4".getBytes());
        when(cvFileRepository.save(any(CvFile.class))).thenAnswer(invocation -> {
            CvFile cvFile = invocation.getArgument(0);
            cvFile.id = 11L;
            return cvFile;
        });

        var response = cvFileService.upload(file, ContentLanguage.EN, "Backend Developer", "2026.02");

        assertThat(response.id()).isEqualTo(11L);
        assertThat(response.status()).isEqualTo(CvFileStatus.DRAFT);
    }

    @Test
    void activateArchivesExistingActiveCvForSameLanguageAndRole() {
        CvFile draft = cvFile(20L, CvFileStatus.DRAFT);
        CvFile active = cvFile(21L, CvFileStatus.ACTIVE);
        when(cvFileRepository.findByIdAndDeletedAtIsNull(20L)).thenReturn(Optional.of(draft));
        when(cvFileRepository.findByLanguageAndTargetRoleAndStatusAndDeletedAtIsNull(
                ContentLanguage.EN,
                "backend-developer",
                CvFileStatus.ACTIVE))
                .thenReturn(List.of(active));
        when(cvFileRepository.save(any(CvFile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = cvFileService.activate(20L);

        assertThat(response.status()).isEqualTo(CvFileStatus.ACTIVE);
        assertThat(response.activatedAt()).isEqualTo(CLOCK.instant());
        assertThat(active.status).isEqualTo(CvFileStatus.ARCHIVED);
        verify(cvFileRepository).save(active);
        verify(cvFileRepository).save(draft);
    }

    @Test
    void publicDownloadReturnsActiveCv() {
        CvFile active = cvFile(30L, CvFileStatus.ACTIVE);
        active.fileData = "%PDF-1.4".getBytes();
        when(cvFileRepository.findFirstByLanguageAndTargetRoleAndStatusAndDeletedAtIsNullOrderByActivatedAtDescIdDesc(
                ContentLanguage.EN,
                "backend-developer",
                CvFileStatus.ACTIVE))
                .thenReturn(Optional.of(active));

        var download = cvFileService.getPublicDownload(ContentLanguage.EN, "Backend Developer");

        assertThat(download.filename()).isEqualTo("nhat-cv.pdf");
        assertThat(download.contentType()).isEqualTo("application/pdf");
        assertThat(download.bytes()).isEqualTo(active.fileData);
    }

    private CvFile cvFile(Long id, CvFileStatus status) {
        CvFile cvFile = new CvFile();
        cvFile.id = id;
        cvFile.language = ContentLanguage.EN;
        cvFile.targetRole = "backend-developer";
        cvFile.version = "2026.01";
        cvFile.originalFilename = "nhat-cv.pdf";
        cvFile.contentType = "application/pdf";
        cvFile.fileSize = 8;
        cvFile.fileData = "%PDF-1.4".getBytes();
        cvFile.status = status;
        cvFile.uploadedAt = CLOCK.instant();
        return cvFile;
    }
}
