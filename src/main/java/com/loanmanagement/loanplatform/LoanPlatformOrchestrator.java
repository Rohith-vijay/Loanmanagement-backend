package com.loanmanagement.loanplatform;

import com.loanmanagement.email.EmailService;
import com.loanmanagement.loan.Loan;
import com.loanmanagement.loan.LoanRepository;
import com.loanmanagement.loan.LoanService;
import com.loanmanagement.loan.LoanStatus;
import com.loanmanagement.loan.dto.LoanResponseDTO;
import com.loanmanagement.payment.Payment;
import com.loanmanagement.payment.PaymentRepository;
import com.loanmanagement.payment.PaymentStatus;
import com.loanmanagement.risk.RiskService;
import com.loanmanagement.risk.dto.RiskScoreDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanPlatformOrchestrator {

    private final LoanRepository loanRepository;
    private final PaymentRepository paymentRepository;
    private final EmailService emailService;
    private final RiskService riskService;
    private final LoanService loanService;

    @Scheduled(cron = "0 30 1 * * ?") // 1:30 AM every day
    @Transactional
    public void orchestrateDailySystemChecks() {
        log.info("Starting daily system orchestration check");
        processLatePayments();
        checkLoanDefaults();
        log.info("Completed daily system orchestration check");
    }

    private void processLatePayments() {
        List<Payment> pendingPayments = paymentRepository.findAll();
        LocalDateTime daysAgo = LocalDateTime.now().minusDays(5);

        for (Payment payment : pendingPayments) {
            if (payment.getStatus() == PaymentStatus.PENDING && payment.getPaymentDate().isBefore(daysAgo)) {
                payment.setStatus(PaymentStatus.FAILED);
                payment.setFailureReason("Timeout - Payment not received within 5 days");
                paymentRepository.save(payment);
                
                log.warn("Payment {} marked as FAILED due to timeout", payment.getId());
                
                emailService.sendEmail(
                        payment.getLoan().getUser().getEmail(), 
                        "Payment Failed Notice", 
                        "Your initiated payment of " + payment.getAmount() + " was not completed and has been marked as failed."
                );
            }
        }
    }

    private void checkLoanDefaults() {
        List<Loan> activeLoans = loanRepository.findByStatus(LoanStatus.ACTIVE);
        LocalDateTime defaultThresholdDate = LocalDateTime.now().minusDays(90);

        for (Loan loan : activeLoans) {
            List<Payment> payments = paymentRepository.findByLoanIdAndStatus(loan.getId(), PaymentStatus.COMPLETED);
            
            LocalDateTime lastPaymentDate;
            if (payments.isEmpty()) {
                lastPaymentDate = loan.getStartDate();
            } else {
                lastPaymentDate = payments.get(0).getPaymentDate();
            }
            
            if (lastPaymentDate != null && lastPaymentDate.isBefore(defaultThresholdDate)) {
                loan.setStatus(LoanStatus.DEFAULTED);
                loanRepository.save(loan);
                log.error("CRITICAL: Loan {} has defaulted. No payments since {}", loan.getId(), lastPaymentDate);
                
                emailService.sendEmail(
                        loan.getUser().getEmail(), 
                        "URGENT: Loan Default Notice", 
                        "Your loan has officially defaulted. Please contact us immediately."
                );
            }
        }
    }

    @Transactional
    public LoanResponseDTO evaluateAndAutoDecision(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
            .orElseThrow(() -> new com.loanmanagement.exception.ResourceNotFoundException("Loan not found: " + loanId));

        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new com.loanmanagement.exception.BadRequestException("Can only evaluate PENDING loans.");
        }

        RiskScoreDTO riskScore = riskService.calculateRiskScore(
            loan.getUser().getId(), 
            loan.getPrincipalAmount(), 
            loan.getInterestRate()
        );

        loan.setRiskScore(riskScore.getRiskScore());
        
        if ("APPROVE".equals(riskScore.getRecommendation())) {
            return loanService.updateLoanStatus(loanId, LoanStatus.APPROVED, "Auto-approved due to LOW risk");
        } else if ("REJECT".equals(riskScore.getRecommendation())) {
            return loanService.updateLoanStatus(loanId, LoanStatus.REJECTED, "Auto-rejected due to HIGH risk");
        } else {
            // Leave as PENDING for REVIEW
            loan.setLenderNote("Requires manual LENDER review due to MEDIUM risk");
            loanRepository.save(loan);
            return loanService.mapToResponse(loan);
        }
    }
}
