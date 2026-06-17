package com.example.portfolio.taxonomy;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByDeletedAtIsNullOrderByDisplayOrderAscNameAsc();
    List<Category> findByStatusAndDeletedAtIsNullOrderByDisplayOrderAscNameAsc(TaxonomyStatus status);
    Optional<Category> findByIdAndDeletedAtIsNull(Long id);
    Optional<Category> findByIdAndStatusAndDeletedAtIsNull(Long id, TaxonomyStatus status);
    Optional<Category> findBySlugAndDeletedAtIsNull(String slug);
}
