package com.samsamhajo.deepground.admin.controller;

import com.samsamhajo.deepground.admin.dto.AdminDashboardStatsResponse;
import com.samsamhajo.deepground.admin.service.AdminService;
import com.samsamhajo.deepground.admin.success.AdminSuccessCode;
import com.samsamhajo.deepground.global.success.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    public ResponseEntity<SuccessResponse<AdminDashboardStatsResponse>> getDashboardStats() {
        AdminDashboardStatsResponse data = adminService.getDashboardStats();
        return ResponseEntity.ok(SuccessResponse.of(AdminSuccessCode.GET_DASHBOARD_SUCCESS, data));
    }
}
