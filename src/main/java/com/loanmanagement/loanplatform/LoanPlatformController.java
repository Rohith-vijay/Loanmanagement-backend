package com.loanmanagement.loanplatform;

import com.loanmanagement.loan.LoanService;
import com.loanmanagement.loan.dto.LoanResponseDTO;
import com.loanmanagement.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/platform")
@RequiredArgsConstructor
public class LoanPlatformController {

    private final LoanPlatformOrchestrator orchestrator;

    @PostMapping("/evaluate/{loanId}")
    @PreAuthorize("hasAnyRole('LENDER', 'ADMIN')")
    public ResponseEntity<ApiResponse<LoanResponseDTO>> evaluateAndAutoApprove(@PathVariable Long loanId) {
        LoanResponseDTO loan = orchestrator.evaluateAndAutoDecision(loanId);
        return ResponseEntity.ok(ApiResponse.success(loan, "Loan automatically evaluated and decision applied."));
    }
}
