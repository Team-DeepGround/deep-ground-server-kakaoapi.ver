package com.samsamhajo.deepground.communityPlace.repository;

import com.samsamhajo.deepground.communityPlace.entity.CommunityPlaceReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface CommunityPlaceReviewRepository extends JpaRepository<CommunityPlaceReview,Long> {
    Page<CommunityPlaceReview> findBySpecificAddressId(Long specificAddressId, Pageable pageable);
    Long countByCreatedAtAfter(LocalDateTime createdAtAfter);

    @Query(value = "SELECT r.community_place_reviews_id AS communityPlaceReviewId, " +
            "r.community_place_scope AS scope, " +
            "r.community_place_content AS content, " +
            "r.specific_address_id AS specificAddressId, " +
            "GROUP_CONCAT(m.review_media_url) AS mediaUrls " +
            "FROM community_place_reviews r " +
            "LEFT JOIN community_place_media m ON m.community_place_reviews_id = r.community_place_reviews_id " +
            "JOIN study_schedules s ON s.specific_address_id = r.specific_address_id " +
            "WHERE s.study_schedule_id = :studyScheduleId AND r.member_id = :memberId " +
            "GROUP BY r.community_place_reviews_id " +
            "LIMIT 1",
            nativeQuery = true)
    Object findMyReviewSummaryByScheduleIdAndMemberId(@Param("studyScheduleId") Long studyScheduleId,
                                                      @Param("memberId") Long memberId);
}
