package com.loanmanagement.payment;

import com.loanmanagement.payment.dto.PaymentRequest;
import com.loanmanagement.payment.dto.PaymentResponse;
import com.loanmanagement.payment.transaction.Transaction;
import com.loanmanagement.payment.transaction.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    public PaymentResponse processPayment(PaymentRequest request) {
        Payment payment = new Payment();
        payment.setLoanId(request.getLoanId());
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setStatus("COMPLETED");
        payment.setPaymentDate(LocalDateTime.now());
        
        Payment savedPayment = paymentRepository.save(payment);

        Transaction transaction = new Transaction();
        transaction.setPaymentId(savedPayment.getId());
        transaction.setTransactionReference(UUID.randomUUID().toString());
        transaction.setAmount(savedPayment.getAmount());
        transaction.setStatus("SUCCESS");
        transaction.setTransactionDate(LocalDateTime.now());
        
        transactionRepository.save(transaction);

        return mapToResponse(savedPayment);
    }

    public PaymentResponse getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id).orElseThrow(() -> new RuntimeException("Payment not found"));
        return mapToResponse(payment);
    }

    public List<PaymentResponse> getPaymentsByLoanId(Long loanId) {
        return paymentRepository.findByLoanId(loanId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private PaymentResponse mapToResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setLoanId(payment.getLoanId());
        response.setAmount(payment.getAmount());
        response.setPaymentDate(payment.getPaymentDate());
        response.setStatus(payment.getStatus());
        response.setPaymentMethod(payment.getPaymentMethod());
        return response;
    }
}
