package com.samsamhajo.deepground.report.controller;

import com.samsamhajo.deepground.auth.security.CustomUserDetails;
import com.samsamhajo.deepground.global.success.SuccessResponse;
import com.samsamhajo.deepground.member.entity.Member;
import com.samsamhajo.deepground.report.dto.ReportRequest;
import com.samsamhajo.deepground.report.dto.ReportResponse;
import com.samsamhajo.deepground.report.service.ReportService;
import com.samsamhajo.deepground.report.success.ReportSuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<SuccessResponse<ReportResponse>> createReport(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ReportRequest request
            ) {
        Member reporter = userDetails.getMember();
        ReportResponse response = reportService.createReport(request, reporter.getId());
        return ResponseEntity
                .status(ReportSuccessCode.CREATE_SUCCESS.getStatus())
                .body(SuccessResponse.of(ReportSuccessCode.CREATE_SUCCESS, response));
    }
}
