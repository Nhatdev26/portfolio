package com.example.portfolio.profile;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileContentRepository extends JpaRepository<ProfileContent, Long> {

    List<ProfileContent> findByProfileIdAndDeletedAtIsNullOrderByLanguageAsc(Long profileId);

    Optional<ProfileContent> findFirstByProfileIdAndLanguageAndDeletedAtIsNullOrderByIdAsc(
            Long profileId,
            ProfileLanguage language);

    Optional<ProfileContent> findFirstByProfileIdAndLanguageAndStatusAndDeletedAtIsNullOrderByIdAsc(
            Long profileId,
            ProfileLanguage language,
            ProfileContentStatus status);
}
