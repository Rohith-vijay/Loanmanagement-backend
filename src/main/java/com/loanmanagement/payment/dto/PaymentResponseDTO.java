package com.loanmanagement.payment.dto;

import com.loanmanagement.payment.PaymentMethod;
import com.loanmanagement.payment.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponseDTO {
    private Long id;
    private Long loanId;
    private String userName;
    private BigDecimal amount;
    private LocalDateTime paymentDate;
    private PaymentStatus status;
    private PaymentMethod paymentMethod;
    private String transactionReference;
    private String failureReason;
    private LocalDateTime createdAt;
}
