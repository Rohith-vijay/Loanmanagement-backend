package com.loanmanagement.risk.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RiskScoreDTO {
    private Long userId;
    private String name;
    private Integer riskScore; // 0-100 (100 is best)
    private String riskLevel; // LOW, MEDIUM, HIGH
    private String recommendation; // APPROVE, REVIEW, REJECT
    private int totalLoans;
    private int defaultedLoans;
    private int latePayments;
}
