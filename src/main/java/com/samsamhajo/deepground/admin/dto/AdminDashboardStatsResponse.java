package com.samsamhajo.deepground.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminDashboardStatsResponse {
    private Long totalMembers;
    private Long newMembersToday;
    private Long totalPosts;
    private Long totalReviews;
    private Long reviewsToday;
    private Long totalStudyGroups;
}
