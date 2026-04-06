package com.loanmanagement.admin;

import com.loanmanagement.admin.dto.SystemOverviewDTO;
import com.loanmanagement.audit.AuditLog;
import com.loanmanagement.audit.AuditService;
import com.loanmanagement.loan.LoanService;
import com.loanmanagement.loan.dto.LoanResponseDTO;
import com.loanmanagement.user.UserService;
import com.loanmanagement.user.dto.UserResponseDTO;
import com.loanmanagement.payment.PaymentService;
import com.loanmanagement.payment.dto.PaymentResponseDTO;
import com.loanmanagement.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final UserService userService;
    private final AuditService auditService;
    private final LoanService loanService;
    private final PaymentService paymentService;

    @GetMapping("/overview")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<ApiResponse<SystemOverviewDTO>> getSystemOverview() {
        SystemOverviewDTO overview = adminService.getSystemOverview();
        return ResponseEntity.ok(ApiResponse.success(overview, "System overview retrieved successfully"));
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserResponseDTO>>> getAllUsers(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<UserResponseDTO> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(ApiResponse.success(users, "Users retrieved successfully"));
    }

    @GetMapping("/audit-logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AuditLog>>> getAuditLogs() {
        List<AuditLog> logs = auditService.getAllLogs();
        return ResponseEntity.ok(ApiResponse.success(logs, "Audit logs retrieved successfully"));
    }

    @GetMapping("/loans")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<LoanResponseDTO>>> getAllLoans(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<LoanResponseDTO> loans = loanService.getAllLoans(pageable);
        return ResponseEntity.ok(ApiResponse.success(loans, "All loans retrieved successfully"));
    }

    @GetMapping("/transactions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<PaymentResponseDTO>>> getAllTransactions(
            @PageableDefault(size = 20, sort = "paymentDate", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        Page<PaymentResponseDTO> transactions = paymentService.getAllPayments(pageable);
        return ResponseEntity.ok(ApiResponse.success(transactions, "All transactions retrieved successfully"));
    }
}
