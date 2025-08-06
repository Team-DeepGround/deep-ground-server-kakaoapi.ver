package com.samsamhajo.deepground.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

public record AdminDashboardStatsResponse(
        Long totalMembers,
        Long newMembersToday,
        Long totalPosts,
        Long totalReviews,
        Long reviewsToday,
        Long totalStudyGroups,
        Long totalReports,
        Long todayReports,
        Long pendingReports
) {}

