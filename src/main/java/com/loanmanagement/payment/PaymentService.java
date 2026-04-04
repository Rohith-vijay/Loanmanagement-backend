package com.loanmanagement.payment;

import com.loanmanagement.email.EmailService;
import com.loanmanagement.exception.BadRequestException;
import com.loanmanagement.exception.ResourceNotFoundException;
import com.loanmanagement.loan.Loan;
import com.loanmanagement.loan.LoanRepository;
import com.loanmanagement.loan.LoanStatus;
import com.loanmanagement.payment.dto.PaymentRequestDTO;
import com.loanmanagement.payment.dto.PaymentResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import com.loanmanagement.email.events.PaymentEvent;
import com.loanmanagement.payment.dto.PaymentMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final LoanRepository loanRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PaymentMapper paymentMapper;

    @Transactional
    public PaymentResponseDTO makePayment(Long loanId, Long userId, PaymentRequestDTO request) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found with id: " + loanId));

        // Security check - if user is making the payment, they must own the loan
        if (userId != null && !loan.getUser().getId().equals(userId)) {
            throw new BadRequestException("You do not have permission to make a payment for this loan.");
        }

        if (loan.getStatus() == LoanStatus.CLOSED || loan.getStatus() == LoanStatus.REJECTED) {
            throw new BadRequestException("Payments cannot be made to a closed or rejected loan.");
        }

        if (request.getAmount().compareTo(loan.getRemainingBalance()) > 0) {
            throw new BadRequestException("Payment amount exceeds the remaining loan balance.");
        }

        String transactionRef = "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Payment payment = Payment.builder()
                .loan(loan)
                .amount(request.getAmount())
                .paymentDate(LocalDateTime.now())
                .status(PaymentStatus.COMPLETED)
                .paymentMethod(request.getPaymentMethod())
                .transactionReference(transactionRef)
                .build();

        payment = paymentRepository.save(payment);

        // Update Loan Remaining Balance
        loan.setRemainingBalance(loan.getRemainingBalance().subtract(request.getAmount()));
        
        if (loan.getRemainingBalance().compareTo(BigDecimal.ZERO) <= 0) {
            loan.setStatus(LoanStatus.CLOSED);
            loan.setRemainingBalance(BigDecimal.ZERO);
            log.info("Loan {} fully paid and closed.", loanId);
        } else if (loan.getStatus() == LoanStatus.PENDING || loan.getStatus() == LoanStatus.APPROVED) {
            loan.setStatus(LoanStatus.ACTIVE); // First payment activates the loan
        }

        loanRepository.save(loan);

        eventPublisher.publishEvent(new PaymentEvent(loan.getUser().getEmail(), loan.getId(), request.getAmount(), "SUCCESS"));
        log.info("Payment {} made for loan {}", transactionRef, loanId);

        return paymentMapper.paymentToPaymentResponseDTO(payment);
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponseDTO> getPaymentsByLoan(Long loanId, Pageable pageable) {
        return paymentRepository.findByLoanId(loanId, pageable).map(paymentMapper::paymentToPaymentResponseDTO);
    }

    @Transactional(readOnly = true)
    public PaymentResponseDTO getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
        return paymentMapper.paymentToPaymentResponseDTO(payment);
    }

    public PaymentResponseDTO mapToResponse(Payment payment) {
        return paymentMapper.paymentToPaymentResponseDTO(payment);
    }
}
