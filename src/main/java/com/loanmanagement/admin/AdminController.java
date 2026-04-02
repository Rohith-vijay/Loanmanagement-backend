package com.loanmanagement.admin;

import com.loanmanagement.admin.dto.SystemOverviewDTO;
import com.loanmanagement.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/overview")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SystemOverviewDTO>> getSystemOverview() {
        SystemOverviewDTO overview = adminService.getSystemOverview();
        return ResponseEntity.ok(ApiResponse.success(overview, "System overview retrieved successfully"));
    }
}
