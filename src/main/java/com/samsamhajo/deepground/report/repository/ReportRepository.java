package com.samsamhajo.deepground.report.repository;

import com.samsamhajo.deepground.report.entity.Report;
import com.samsamhajo.deepground.report.enums.AIReviewResult;
import com.samsamhajo.deepground.report.enums.ReportTargetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    long countByCreatedAtAfter(LocalDateTime date);

    long countByAiReviewResultOrTargetType(AIReviewResult aiReviewResult, ReportTargetType targetType);

    boolean existsByReporterIdAndTargetTypeAndTargetId(Long reporterId, ReportTargetType targetType, Long targetId);

    @Query("SELECT r FROM Report r WHERE r.targetType = :memberType OR r.aiReviewResult = :pendingStatus")
    List<Report> findReportsNeedingAdminReview(
            @Param("memberType") ReportTargetType memberType,
            @Param("pendingStatus") AIReviewResult pendingStatus
    );
}

