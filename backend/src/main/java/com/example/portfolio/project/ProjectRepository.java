package com.example.portfolio.project;

import com.example.portfolio.content.ContentLanguage;
import com.example.portfolio.content.ContentStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByDeletedAtIsNullOrderByUpdatedAtDescIdDesc();

    List<Project> findByIdInAndDeletedAtIsNull(Collection<Long> ids);

    Optional<Project> findByIdAndDeletedAtIsNull(Long id);

    Optional<Project> findBySlugAndLanguageAndDeletedAtIsNull(String slug, ContentLanguage language);

    Optional<Project> findBySlugAndLanguageAndContentStatusAndDeletedAtIsNull(
            String slug,
            ContentLanguage language,
            ContentStatus status);

    List<Project> findByContentStatusAndDeletedAtIsNullOrderByDisplayOrderAscPublishedAtDescIdDesc(ContentStatus status);
}
