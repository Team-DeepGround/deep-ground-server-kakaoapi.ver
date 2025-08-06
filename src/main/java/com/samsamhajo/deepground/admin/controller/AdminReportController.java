package com.samsamhajo.deepground.admin.controller;

import com.samsamhajo.deepground.admin.service.AdminReportService;
import com.samsamhajo.deepground.admin.success.AdminSuccessCode;
import com.samsamhajo.deepground.global.success.SuccessResponse;
import com.samsamhajo.deepground.report.dto.ReportDetailResponse;
import com.samsamhajo.deepground.report.dto.ReportResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/report")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminReportController {

    private final AdminReportService adminReportService;

    @GetMapping("/pending")
    public ResponseEntity<SuccessResponse<List<ReportResponse>>> getPendingOrMemberReports() {
        List<ReportResponse> responses = adminReportService.getReportsNeedingAdminReview();
        return ResponseEntity.ok(SuccessResponse.of(AdminSuccessCode.GET_REPORT_SUCCESS, responses));
    }

    @GetMapping
    public ResponseEntity<SuccessResponse<Page<ReportResponse>>> getAllReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<ReportResponse> responses = adminReportService.getAllReports(page, size);
        return ResponseEntity.ok(SuccessResponse.of(AdminSuccessCode.GET_REPORT_SUCCESS, responses));
    }

    @GetMapping("/{reportId}")
    public ResponseEntity<SuccessResponse<ReportDetailResponse>> getReportDetail(@PathVariable Long reportId) {
        ReportDetailResponse response = adminReportService.getReportDetail(reportId);
        return ResponseEntity.ok(SuccessResponse.of(AdminSuccessCode.GET_REPORT_DETAIL_SUCCESS, response));
    }

    @PostMapping("/{reportId}/keep-feed")
    public ResponseEntity<Void> keepFeed(@PathVariable Long reportId) {
        adminReportService.keepReportedFeed(reportId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{reportId}/keep-member")
    public ResponseEntity<Void> keepMember(@PathVariable Long reportId) {
        adminReportService.keepReportedMember(reportId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{reportId}/delete-feed")
    public ResponseEntity<Void> deleteFeed(@PathVariable Long reportId) {
        adminReportService.deleteReportedFeed(reportId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{reportId}/ban-member")
    public ResponseEntity<SuccessResponse<Void>> banMember(
            @PathVariable Long reportId,
            @RequestParam("days") int banDays
    ) {
        adminReportService.banReportedMember(reportId, banDays);
        return ResponseEntity.ok(SuccessResponse.of(AdminSuccessCode.BAN_MEMBER_SUCCESS));
    }
}
