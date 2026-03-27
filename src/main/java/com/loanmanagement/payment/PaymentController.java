package com.loanmanagement.payment;

import com.loanmanagement.payment.dto.PaymentRequestDTO;
import com.loanmanagement.payment.dto.PaymentResponseDTO;
import com.loanmanagement.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/loan/{loanId}")
    @PreAuthorize("hasRole('ADMIN') or @customSecurityExp.isOwner(authentication, @loanService.getLoanById(#loanId).getUserId())")
    public ResponseEntity<ApiResponse<PaymentResponseDTO>> makePayment(
            @PathVariable Long loanId,
            @Valid @RequestBody PaymentRequestDTO request) {
        
        PaymentResponseDTO response = paymentService.makePayment(loanId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Payment successful"));
    }

    @GetMapping("/loan/{loanId}")
    @PreAuthorize("hasRole('ADMIN') or @customSecurityExp.isOwner(authentication, @loanService.getLoanById(#loanId).getUserId())")
    public ResponseEntity<ApiResponse<List<PaymentResponseDTO>>> getPaymentsByLoan(@PathVariable Long loanId) {
        List<PaymentResponseDTO> payments = paymentService.getPaymentsByLoan(loanId);
        return ResponseEntity.ok(ApiResponse.success(payments, "Payments retrieved successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PaymentResponseDTO>> getPaymentById(@PathVariable Long id) {
        PaymentResponseDTO payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(ApiResponse.success(payment, "Payment retrieved successfully"));
    }
}
