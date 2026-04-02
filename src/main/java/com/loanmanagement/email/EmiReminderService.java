package com.loanmanagement.email;

import com.loanmanagement.loan.Loan;
import com.loanmanagement.loan.LoanRepository;
import com.loanmanagement.loan.LoanStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmiReminderService {

    private final LoanRepository loanRepository;
    private final EmailService emailService;

    // Run every day at 10 AM
    @Scheduled(cron = "0 0 10 * * ?")
    public void sendEmiReminders() {
        log.info("Starting scheduled EMI reminder job at {}", LocalDateTime.now());
        
        List<Loan> activeLoans = loanRepository.findByStatus(LoanStatus.ACTIVE);
        int count = 0;
        
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
        
        for (Loan loan : activeLoans) {
            // Simplified logic: Assume EMI is due on the 5th of every month
            // If today is the 1st, 2nd, or 3rd, send a reminder
            int dayOfMonth = now.getDayOfMonth();
            if (dayOfMonth >= 1 && dayOfMonth <= 3) {
                // Next 5th of the month
                LocalDateTime dueDate = LocalDateTime.of(now.getYear(), now.getMonth(), 5, 0, 0);
                
                if (loan.getEmiAmount() != null) {
                    emailService.sendEmiReminderEmail(
                            loan.getUser().getEmail(),
                            loan.getUser().getName(),
                            loan.getId(),
                            loan.getEmiAmount().toString(),
                            dueDate.format(formatter)
                    );
                    count++;
                }
            }
        }
        
        log.info("Completed EMI reminder job. Sent {} reminders.", count);
    }
}
