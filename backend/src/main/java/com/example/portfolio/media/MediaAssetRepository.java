package com.example.portfolio.media;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MediaAssetRepository extends JpaRepository<MediaAsset, Long> {
    List<MediaAsset> findByDeletedAtIsNullOrderByUploadedAtDescIdDesc();

    List<MediaAsset> findByIdInAndDeletedAtIsNull(List<Long> ids);

    Optional<MediaAsset> findByIdAndDeletedAtIsNull(Long id);

    Optional<MediaAsset> findByIdAndStatusAndVisibilityAndDeletedAtIsNull(
            Long id,
            MediaAssetStatus status,
            MediaVisibility visibility);
}
