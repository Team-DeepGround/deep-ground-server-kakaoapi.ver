package com.samsamhajo.deepground.admin.repository;

import com.samsamhajo.deepground.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface AdminDashboardRepository extends JpaRepository<Member, Long> {

    @Query("SELECT COUNT(m) FROM Member m")
    Long countTotalMembers();

    @Query("SELECT COUNT(m) FROM Member m WHERE m.createdAt >= :today")
    Long countNewMembersToday(@Param("today") LocalDateTime today);

    @Query("SELECT COUNT(f) FROM Feed f")
    Long countTotalPosts();

    @Query("SELECT COUNT(r) FROM CommunityPlaceReview r")
    Long countTotalReviews();

    @Query("SELECT COUNT(r) FROM CommunityPlaceReview r WHERE r.createdAt >= :today")
    Long countReviewsToday(@Param("today") LocalDateTime today);

    @Query("SELECT COUNT(s) FROM StudyGroup s")
    Long countTotalStudyGroups();
}
