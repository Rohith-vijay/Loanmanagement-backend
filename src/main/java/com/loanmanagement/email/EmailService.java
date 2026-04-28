package com.loanmanagement.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Retryable(
            retryFor = {Exception.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public void sendEmail(String to, String subject, String body) {
        log.info("Attempting to send email to {} with subject: {}", to, subject);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}", to, e);
            throw e; // Rethrow to trigger retry
        }
    }

    @Async("emailTaskExecutor")
    public void sendWelcomeEmail(String toEmail, String userName) {
        String subject = "Welcome to Loan Management System";
        String body = String.format("Dear %s,\n\nWelcome to our SaaS Loan Management System. " +
                "Your account has been created successfully.\n\nThank you for choosing us.", userName);
        sendEmail(toEmail, subject, body);
    }

    @Async("emailTaskExecutor")
    public void sendVerificationEmail(String toEmail, String userName, String token) {
        String subject = "Verify Your Email - Loan Management System";
        // In reality, this would be a link to the frontend which then calls the backend API
        String verifyUrl = String.format("http://localhost:5173/verify-email?token=%s", token);
        String body = String.format("Dear %s,\n\nPlease verify your email address to activate your account and gain full access to the Loan Management System.\n\nClick the link below to verify:\n%s\n\nThis link will expire in 24 hours.\n\nThank you.", userName, verifyUrl);
        sendEmail(toEmail, subject, body);
    }

    @Async("emailTaskExecutor")
    public void sendLoanApprovalEmail(String toEmail, String userName, Long loanId) {
        String subject = "Loan Approved - Loan Management System";
        String body = String.format("Dear %s,\n\nYour loan application (ID: %d) has been approved.\n\nThank you for choosing us.", userName, loanId);
        sendEmail(toEmail, subject, body);
    }
    
    @Async("emailTaskExecutor")
    public void sendLoanRejectionEmail(String toEmail, String userName, Long loanId) {
        String subject = "Loan Update - Loan Management System";
        String body = String.format("Dear %s,\n\nWe regret to inform you that your loan application (ID: %d) has been rejected.\n\nThank you for choosing us.", userName, loanId);
        sendEmail(toEmail, subject, body);
    }

    @Async("emailTaskExecutor")
    public void sendPaymentSuccessEmail(String toEmail, String userName, Long loanId, String amount) {
        String subject = "Payment Successful - Loan Management System";
        String body = String.format("Dear %s,\n\nYour payment of ₹%s for Loan ID %d was successful.\n\nThank you.", userName, amount, loanId);
        sendEmail(toEmail, subject, body);
    }

    @Async("emailTaskExecutor")
    public void sendEmiReminderEmail(String toEmail, String userName, Long loanId, String emiAmount, String dueDate) {
        String subject = "EMI Reminder - Loan Management System";
        String body = String.format("Dear %s,\n\nThis is a friendly reminder that your EMI of ₹%s for Loan ID %d is due on %s.\n\nPlease ensure your account has sufficient balance.\n\nThank you.", userName, emiAmount, loanId, dueDate);
        sendEmail(toEmail, subject, body);
    }

    @Async("emailTaskExecutor")
    public void sendPasswordResetEmail(String toEmail, String userName, String token) {
        String subject = "Reset Password - Loan Management System";
        String resetUrl = String.format("http://localhost:5173/reset-password?token=%s", token);
        String body = String.format("Dear %s,\n\nYou have requested to reset your password.\n\nClick the link below to reset it:\n%s\n\nThis link will expire in 1 hour.\n\nIf you did not request this, please ignore this email.\n\nThank you.", userName, resetUrl);
        sendEmail(toEmail, subject, body);
    }
}
