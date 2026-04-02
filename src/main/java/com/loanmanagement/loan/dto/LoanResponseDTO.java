package com.loanmanagement.loan.dto;

import com.loanmanagement.loan.LoanStatus;
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
public class LoanResponseDTO {
    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private BigDecimal principalAmount;
    private BigDecimal interestRate;
    private Integer durationMonths;
    private BigDecimal emiAmount;
    private BigDecimal remainingBalance;
    private BigDecimal totalInterestPayable;
    private LoanStatus status;
    private String purpose;
    private String lenderNote;
    private Integer riskScore;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
