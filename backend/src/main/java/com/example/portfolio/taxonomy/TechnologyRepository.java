package com.example.portfolio.taxonomy;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TechnologyRepository extends JpaRepository<Technology, Long> {
    List<Technology> findByDeletedAtIsNullOrderByDisplayOrderAscNameAsc();
    List<Technology> findByStatusAndDeletedAtIsNullOrderByDisplayOrderAscNameAsc(TaxonomyStatus status);
    List<Technology> findByIdInAndStatusAndDeletedAtIsNull(Collection<Long> ids, TaxonomyStatus status);
    Optional<Technology> findByIdAndDeletedAtIsNull(Long id);
    Optional<Technology> findBySlugAndDeletedAtIsNull(String slug);
    Optional<Technology> findBySlugAndStatusAndDeletedAtIsNull(String slug, TaxonomyStatus status);
}
