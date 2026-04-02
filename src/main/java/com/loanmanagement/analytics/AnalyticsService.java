package com.loanmanagement.analytics;

import com.loanmanagement.analytics.dto.DashboardDTO;
import com.loanmanagement.loan.LoanRepository;
import com.loanmanagement.loan.LoanStatus;
import com.loanmanagement.payment.PaymentRepository;
import com.loanmanagement.user.Role;
import com.loanmanagement.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final UserRepository userRepository;
    private final LoanRepository loanRepository;
    private final PaymentRepository paymentRepository;

    @Transactional(readOnly = true)
    public DashboardDTO getDashboardStats() {
        log.info("Generating dashboard analytics");

        long activeUsers = userRepository.countActiveUsers();
        long totalBorrowers = userRepository.countByRole(Role.BORROWER);
        long totalLenders = userRepository.countByRole(Role.LENDER);

        long totalLoans = loanRepository.countAllLoans();
        long activeLoans = loanRepository.countByStatus(LoanStatus.ACTIVE);
        long rejectedLoans = loanRepository.countByStatus(LoanStatus.REJECTED);

        BigDecimal disbursedAmount = loanRepository.totalDisbursedAmount();
        if (disbursedAmount == null) disbursedAmount = BigDecimal.ZERO;

        BigDecimal outstandingBalance = loanRepository.totalOutstandingBalance();
        if (outstandingBalance == null) outstandingBalance = BigDecimal.ZERO;

        BigDecimal collectedPayments = paymentRepository.totalCompletedPayments();
        if (collectedPayments == null) collectedPayments = BigDecimal.ZERO;

        BigDecimal expectedPayments = disbursedAmount.subtract(outstandingBalance);
        BigDecimal collectionEfficiency = expectedPayments.compareTo(BigDecimal.ZERO) > 0 
                ? collectedPayments.divide(expectedPayments, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        return DashboardDTO.builder()
                .totalActiveUsers(activeUsers)
                .totalBorrowers(totalBorrowers)
                .totalLenders(totalLenders)
                .totalLoansAppiled(totalLoans)
                .activeLoans(activeLoans)
                .rejectedLoans(rejectedLoans)
                .totalDisbursedAmount(disbursedAmount)
                .totalOutstandingBalance(outstandingBalance)
                .totalCollectedPayments(collectedPayments)
                .collectionEfficiency(collectionEfficiency)
                .build();
    }
}
