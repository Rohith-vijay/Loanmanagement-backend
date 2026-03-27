package com.loanmanagement.loan;

import com.loanmanagement.loan.dto.LoanRequestDTO;
import com.loanmanagement.loan.dto.LoanResponseDTO;
import com.loanmanagement.security.JwtTokenProvider;
import com.loanmanagement.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import com.loanmanagement.user.User;
import com.loanmanagement.user.UserRepository;
import com.loanmanagement.exception.ResourceNotFoundException;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;
    private final UserRepository userRepository;

    @PostMapping("/apply")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<LoanResponseDTO>> applyForLoan(
            Authentication authentication,
            @Valid @RequestBody LoanRequestDTO request) {
        
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        LoanResponseDTO loan = loanService.applyForLoan(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(loan, "Loan applied successfully"));
    }

    @GetMapping("/my-loans")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<List<LoanResponseDTO>>> getMyLoans(Authentication authentication) {
        String email = ((UserDetails) authentication.getPrincipal()).getUsername();
        User user = userRepository.findByEmail(email).orElseThrow();
        
        List<LoanResponseDTO> loans = loanService.getLoansByUser(user.getId());
        return ResponseEntity.ok(ApiResponse.success(loans, "User loans retrieved successfully"));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<LoanResponseDTO>>> getAllLoans() {
        List<LoanResponseDTO> loans = loanService.getAllLoans();
        return ResponseEntity.ok(ApiResponse.success(loans, "All loans retrieved successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @customSecurityExp.isOwner(authentication, @loanService.getLoanById(#id).getUserId())")
    public ResponseEntity<ApiResponse<LoanResponseDTO>> getLoanById(@PathVariable Long id) {
        LoanResponseDTO loan = loanService.getLoanById(id);
        return ResponseEntity.ok(ApiResponse.success(loan, "Loan retrieved successfully"));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<LoanResponseDTO>> updateLoanStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        
        LoanResponseDTO loan = loanService.updateLoanStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(loan, "Loan status updated to " + status));
    }
}
