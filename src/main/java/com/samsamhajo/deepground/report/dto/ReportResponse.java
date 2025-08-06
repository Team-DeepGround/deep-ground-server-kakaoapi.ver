package com.samsamhajo.deepground.report.dto;

import com.samsamhajo.deepground.report.entity.Report;
import com.samsamhajo.deepground.report.enums.AIReviewResult;
import com.samsamhajo.deepground.report.enums.ReportReason;
import com.samsamhajo.deepground.report.enums.ReportTargetType;

import java.time.LocalDateTime;

public record ReportResponse(
        Long id,
        ReportTargetType targetType,
        Long targetId,
        ReportReason reason,
        String content,
        boolean isAutoBanned,
        AIReviewResult aiReviewResult,
        LocalDateTime createdAt
) {
    public static ReportResponse from(Report report) {
        return new ReportResponse(
                report.getId(),
                report.getTargetType(),
                report.getTargetId(),
                report.getReason(),
                report.getContent(),
                report.isAutoBanned(),
                report.getAiReviewResult(),
                report.getCreatedAt()
        );
    }
}

