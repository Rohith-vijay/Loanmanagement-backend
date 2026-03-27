package com.loanmanagement.payment;

import com.loanmanagement.exception.BadRequestException;
import com.loanmanagement.exception.ResourceNotFoundException;
import com.loanmanagement.loan.Loan;
import com.loanmanagement.loan.LoanRepository;
import com.loanmanagement.payment.dto.PaymentRequestDTO;
import com.loanmanagement.payment.dto.PaymentResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final LoanRepository loanRepository;
    private final com.loanmanagement.email.EmailService emailService;

    @Transactional
    public PaymentResponseDTO makePayment(Long loanId, PaymentRequestDTO request) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        if ("CLOSED".equals(loan.getStatus()) || "REJECTED".equals(loan.getStatus())) {
            throw new BadRequestException("Payments cannot be made to a closed or rejected loan.");
        }

        if (request.getAmount().compareTo(loan.getRemainingBalance()) > 0) {
            throw new BadRequestException("Payment amount exceeds the remaining loan balance.");
        }

        String transactionRef = UUID.randomUUID().toString();

        Payment payment = Payment.builder()
                .loan(loan)
                .amount(request.getAmount())
                .paymentDate(LocalDateTime.now())
                .status("COMPLETED")
                .paymentMethod(request.getPaymentMethod())
                .transactionReference(transactionRef)
                .build();

        payment = paymentRepository.save(payment);

        // Update Loan Remaining Balance
        loan.setRemainingBalance(loan.getRemainingBalance().subtract(request.getAmount()));
        
        if (loan.getRemainingBalance().compareTo(BigDecimal.ZERO) == 0) {
            loan.setStatus("CLOSED");
        } else if ("PENDING".equals(loan.getStatus()) || "APPROVED".equals(loan.getStatus())) {
            loan.setStatus("ACTIVE"); // First payment activates the loan
        }

        loanRepository.save(loan);

        emailService.sendPaymentSuccessEmail(loan.getUser().getEmail(), loan.getUser().getName(), loan.getId(), request.getAmount().toString());

        return mapToResponse(payment);
    }

    public List<PaymentResponseDTO> getPaymentsByLoan(Long loanId) {
        return paymentRepository.findByLoanId(loanId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public PaymentResponseDTO getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
        return mapToResponse(payment);
    }

    public PaymentResponseDTO mapToResponse(Payment payment) {
        return PaymentResponseDTO.builder()
                .id(payment.getId())
                .loanId(payment.getLoan().getId())
                .amount(payment.getAmount())
                .paymentDate(payment.getPaymentDate())
                .status(payment.getStatus())
                .paymentMethod(payment.getPaymentMethod())
                .transactionReference(payment.getTransactionReference())
                .build();
    }
}
