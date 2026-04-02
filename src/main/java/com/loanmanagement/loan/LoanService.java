package com.loanmanagement.loan;

import com.loanmanagement.email.EmailService;
import com.loanmanagement.exception.BadRequestException;
import com.loanmanagement.exception.ResourceNotFoundException;
import com.loanmanagement.loan.dto.LoanRequestDTO;
import com.loanmanagement.loan.dto.LoanResponseDTO;
import com.loanmanagement.user.User;
import com.loanmanagement.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoanService {

    private final LoanRepository loanRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Transactional
    public LoanResponseDTO applyForLoan(Long userId, LoanRequestDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        BigDecimal emi = calculateEMI(request.getPrincipalAmount(),
                request.getInterestRate(), request.getDurationMonths());

        Loan loan = Loan.builder()
                .user(user)
                .principalAmount(request.getPrincipalAmount())
                .interestRate(request.getInterestRate())
                .durationMonths(request.getDurationMonths())
                .emiAmount(emi)
                .remainingBalance(request.getPrincipalAmount())
                .status(LoanStatus.PENDING)
                .purpose(request.getPurpose())
                .build();

        loan = loanRepository.save(loan);
        log.info("Loan application submitted: loanId={}, userId={}, amount={}",
                loan.getId(), userId, request.getPrincipalAmount());

        return mapToResponse(loan);
    }

    @Transactional(readOnly = true)
    public Page<LoanResponseDTO> getLoansByUser(Long userId, Pageable pageable) {
        return loanRepository.findByUserId(userId, pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<LoanResponseDTO> getAllLoans(Pageable pageable) {
        return loanRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<LoanResponseDTO> getLoansByStatus(LoanStatus status, Pageable pageable) {
        return loanRepository.findByStatus(status, pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public LoanResponseDTO getLoanById(Long id) {
        Loan loan = findLoanById(id);
        return mapToResponse(loan);
    }

    @Transactional
    public LoanResponseDTO updateLoanStatus(Long loanId, LoanStatus newStatus, String lenderNote) {
        Loan loan = findLoanById(loanId);

        // Status transition validation
        validateStatusTransition(loan.getStatus(), newStatus);

        loan.setStatus(newStatus);
        if (lenderNote != null) loan.setLenderNote(lenderNote);

        if (newStatus == LoanStatus.APPROVED) {
            loan.setStartDate(LocalDateTime.now());
            loan.setEndDate(LocalDateTime.now().plusMonths(loan.getDurationMonths()));
            emailService.sendLoanApprovalEmail(
                    loan.getUser().getEmail(), loan.getUser().getName(), loan.getId());
        } else if (newStatus == LoanStatus.REJECTED) {
            emailService.sendLoanRejectionEmail(
                    loan.getUser().getEmail(), loan.getUser().getName(), loan.getId());
        }

        log.info("Loan status updated: loanId={}, newStatus={}", loanId, newStatus);
        return mapToResponse(loanRepository.save(loan));
    }

    private void validateStatusTransition(LoanStatus current, LoanStatus next) {
        boolean valid = switch (current) {
            case PENDING -> next == LoanStatus.APPROVED || next == LoanStatus.REJECTED;
            case APPROVED -> next == LoanStatus.ACTIVE || next == LoanStatus.REJECTED;
            case ACTIVE -> next == LoanStatus.CLOSED || next == LoanStatus.DEFAULTED;
            default -> false;
        };
        if (!valid) {
            throw new BadRequestException(
                    "Invalid status transition from " + current + " to " + next);
        }
    }

    public BigDecimal calculateEMI(BigDecimal principal, BigDecimal annualRate, int months) {
        // EMI = P * r * (1 + r)^n / ((1 + r)^n - 1)
        // r = monthly interest rate = annualRate / (12 * 100)
        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(1200), 10, RoundingMode.HALF_UP);

        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);
        }

        BigDecimal onePlusRToN = BigDecimal.ONE.add(monthlyRate).pow(months);
        BigDecimal numerator = principal.multiply(monthlyRate).multiply(onePlusRToN);
        BigDecimal denominator = onePlusRToN.subtract(BigDecimal.ONE);

        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateTotalInterest(Loan loan) {
        if (loan.getEmiAmount() == null) return BigDecimal.ZERO;
        BigDecimal totalRepayable = loan.getEmiAmount()
                .multiply(BigDecimal.valueOf(loan.getDurationMonths()));
        return totalRepayable.subtract(loan.getPrincipalAmount());
    }

    public LoanResponseDTO mapToResponse(Loan loan) {
        return LoanResponseDTO.builder()
                .id(loan.getId())
                .userId(loan.getUser().getId())
                .userName(loan.getUser().getName())
                .userEmail(loan.getUser().getEmail())
                .principalAmount(loan.getPrincipalAmount())
                .interestRate(loan.getInterestRate())
                .durationMonths(loan.getDurationMonths())
                .emiAmount(loan.getEmiAmount())
                .remainingBalance(loan.getRemainingBalance())
                .totalInterestPayable(calculateTotalInterest(loan))
                .status(loan.getStatus())
                .purpose(loan.getPurpose())
                .lenderNote(loan.getLenderNote())
                .riskScore(loan.getRiskScore())
                .startDate(loan.getStartDate())
                .endDate(loan.getEndDate())
                .createdAt(loan.getCreatedAt())
                .updatedAt(loan.getUpdatedAt())
                .build();
    }

    public Loan findLoanById(Long id) {
        return loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found with id: " + id));
    }
}
