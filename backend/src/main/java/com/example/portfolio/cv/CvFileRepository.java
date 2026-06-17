package com.example.portfolio.cv;

import com.example.portfolio.content.ContentLanguage;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CvFileRepository extends JpaRepository<CvFile, Long> {
    List<CvFile> findByDeletedAtIsNullOrderByUploadedAtDescIdDesc();

    List<CvFile> findByLanguageAndTargetRoleAndStatusAndDeletedAtIsNull(
            ContentLanguage language,
            String targetRole,
            CvFileStatus status);

    Optional<CvFile> findByIdAndDeletedAtIsNull(Long id);

    Optional<CvFile> findFirstByLanguageAndTargetRoleAndStatusAndDeletedAtIsNullOrderByActivatedAtDescIdDesc(
            ContentLanguage language,
            String targetRole,
            CvFileStatus status);
}
