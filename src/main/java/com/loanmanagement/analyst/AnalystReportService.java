package com.loanmanagement.analyst;

import com.loanmanagement.loan.Loan;
import com.loanmanagement.loan.LoanRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnalystReportService {
    
    private final LoanRepository loanRepository;

    public AnalystReportService(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    public String generateCsvReport() {
        List<Loan> loans = loanRepository.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("Loan ID,User ID,Principal,Interest Rate,Duration(Months),Status\n");
        
        for (Loan loan : loans) {
            sb.append(loan.getId()).append(",")
              .append(loan.getUserId()).append(",")
              .append(loan.getPrincipalAmount()).append(",")
              .append(loan.getInterestRate()).append(",")
              .append(loan.getDurationMonths()).append(",")
              .append(loan.getStatus()).append("\n");
        }
        
        return sb.toString();
    }
}