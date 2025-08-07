package com.samsamhajo.deepground.admin.repository;

import com.samsamhajo.deepground.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface AdminDashboardRepository {
    Long countTotalMembers();
    Long countNewMembersToday(LocalDateTime today);
    Long countTotalPosts();
    Long countTotalReviews();
    Long countReviewsToday(LocalDateTime today);
    Long countTotalStudyGroups();
    Long countTotalReports();
    Long countTodayReports(LocalDateTime today);
    Long countPendingReports();
}
