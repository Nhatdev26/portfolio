package com.example.portfolio.note;

import com.example.portfolio.content.ContentLanguage;
import com.example.portfolio.content.ContentStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TechnicalNoteRepository extends JpaRepository<TechnicalNote, Long> {
    List<TechnicalNote> findByDeletedAtIsNullOrderByUpdatedAtDescIdDesc();

    List<TechnicalNote> findByIdInAndDeletedAtIsNull(Collection<Long> ids);

    Optional<TechnicalNote> findByIdAndDeletedAtIsNull(Long id);

    Optional<TechnicalNote> findBySlugAndLanguageAndDeletedAtIsNull(String slug, ContentLanguage language);

    Optional<TechnicalNote> findBySlugAndLanguageAndStatusAndDeletedAtIsNull(
            String slug,
            ContentLanguage language,
            ContentStatus status);

    List<TechnicalNote> findByStatusAndDeletedAtIsNullOrderByDisplayOrderAscPublishedAtDescIdDesc(ContentStatus status);
}
