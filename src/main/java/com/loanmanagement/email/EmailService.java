package com.loanmanagement.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@loanmanagement.com");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}", to, e);
        }
    }

    public void sendLoanApprovalEmail(String toEmail, String userName, Long loanId) {
        String subject = "Loan Approved - Loan Management System";
        String body = String.format("Dear %s,\n\nYour loan application (ID: %d) has been approved.\n\nThank you for choosing us.", userName, loanId);
        sendEmail(toEmail, subject, body);
    }
    
    public void sendLoanRejectionEmail(String toEmail, String userName, Long loanId) {
        String subject = "Loan Update - Loan Management System";
        String body = String.format("Dear %s,\n\nWe regret to inform you that your loan application (ID: %d) has been rejected.\n\nThank you for choosing us.", userName, loanId);
        sendEmail(toEmail, subject, body);
    }

    public void sendPaymentSuccessEmail(String toEmail, String userName, Long loanId, String amount) {
        String subject = "Payment Successful - Loan Management System";
        String body = String.format("Dear %s,\n\nYour payment of %s for Loan ID %d was successful.\n\nThank you.", userName, amount, loanId);
        sendEmail(toEmail, subject, body);
    }
}