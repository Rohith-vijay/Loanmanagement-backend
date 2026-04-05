package com.loanmanagement.analytics;

import com.loanmanagement.analytics.dto.DashboardDTO;
import com.loanmanagement.audit.AuditLog;
import com.loanmanagement.audit.AuditService;
import com.loanmanagement.loan.LoanRepository;
import com.loanmanagement.loan.LoanStatus;
import com.loanmanagement.user.UserRepository;
import com.loanmanagement.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final LoanRepository loanRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<ApiResponse<DashboardDTO>> getDashboardStats() {
        DashboardDTO dashboard = analyticsService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success(dashboard, "Dashboard analytics retrieved successfully"));
    }

    @GetMapping("/loans/by-status")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getLoansByStatus() {
        Map<String, Long> statusMap = new LinkedHashMap<>();
        for (LoanStatus status : LoanStatus.values()) {
            statusMap.put(status.name(), loanRepository.countByStatus(status));
        }
        return ResponseEntity.ok(ApiResponse.success(statusMap, "Loan distribution by status"));
    }

    @GetMapping("/loans/by-purpose")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getLoansByPurpose() {
        Map<String, Long> purposeMap = new LinkedHashMap<>();
        List<Object[]> results = loanRepository.countByPurpose();
        for (Object[] row : results) {
            purposeMap.put(String.valueOf(row[0]), ((Number) row[1]).longValue());
        }
        return ResponseEntity.ok(ApiResponse.success(purposeMap, "Loan distribution by purpose"));
    }

    @GetMapping("/users/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserSummary() {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalUsers", userRepository.count());
        summary.put("activeUsers", userRepository.countActiveUsers());
        summary.put("totalBorrowers", userRepository.countByRole(com.loanmanagement.user.Role.BORROWER));
        summary.put("totalLenders", userRepository.countByRole(com.loanmanagement.user.Role.LENDER));
        summary.put("totalAnalysts", userRepository.countByRole(com.loanmanagement.user.Role.ANALYST));
        summary.put("totalAdmins", userRepository.countByRole(com.loanmanagement.user.Role.ADMIN));
        return ResponseEntity.ok(ApiResponse.success(summary, "User summary retrieved"));
    }

    @GetMapping("/audit/recent")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<ApiResponse<List<AuditLog>>> getRecentAuditLogs() {
        List<AuditLog> logs = auditService.getRecentLogs(50);
        return ResponseEntity.ok(ApiResponse.success(logs, "Recent audit logs retrieved"));
    }
}
