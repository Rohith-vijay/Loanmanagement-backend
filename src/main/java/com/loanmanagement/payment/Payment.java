package com.loanmanagement.payment;

import com.loanmanagement.loan.Loan;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Loan loan;

    @Column(nullable = false)
    private BigDecimal amount;

    @Builder.Default
    private LocalDateTime paymentDate = LocalDateTime.now();

    @Column(nullable = false)
    private String status; // PENDING, COMPLETED, FAILED

    @Column(nullable = false)
    private String paymentMethod; // CREDIT_CARD, DEBIT_CARD, UPI, BANK_TRANSFER
    
    private String transactionReference;
}
