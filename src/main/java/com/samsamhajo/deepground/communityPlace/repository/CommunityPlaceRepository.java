package com.samsamhajo.deepground.communityPlace.repository;

import com.samsamhajo.deepground.communityPlace.entity.CommunityPlaceReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface CommunityPlaceRepository extends JpaRepository<CommunityPlaceReview,Long> {
    Page<CommunityPlaceReview> findByPlaceId(Long placeId, Pageable pageable);

    Long countByCreatedAtAfter(LocalDateTime createdAtAfter);
}
