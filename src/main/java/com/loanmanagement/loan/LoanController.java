package com.loanmanagement.loan;

import com.loanmanagement.loan.dto.LoanRequestDTO;
import com.loanmanagement.loan.dto.LoanResponseDTO;
import com.loanmanagement.user.User;
import com.loanmanagement.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @PostMapping("/apply")
    @PreAuthorize("hasAnyRole('BORROWER', 'USER')")
    public ResponseEntity<ApiResponse<LoanResponseDTO>> applyForLoan(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody LoanRequestDTO request) {
        LoanResponseDTO loan = loanService.applyForLoan(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(loan, "Loan application submitted successfully"));
    }

    @GetMapping("/my-loans")
    @PreAuthorize("hasAnyRole('BORROWER', 'USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Page<LoanResponseDTO>>> getMyLoans(
            @AuthenticationPrincipal User currentUser,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        Page<LoanResponseDTO> loans = loanService.getLoansByUser(currentUser.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(loans, "Loans retrieved successfully"));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST', 'LENDER')")
    public ResponseEntity<ApiResponse<Page<LoanResponseDTO>>> getAllLoans(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        Page<LoanResponseDTO> loans = loanService.getAllLoans(pageable);
        return ResponseEntity.ok(ApiResponse.success(loans, "All loans retrieved successfully"));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST', 'LENDER')")
    public ResponseEntity<ApiResponse<Page<LoanResponseDTO>>> getLoansByStatus(
            @PathVariable LoanStatus status,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        Page<LoanResponseDTO> loans = loanService.getLoansByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(loans, "Loans by status retrieved"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST', 'LENDER') or @customSecurityExp.isOwner(authentication, @loanService.getLoanById(#id).getUserId())")
    public ResponseEntity<ApiResponse<LoanResponseDTO>> getLoanById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                loanService.getLoanById(id), "Loan retrieved successfully"));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'LENDER')")
    public ResponseEntity<ApiResponse<LoanResponseDTO>> updateLoanStatus(
            @PathVariable Long id,
            @RequestParam LoanStatus status,
            @RequestParam(required = false) String lenderNote) {
        LoanResponseDTO loan = loanService.updateLoanStatus(id, status, lenderNote);
        return ResponseEntity.ok(ApiResponse.success(loan, "Loan status updated to " + status));
    }
}
