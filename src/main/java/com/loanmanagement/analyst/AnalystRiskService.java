package com.loanmanagement.analyst;

import com.loanmanagement.loan.Loan;
import com.loanmanagement.loan.LoanRepository;
import com.loanmanagement.loan.application.LoanApplicationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnalystRiskService {
    
    private final LoanRepository loanRepository;

    public AnalystRiskService(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    public List<RiskProfileDTO> getRiskAssessments() {
        return loanRepository.findAll().stream().map(loan -> {
            RiskProfileDTO dto = new RiskProfileDTO();
            dto.setLoanId(loan.getId());
            dto.setApplicationId(loan.getApplicationId());
            
            double score = 30.0; // base score base
            StringBuilder reasons = new StringBuilder();
            
            if (loan.getInterestRate() != null && loan.getInterestRate().doubleValue() > 10.0) {
                score += 20;
                reasons.append("High interest rate. ");
            }
            if (loan.getPrincipalAmount() != null && loan.getPrincipalAmount().doubleValue() > 50000) {
                score += 25;
                reasons.append("Large principal amount. ");
            }
            if ("DEFAULTED".equalsIgnoreCase(loan.getStatus())) {
                score = 99.0;
                reasons.append("History of default. ");
            } else if ("ACTIVE".equalsIgnoreCase(loan.getStatus())) {
                score += 5;
            }

            dto.setRiskScore(Math.min(score, 100.0));
            
            if (dto.getRiskScore() >= 75) {
                dto.setRiskLevel("HIGH");
            } else if (dto.getRiskScore() >= 50) {
                dto.setRiskLevel("MEDIUM");
            } else {
                dto.setRiskLevel("LOW");
            }
            
            dto.setRiskReason(reasons.toString().trim());
            if (dto.getRiskReason().isEmpty()) {
                dto.setRiskReason("Standard profile");
            }
            
            return dto;
        }).collect(Collectors.toList());
    }
}