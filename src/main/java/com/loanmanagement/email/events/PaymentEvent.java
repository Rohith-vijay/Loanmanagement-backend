package com.loanmanagement.email.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class PaymentEvent {
    private String userEmail;
    private Long loanId;
    private BigDecimal amount;
    private String eventType; // SUCCESS
}
