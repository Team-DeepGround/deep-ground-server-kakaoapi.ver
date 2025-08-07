package com.samsamhajo.deepground.report.dto;

import com.samsamhajo.deepground.report.enums.AIReviewResult;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AIReportResponse {
    private static final double HIGH_CONFIDENCE_THRESHOLD = 0.75;
    private static final double LOW_CONFIDENCE_THRESHOLD = 0.4;

    private AIReviewResult result;
    private String reason;
    private double confidence;

    private AIReportResponse(AIReviewResult result, String reason, double confidence) {
        this.result = result;
        this.reason = reason;
        this.confidence = confidence;
    }

    public void setResultByConfidence() {
        if (this.confidence >= HIGH_CONFIDENCE_THRESHOLD) {
            this.result = AIReviewResult.ACCEPTED;
        } else if (this.confidence < LOW_CONFIDENCE_THRESHOLD) {
            this.result = AIReviewResult.REJECTED;
        } else {
            this.result = AIReviewResult.PENDING;
        }
    }

    public static AIReportResponse createPendingResponse() {
        return new AIReportResponse(
                AIReviewResult.PENDING,
                "AI 판단 실패 - 수동 검토 필요",
                0.0
        );
    }
}
