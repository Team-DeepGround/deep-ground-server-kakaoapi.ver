package com.samsamhajo.deepground.report.dto;

import com.samsamhajo.deepground.member.entity.Member;
import com.samsamhajo.deepground.report.entity.Report;
import com.samsamhajo.deepground.report.enums.AIReviewResult;
import com.samsamhajo.deepground.report.enums.ReportReason;
import com.samsamhajo.deepground.report.enums.ReportTargetType;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ReportDetailResponse(
        Long id,
        ReportTargetType targetType,
        ReportReason reason,
        String content,
        boolean isAutoBanned,
        AIReviewResult aiReviewResult,
        LocalDateTime createdAt,

        // 신고자 정보
        Long reporterId,
        String reporterNickname,

        // 피신고자 정보
        Long reportedMemberId,
        String reportedMemberNickname,

        // 피드 관련 정보 (null 가능)
        Long feedId,
        String feedContent
) {
    public static ReportDetailResponse from(Report report, String feedContent) {
        Member reporter = report.getReporter();
        Member reported = report.getReportedMember();

        return ReportDetailResponse.builder()
                .id(report.getId())
                .targetType(report.getTargetType())
                .reason(report.getReason())
                .content(report.getContent())
                .isAutoBanned(report.isAutoBanned())
                .aiReviewResult(report.getAiReviewResult())
                .createdAt(report.getCreatedAt())
                .reporterId(reporter.getId())
                .reporterNickname(reporter.getNickname())
                .reportedMemberId(reported.getId())
                .reportedMemberNickname(reported.getNickname())
                .feedId(report.getTargetType() == ReportTargetType.FEED ? report.getTargetId() : null)
                .feedContent(feedContent)
                .build();
    }
}
