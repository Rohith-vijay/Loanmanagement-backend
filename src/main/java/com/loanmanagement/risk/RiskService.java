package com.loanmanagement.risk;

import com.loanmanagement.exception.ResourceNotFoundException;
import com.loanmanagement.loan.Loan;
import com.loanmanagement.loan.LoanRepository;
import com.loanmanagement.loan.LoanStatus;
import com.loanmanagement.payment.Payment;
import com.loanmanagement.payment.PaymentRepository;
import com.loanmanagement.payment.PaymentStatus;
import com.loanmanagement.risk.dto.RiskScoreDTO;
import com.loanmanagement.user.User;
import com.loanmanagement.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RiskService {

    private final UserRepository userRepository;
    private final LoanRepository loanRepository;
    private final PaymentRepository paymentRepository;

    public RiskScoreDTO calculateRiskScore(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        List<Loan> userLoans = loanRepository.findByUserId(userId);
        
        int baseScore = 70; // Default average score for new users
        int score = baseScore;
        int defaultedLoansCount = 0;
        int latePaymentsCount = 0; // In a real system, we'd compare payment date vs due date

        if (!userLoans.isEmpty()) {
            for (Loan loan : userLoans) {
                if (loan.getStatus() == LoanStatus.CLOSED) {
                    score += 10; // Bonus for completing loans
                } else if (loan.getStatus() == LoanStatus.DEFAULTED) {
                    score -= 30; // Huge penalty for defaulting
                    defaultedLoansCount++;
                }

                // Check payments for this loan
                List<Payment> payments = paymentRepository.findByLoanId(loan.getId());
                for (Payment payment : payments) {
                    if (payment.getStatus() == PaymentStatus.FAILED) {
                        score -= 5;
                        latePaymentsCount++;
                    }
                }
            }
        }

        // Cap score inside 0-100
        score = Math.max(0, Math.min(100, score));

        String riskLevel;
        String recommendation;

        if (score >= 70) {
            riskLevel = "LOW";
            recommendation = "APPROVE";
        } else if (score >= 40) {
            riskLevel = "MEDIUM";
            recommendation = "REVIEW";
        } else {
            riskLevel = "HIGH";
            recommendation = "REJECT";
        }

        log.info("Calculated risk score {} for user {}", score, userId);

        return RiskScoreDTO.builder()
                .userId(userId)
                .name(user.getName())
                .riskScore(score)
                .riskLevel(riskLevel)
                .recommendation(recommendation)
                .totalLoans(userLoans.size())
                .defaultedLoans(defaultedLoansCount)
                .latePayments(latePaymentsCount)
                .build();
    }
}
