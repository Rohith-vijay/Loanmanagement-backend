package com.loanmanagement.loan.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LoanApplicationService {

    @Autowired
    private LoanApplicationRepository loanApplicationRepository;

    public LoanApplication createApplication(LoanApplication application) {
        application.setStatus("PENDING");
        return loanApplicationRepository.save(application);
    }

    public LoanApplication getApplicationById(Long id) {
        return loanApplicationRepository.findById(id).orElseThrow(() -> new RuntimeException("Application not found"));
    }

    public List<LoanApplication> getApplicationsByUserId(Long userId) {
        return loanApplicationRepository.findByUserId(userId);
    }

    public LoanApplication updateApplicationStatus(Long id, String status) {
        LoanApplication application = getApplicationById(id);
        application.setStatus(status);
        return loanApplicationRepository.save(application);
    }
}
