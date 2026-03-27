package com.loanmanagement.loan.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class LoanResponseDTO {
    private Long id;
    private Long userId;
    private String userName;
    private BigDecimal principalAmount;
    private BigDecimal interestRate;
    private Integer durationMonths;
    private BigDecimal emiAmount;
    private BigDecimal remainingBalance;
    private String status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime createdAt;
}
