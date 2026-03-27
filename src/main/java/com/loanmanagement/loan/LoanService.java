package com.loanmanagement.loan;

import com.loanmanagement.exception.BadRequestException;
import com.loanmanagement.exception.ResourceNotFoundException;
import com.loanmanagement.loan.dto.LoanRequestDTO;
import com.loanmanagement.loan.dto.LoanResponseDTO;
import com.loanmanagement.user.User;
import com.loanmanagement.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final UserRepository userRepository;
    private final com.loanmanagement.email.EmailService emailService;

    @Transactional
    public LoanResponseDTO applyForLoan(Long userId, LoanRequestDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        BigDecimal emi = calculateEMI(request.getPrincipalAmount(), request.getInterestRate(), request.getDurationMonths());

        Loan loan = Loan.builder()
                .user(user)
                .principalAmount(request.getPrincipalAmount())
                .interestRate(request.getInterestRate())
                .durationMonths(request.getDurationMonths())
                .emiAmount(emi)
                .remainingBalance(request.getPrincipalAmount())
                .status("PENDING")
                .build();

        loan = loanRepository.save(loan);
        return mapToResponse(loan);
    }

    public List<LoanResponseDTO> getLoansByUser(Long userId) {
        return loanRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<LoanResponseDTO> getAllLoans() {
        return loanRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public LoanResponseDTO getLoanById(Long id) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));
        return mapToResponse(loan);
    }

    @Transactional
    public LoanResponseDTO updateLoanStatus(Long loanId, String status) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        if (!status.equals("APPROVED") && !status.equals("REJECTED") && !status.equals("ACTIVE") && !status.equals("CLOSED")) {
            throw new BadRequestException("Invalid status provided");
        }

        loan.setStatus(status);
        if (status.equals("APPROVED")) {
            loan.setStartDate(LocalDateTime.now());
            loan.setEndDate(LocalDateTime.now().plusMonths(loan.getDurationMonths()));
            emailService.sendLoanApprovalEmail(loan.getUser().getEmail(), loan.getUser().getName(), loan.getId());
        } else if (status.equals("REJECTED")) {
            emailService.sendLoanRejectionEmail(loan.getUser().getEmail(), loan.getUser().getName(), loan.getId());
        }

        return mapToResponse(loanRepository.save(loan));
    }

    private BigDecimal calculateEMI(BigDecimal principal, BigDecimal annualRate, int months) {
        // EMI = P * r * (1 + r)^n / ((1 + r)^n - 1)
        // r = annualRate / (12 * 100)
        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(1200), 10, RoundingMode.HALF_UP);
        
        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);
        }

        BigDecimal onePlusRToN = BigDecimal.ONE.add(monthlyRate).pow(months);
        BigDecimal numerator = principal.multiply(monthlyRate).multiply(onePlusRToN);
        BigDecimal denominator = onePlusRToN.subtract(BigDecimal.ONE);
        
        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }

    public LoanResponseDTO mapToResponse(Loan loan) {
        return LoanResponseDTO.builder()
                .id(loan.getId())
                .userId(loan.getUser().getId())
                .userName(loan.getUser().getName())
                .principalAmount(loan.getPrincipalAmount())
                .interestRate(loan.getInterestRate())
                .durationMonths(loan.getDurationMonths())
                .emiAmount(loan.getEmiAmount())
                .remainingBalance(loan.getRemainingBalance())
                .status(loan.getStatus())
                .startDate(loan.getStartDate())
                .endDate(loan.getEndDate())
                .createdAt(loan.getCreatedAt())
                .build();
    }
}
