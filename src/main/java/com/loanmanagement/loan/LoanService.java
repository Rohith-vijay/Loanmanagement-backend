package com.loanmanagement.loan;

import com.loanmanagement.loan.dto.LoanRequest;
import com.loanmanagement.loan.dto.LoanResponse;
import com.loanmanagement.loan.dto.LoanSummaryDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LoanService {

    @Autowired
    private LoanRepository loanRepository;

    public LoanResponse createLoan(LoanRequest request) {
        Loan loan = new Loan();
        loan.setUserId(request.getUserId());
        loan.setApplicationId(request.getApplicationId());
        loan.setPrincipalAmount(request.getPrincipalAmount());
        loan.setInterestRate(request.getInterestRate());
        loan.setDurationMonths(request.getDurationMonths());
        loan.setRemainingBalance(request.getPrincipalAmount()); // Initially equal to principal
        loan.setStatus("ACTIVE");
        loan.setStartDate(LocalDateTime.now());
        loan.setEndDate(LocalDateTime.now().plusMonths(request.getDurationMonths()));
        
        Loan savedLoan = loanRepository.save(loan);
        return mapToResponse(savedLoan);
    }

    public LoanResponse getLoanById(Long id) {
        Loan loan = loanRepository.findById(id).orElseThrow(() -> new RuntimeException("Loan not found"));
        return mapToResponse(loan);
    }

    public List<LoanResponse> getLoansByUserId(Long userId) {
        return loanRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<LoanSummaryDTO> getLoanSummariesByUserId(Long userId) {
        return loanRepository.findByUserId(userId).stream().map(loan -> {
            LoanSummaryDTO dto = new LoanSummaryDTO();
            dto.setLoanId(loan.getId());
            dto.setPrincipalAmount(loan.getPrincipalAmount());
            dto.setRemainingBalance(loan.getRemainingBalance());
            dto.setStatus(loan.getStatus());
            return dto;
        }).collect(Collectors.toList());
    }

    private LoanResponse mapToResponse(Loan loan) {
        LoanResponse response = new LoanResponse();
        response.setId(loan.getId());
        response.setUserId(loan.getUserId());
        response.setApplicationId(loan.getApplicationId());
        response.setPrincipalAmount(loan.getPrincipalAmount());
        response.setInterestRate(loan.getInterestRate());
        response.setDurationMonths(loan.getDurationMonths());
        response.setRemainingBalance(loan.getRemainingBalance());
        response.setStatus(loan.getStatus());
        response.setStartDate(loan.getStartDate());
        response.setEndDate(loan.getEndDate());
        return response;
    }
}
