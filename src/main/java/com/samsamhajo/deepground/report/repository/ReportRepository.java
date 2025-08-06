package com.samsamhajo.deepground.report.repository;

import com.samsamhajo.deepground.report.entity.Report;
import com.samsamhajo.deepground.report.enums.AIReviewResult;
import com.samsamhajo.deepground.report.enums.ReportTargetType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface ReportRepository extends JpaRepository<Report, Long> {
    long countByCreatedAtAfter(LocalDateTime date);

    long countByAiReviewResultOrTargetType(AIReviewResult aiReviewResult, ReportTargetType targetType);
}

