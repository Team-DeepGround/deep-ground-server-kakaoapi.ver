package com.samsamhajo.deepground.report.entity;

import com.samsamhajo.deepground.global.BaseEntity;
import com.samsamhajo.deepground.member.entity.Member;
import com.samsamhajo.deepground.report.enums.AIReviewResult;
import com.samsamhajo.deepground.report.enums.ReportReason;
import com.samsamhajo.deepground.report.enums.ReportTargetType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reports")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Report extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private ReportTargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false)
    private ReportReason reason;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_auto_banned", nullable = false)
    private boolean isAutoBanned;

    // 신고자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private Member reporter;

    // 피신고자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_member_id", nullable = false)
    private Member reportedMember;

    @Enumerated(EnumType.STRING)
    @Column(name = "ai_review_result", nullable = false)
    private AIReviewResult aiReviewResult;

    @Column(name = "is_processed", nullable = false)
    private boolean isProcessed = false;

    public void markAsProcessed() {
        this.isProcessed = true;
    }

    private Report(ReportTargetType targetType, Long targetId, ReportReason reason, String content,
                   boolean isAutoBanned, Member reporter, Member reportedMember, AIReviewResult aiReviewResult) {
        this.targetType = targetType;
        this.targetId = targetId;
        this.reason = reason;
        this.content = content;
        this.isAutoBanned = isAutoBanned;
        this.reporter = reporter;
        this.reportedMember = reportedMember;
        this.aiReviewResult = aiReviewResult;
    }

    public static Report of(ReportTargetType targetType, Long targetId, ReportReason reason, String content,
                            boolean isAutoBanned, Member reporter, Member reportedMember, AIReviewResult aiResult) {
        return new Report(targetType, targetId, reason, content, isAutoBanned, reporter, reportedMember, aiResult);
    }
}
