package com.loanmanagement.analytics.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DashboardDTO {
    private long totalActiveUsers;
    private long totalBorrowers;
    private long totalLenders;
    
    private long totalLoansAppiled;
    private long activeLoans;
    private long rejectedLoans;
    
    private BigDecimal totalDisbursedAmount;
    private BigDecimal totalOutstandingBalance;
    private BigDecimal totalCollectedPayments;
    
    private BigDecimal collectionEfficiency; // Collected vs Expected
}
