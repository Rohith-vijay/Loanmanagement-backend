package com.loanmanagement.email.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoanEvent {
    private String userEmail;
    private String userName;
    private Long loanId;
    private String eventType; // APPROVED, REJECTED
}
