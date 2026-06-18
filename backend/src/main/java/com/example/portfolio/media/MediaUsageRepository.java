package com.example.portfolio.media;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MediaUsageRepository extends JpaRepository<MediaUsage, Long> {
    List<MediaUsage> findByMediaAssetIdOrderByCreatedAtDescIdDesc(Long mediaAssetId);

    boolean existsByMediaAssetId(Long mediaAssetId);

    Optional<MediaUsage> findByIdAndMediaAssetId(Long id, Long mediaAssetId);

    Optional<MediaUsage> findByMediaAssetIdAndEntityTypeAndEntityIdAndUsageType(
            Long mediaAssetId,
            MediaEntityType entityType,
            Long entityId,
            MediaUsageType usageType);
}
