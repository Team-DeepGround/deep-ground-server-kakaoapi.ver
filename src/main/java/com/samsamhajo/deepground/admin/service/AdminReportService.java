package com.samsamhajo.deepground.admin.service;

import com.samsamhajo.deepground.admin.exception.AdminErrorCode;
import com.samsamhajo.deepground.admin.exception.AdminException;
import com.samsamhajo.deepground.feed.feed.entity.Feed;
import com.samsamhajo.deepground.feed.feed.exception.FeedErrorCode;
import com.samsamhajo.deepground.feed.feed.exception.FeedException;
import com.samsamhajo.deepground.feed.feed.repository.FeedRepository;
import com.samsamhajo.deepground.feed.feed.service.FeedService;
import com.samsamhajo.deepground.member.entity.Member;
import com.samsamhajo.deepground.member.exception.MemberErrorCode;
import com.samsamhajo.deepground.member.exception.MemberException;
import com.samsamhajo.deepground.member.repository.MemberRepository;
import com.samsamhajo.deepground.report.dto.ReportDetailResponse;
import com.samsamhajo.deepground.report.dto.ReportResponse;
import com.samsamhajo.deepground.report.entity.Report;
import com.samsamhajo.deepground.report.enums.AIReviewResult;
import com.samsamhajo.deepground.report.enums.ReportTargetType;
import com.samsamhajo.deepground.report.exception.ReportErrorCode;
import com.samsamhajo.deepground.report.exception.ReportException;
import com.samsamhajo.deepground.report.repository.ReportRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminReportService {

    private final ReportRepository reportRepository;
    private final FeedRepository feedRepository;
    private final MemberRepository memberRepository;
    private final FeedService feedService;

    // 전체 신고 목록 조회
    public Page<ReportResponse> getAllReports(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return reportRepository.findAll(pageable)
                .map(ReportResponse::from);
    }


    /**
     * 관리자 판단이 필요한 신고 목록 조회
     * - AI 판단 결과가 PENDING 이거나
     * - 신고 대상이 MEMBER 인 경우
     */
    public List<ReportResponse> getReportsNeedingAdminReview() {
        return reportRepository.findReportsNeedingAdminReview(ReportTargetType.MEMBER, AIReviewResult.PENDING)
                .stream()
                .map(ReportResponse::from)
                .toList();
    }

    @Transactional
    public ReportDetailResponse getReportDetail(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ReportException(ReportErrorCode.REPORT_NOT_FOUND));

        String feedContent = null;
        if (report.getTargetType() == ReportTargetType.FEED) {
            Feed feed = feedRepository.findById(report.getTargetId())
                    .orElseThrow(() -> new FeedException(FeedErrorCode.FEED_NOT_FOUND));
            feedContent = feed.getContent();
        }

        return ReportDetailResponse.from(report, feedContent);
    }

    @Transactional
    public void deleteReportedFeed(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ReportException(ReportErrorCode.REPORT_NOT_FOUND));

        if (report.getTargetType() != ReportTargetType.FEED) {
            throw new ReportException(ReportErrorCode.INVALID_TYPE);
        }

        Feed feed = feedRepository.findById(report.getTargetId())
                .orElseThrow(() -> new FeedException(FeedErrorCode.FEED_NOT_FOUND));

        feedService.deleteFeed(report.getTargetId());
        report.markAsProcessed("피드 삭제");
    }


    // 멤버 제재 - 정지 일자 설정
    @Transactional
    public void banReportedMember(Long reportId, int days) {
        if (days <= 0) {
            throw new AdminException(AdminErrorCode.INVALID_BAN_DAYS);
        }

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ReportException(ReportErrorCode.REPORT_NOT_FOUND));

        if (report.getTargetType() != ReportTargetType.MEMBER) {
            throw new ReportException(ReportErrorCode.INVALID_TYPE);
        }

        Member member = memberRepository.findById(report.getTargetId())
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        member.applyBanUntil(LocalDateTime.now().plusDays(days));

        report.markAsProcessed("회원 정지 " + days + "일");
    }

    @Transactional
    public void keepReportedFeed(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ReportException(ReportErrorCode.REPORT_NOT_FOUND));

        if (report.getTargetType() != ReportTargetType.FEED) {
            throw new ReportException(ReportErrorCode.INVALID_TYPE);
        }

        report.markAsProcessed("피드 유지");
    }

    @Transactional
    public void keepReportedMember(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ReportException(ReportErrorCode.REPORT_NOT_FOUND));

        if (report.getTargetType() != ReportTargetType.MEMBER) {
            throw new ReportException(ReportErrorCode.INVALID_TYPE);
        }

        report.markAsProcessed("회원 유지");
    }
}
