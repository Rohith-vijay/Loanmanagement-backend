package com.loanmanagement.email.events;

import com.loanmanagement.email.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailEventListener {

    private final EmailService emailService;

    @Async
    @EventListener
    public void handleLoanEvent(LoanEvent event) {
        if ("APPROVED".equals(event.getEventType())) {
            emailService.sendLoanApprovalEmail(event.getUserEmail(), event.getUserName(), event.getLoanId());
        } else if ("REJECTED".equals(event.getEventType())) {
            emailService.sendLoanRejectionEmail(event.getUserEmail(), event.getUserName(), event.getLoanId());
        }
    }

    @Async
    @EventListener
    public void handlePaymentEvent(PaymentEvent event) {
        if ("SUCCESS".equals(event.getEventType())) {
            emailService.sendEmail(
                    event.getUserEmail(),
                    "Payment Successful",
                    "Your payment of " + event.getAmount() + " for Loan ID " + event.getLoanId() + " was successful."
            );
        }
    }
}
