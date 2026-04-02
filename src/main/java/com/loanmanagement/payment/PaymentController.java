package com.loanmanagement.payment;

import com.loanmanagement.payment.dto.PaymentRequestDTO;
import com.loanmanagement.payment.dto.PaymentResponseDTO;
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
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/loan/{loanId}")
    @PreAuthorize("hasRole('ADMIN') or @customSecurityExp.isOwner(authentication, @loanService.getLoanById(#loanId).getUserId())")
    public ResponseEntity<ApiResponse<PaymentResponseDTO>> makePayment(
            @PathVariable Long loanId,
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody PaymentRequestDTO request) {
        
        // ADMIN can make payment on behalf of user, otherwise user makes it
        Long requestingUserId = currentUser.getRole().name().equals("ADMIN") ? null : currentUser.getId();
        
        PaymentResponseDTO response = paymentService.makePayment(loanId, requestingUserId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Payment successful"));
    }

    @GetMapping("/loan/{loanId}")
    @PreAuthorize("hasRole('ADMIN') or @customSecurityExp.isOwner(authentication, @loanService.getLoanById(#loanId).getUserId())")
    public ResponseEntity<ApiResponse<Page<PaymentResponseDTO>>> getPaymentsByLoan(
            @PathVariable Long loanId,
            @PageableDefault(size = 20, sort = "paymentDate") Pageable pageable) {
        Page<PaymentResponseDTO> payments = paymentService.getPaymentsByLoan(loanId, pageable);
        return ResponseEntity.ok(ApiResponse.success(payments, "Payments retrieved successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PaymentResponseDTO>> getPaymentById(@PathVariable Long id) {
        PaymentResponseDTO payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(ApiResponse.success(payment, "Payment retrieved successfully"));
    }
}
