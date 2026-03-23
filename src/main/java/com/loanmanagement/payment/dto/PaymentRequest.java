package com.loanmanagement.payment.dto;

import java.math.BigDecimal;

public class PaymentRequest {

    private Long loanId;
    private BigDecimal amount;
    private String paymentMethod;

    public PaymentRequest() {}

    public Long getLoanId() {
        return loanId;
    }

    public void setLoanId(Long loanId) {
        this.loanId = loanId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
