package com.example.portfolio.taxonomy;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkillGroupRepository extends JpaRepository<SkillGroup, Long> {
    List<SkillGroup> findByDeletedAtIsNullOrderByDisplayOrderAscNameAsc();
    List<SkillGroup> findByStatusAndDeletedAtIsNullOrderByDisplayOrderAscNameAsc(TaxonomyStatus status);
    Optional<SkillGroup> findByIdAndDeletedAtIsNull(Long id);
    Optional<SkillGroup> findBySlugAndDeletedAtIsNull(String slug);
}
