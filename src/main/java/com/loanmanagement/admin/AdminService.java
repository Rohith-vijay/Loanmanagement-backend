package com.loanmanagement.admin;

import com.loanmanagement.admin.dto.SystemOverviewDTO;
import com.loanmanagement.loan.LoanRepository;
import com.loanmanagement.loan.LoanStatus;
import com.loanmanagement.payment.PaymentRepository;
import com.loanmanagement.payment.PaymentStatus;
import com.loanmanagement.user.Role;
import com.loanmanagement.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepository userRepository;
    private final LoanRepository loanRepository;
    private final PaymentRepository paymentRepository;

    public SystemOverviewDTO getSystemOverview() {
        log.info("Fetching system overview for admin dashboard");

        Map<String, Long> userRoleDist = new java.util.HashMap<>();
        for (Role role : Role.values()) {
            userRoleDist.put(role.name(), userRepository.countByRole(role));
        }

        Map<String, Long> loanStatusDist = new java.util.HashMap<>();
        for (LoanStatus status : LoanStatus.values()) {
            loanStatusDist.put(status.name(), loanRepository.countByStatus(status));
        }

        Map<String, Long> paymentStatusDist = new java.util.HashMap<>();
        for (PaymentStatus status : PaymentStatus.values()) {
            paymentStatusDist.put(status.name(), paymentRepository.countByStatus(status));
        }

        return SystemOverviewDTO.builder()
                .systemStatus("HEALTHY")
                .version("1.0.0-PROD")
                .userDistributionByRole(userRoleDist)
                .loanDistributionByStatus(loanStatusDist)
                .paymentDistributionByStatus(paymentStatusDist)
                .totalActiveUsers(userRepository.countActiveUsers())
                .totalAuditEvents(1024L) // Currently mocked until full Audit module is operational
                .build();
    }
}
