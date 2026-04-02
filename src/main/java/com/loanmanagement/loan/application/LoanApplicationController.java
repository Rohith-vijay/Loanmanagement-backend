package com.loanmanagement.loan.application;

import com.loanmanagement.loan.LoanService;
import com.loanmanagement.loan.LoanStatus;
import com.loanmanagement.loan.dto.LoanRequestDTO;
import com.loanmanagement.loan.dto.LoanResponseDTO;
import com.loanmanagement.user.User;
import com.loanmanagement.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Dedicated controller for the Loan Application & Workflow process as requested.
 */
@RestController
@RequestMapping("/api/loan-workflow")
@RequiredArgsConstructor
public class LoanApplicationController {

    private final LoanService loanService;

    @PostMapping("/apply")
    @PreAuthorize("hasAnyRole('BORROWER', 'USER')")
    public ResponseEntity<ApiResponse<LoanResponseDTO>> applyForLoan(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody LoanRequestDTO request) {
        
        // Borrower applies for loan
        LoanResponseDTO loan = loanService.applyForLoan(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(loan, "Loan application submitted successfully. Pending review."));
    }

    @PostMapping("/{loanId}/approve")
    @PreAuthorize("hasAnyRole('LENDER', 'ADMIN')")
    public ResponseEntity<ApiResponse<LoanResponseDTO>> approveLoan(
            @PathVariable Long loanId,
            @RequestParam(required = false) String remarks) {
        
        // Lender approval workflow
        LoanResponseDTO loan = loanService.updateLoanStatus(loanId, LoanStatus.APPROVED, remarks);
        return ResponseEntity.ok(ApiResponse.success(loan, "Loan officially APPROVED."));
    }

    @PostMapping("/{loanId}/reject")
    @PreAuthorize("hasAnyRole('LENDER', 'ADMIN')")
    public ResponseEntity<ApiResponse<LoanResponseDTO>> rejectLoan(
            @PathVariable Long loanId,
            @RequestParam(required = false) String remarks) {
        
        // Lender rejection workflow
        LoanResponseDTO loan = loanService.updateLoanStatus(loanId, LoanStatus.REJECTED, remarks);
        return ResponseEntity.ok(ApiResponse.success(loan, "Loan officially REJECTED."));
    }
}
