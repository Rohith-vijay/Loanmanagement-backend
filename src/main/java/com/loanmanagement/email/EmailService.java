package com.loanmanagement.email;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    // 🔹 Generic Email Sender
    public void sendEmail(String to, String subject, String body) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }

    // 🔹 Registration Email
    public void sendWelcomeEmail(String email, String name) {

        String subject = "Welcome to Crediflow!";
        String body = "Hi " + name + ",\n\n"
                + "Your account has been successfully created.\n\n"
                + "Welcome to Crediflow Smart Loan System.";

        sendEmail(email, subject, body);
    }

    // 🔹 Loan Approval Email
    public void sendLoanApprovalEmail(String email, double amount) {

        String subject = "Loan Approved!";
        String body = "Congratulations!\n\n"
                + "Your loan of ₹" + amount + " has been approved.\n\n"
                + "Thank you for using Crediflow.";

        sendEmail(email, subject, body);
    }

    // 🔹 EMI Reminder Email
    public void sendEmiReminder(String email, double emiAmount) {

        String subject = "EMI Payment Reminder";
        String body = "Reminder:\n\n"
                + "Your EMI payment of ₹" + emiAmount + " is due.\n\n"
                + "Please make the payment on time.";

        sendEmail(email, subject, body);
    }
}