package com.samsamhajo.deepground.admin.service;

import com.samsamhajo.deepground.admin.dto.AdminDashboardStatsResponse;
import com.samsamhajo.deepground.admin.repository.AdminDashboardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminDashboardRepository adminDashboardRepository;

    public AdminDashboardStatsResponse getDashboardStats() {
        LocalDateTime today = LocalDate.now().atStartOfDay();

        return new AdminDashboardStatsResponse(
                adminDashboardRepository.countTotalMembers(),
                adminDashboardRepository.countNewMembersToday(today),
                adminDashboardRepository.countTotalPosts(),
                adminDashboardRepository.countTotalReviews(),
                adminDashboardRepository.countReviewsToday(today),
                adminDashboardRepository.countTotalStudyGroups(),
                adminDashboardRepository.countTotalReports(),
                adminDashboardRepository.countTodayReports(today),
                adminDashboardRepository.countPendingReports()
        );
    }
}
