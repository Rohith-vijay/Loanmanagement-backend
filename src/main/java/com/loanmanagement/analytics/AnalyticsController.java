package com.loanmanagement.analytics;

import com.loanmanagement.analytics.dto.DashboardDTO;
import com.loanmanagement.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<ApiResponse<DashboardDTO>> getDashboardStats() {
        DashboardDTO dashboard = analyticsService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success(dashboard, "Dashboard analytics retrieved successfully"));
    }
}
