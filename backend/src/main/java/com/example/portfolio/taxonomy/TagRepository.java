package com.example.portfolio.taxonomy;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {
    List<Tag> findByDeletedAtIsNullOrderByDisplayOrderAscNameAsc();
    List<Tag> findByStatusAndDeletedAtIsNullOrderByDisplayOrderAscNameAsc(TaxonomyStatus status);
    List<Tag> findByIdInAndStatusAndDeletedAtIsNull(Collection<Long> ids, TaxonomyStatus status);
    Optional<Tag> findByIdAndDeletedAtIsNull(Long id);
    Optional<Tag> findBySlugAndDeletedAtIsNull(String slug);
}
