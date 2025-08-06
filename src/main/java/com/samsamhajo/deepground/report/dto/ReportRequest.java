package com.samsamhajo.deepground.report.dto;

import com.samsamhajo.deepground.report.enums.ReportReason;
import com.samsamhajo.deepground.report.enums.ReportTargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReportRequest(
        @NotNull ReportTargetType targetType,
        @NotNull Long targetId,
        @NotNull ReportReason reason,
        @NotBlank String content
) {}
