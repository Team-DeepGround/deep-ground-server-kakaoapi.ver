package com.samsamhajo.deepground.report.dto;

import com.samsamhajo.deepground.report.enums.AIReviewResult;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AIReportResponse {
    private AIReviewResult result;
    private String reason;
    private double confidence;

    public void setResultByConfidence() {
        if (this.confidence >= 0.75) {
            this.result = AIReviewResult.ACCEPTED;
        } else if (this.confidence < 0.4) {
            this.result = AIReviewResult.REJECTED;
        } else {
            this.result = AIReviewResult.PENDING;
        }
    }
}
