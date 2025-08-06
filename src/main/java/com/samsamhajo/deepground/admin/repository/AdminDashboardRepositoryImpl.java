package com.samsamhajo.deepground.admin.repository;

import com.samsamhajo.deepground.communityPlace.repository.CommunityPlaceReviewRepository;
import com.samsamhajo.deepground.feed.feed.repository.FeedRepository;
import com.samsamhajo.deepground.member.repository.MemberRepository;
import com.samsamhajo.deepground.report.enums.AIReviewResult;
import com.samsamhajo.deepground.report.enums.ReportTargetType;
import com.samsamhajo.deepground.report.repository.ReportRepository;
import com.samsamhajo.deepground.studyGroup.repository.StudyGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
public class AdminDashboardRepositoryImpl implements AdminDashboardRepository{

    private final MemberRepository memberRepository;
    private final FeedRepository feedRepository;
    private final CommunityPlaceReviewRepository communityPlaceReviewRepository;
    private final StudyGroupRepository studyGroupRepository;
    private final ReportRepository reportRepository;

    @Override
    public Long countTotalMembers() {
        return memberRepository.count();
    }

    @Override
    public Long countNewMembersToday(LocalDateTime today) {
        return memberRepository.countByCreatedAtAfter(today);
    }

    @Override
    public Long countTotalPosts() {
        return feedRepository.count();
    }

    @Override
    public Long countTotalReviews() {
        return communityPlaceReviewRepository.count();
    }

    @Override
    public Long countReviewsToday(LocalDateTime today) {
        return communityPlaceReviewRepository.countByCreatedAtAfter(today);
    }

    @Override
    public Long countTotalStudyGroups() {
        return studyGroupRepository.count();
    }

    @Override
    public Long countTotalReports() {
        return reportRepository.count();
    }

    @Override
    public Long countTodayReports(LocalDateTime today) {
        return reportRepository.countByCreatedAtAfter(today);
    }

    @Override
    public Long countPendingReports() {
        return reportRepository.countByAiReviewResultOrTargetType(AIReviewResult.PENDING, ReportTargetType.MEMBER);
    }
}
