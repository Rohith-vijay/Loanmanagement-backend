package com.loanmanagement.loan.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LoanRequestDTO {

    @NotNull(message = "Principal amount is required")
    @DecimalMin(value = "1000.00", message = "Minimum loan amount is ₹1,000")
    @DecimalMax(value = "10000000.00", message = "Maximum loan amount is ₹1,00,00,000")
    private BigDecimal principalAmount;

    @NotNull(message = "Interest rate is required")
    @DecimalMin(value = "0.01", message = "Interest rate must be greater than 0")
    @DecimalMax(value = "50.00", message = "Interest rate cannot exceed 50%")
    private BigDecimal interestRate;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 month")
    @Max(value = 360, message = "Duration cannot exceed 360 months (30 years)")
    private Integer durationMonths;

    @Size(max = 200, message = "Purpose cannot exceed 200 characters")
    private String purpose;
}
