package com.loanmanagement.email;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    // Test endpoint
    @PostMapping("/send")
    public String sendEmail(@RequestParam String to) {

        emailService.sendEmail(
                to,
                "Test Email",
                "This is a test email from Crediflow."
        );

        return "Email sent successfully!";
    }
}