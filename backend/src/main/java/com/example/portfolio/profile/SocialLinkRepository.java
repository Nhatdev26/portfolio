package com.example.portfolio.profile;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SocialLinkRepository extends JpaRepository<SocialLink, Long> {

    List<SocialLink> findByProfileIdAndDeletedAtIsNullOrderByDisplayOrderAscIdAsc(Long profileId);

    List<SocialLink> findByProfileIdAndStatusAndDeletedAtIsNullOrderByDisplayOrderAscIdAsc(
            Long profileId,
            SocialLinkStatus status);

    Optional<SocialLink> findByIdAndProfileIdAndDeletedAtIsNull(Long id, Long profileId);
}
