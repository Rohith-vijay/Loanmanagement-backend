package com.loanmanagement.loan.dto;

import com.loanmanagement.loan.Loan;
import com.loanmanagement.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LoanMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.name", target = "userName")
    @Mapping(source = "user.email", target = "userEmail")
    @Mapping(target = "totalInterestPayable", expression = "java(calculateTotalInterest(loan))")
    LoanResponseDTO loanToLoanResponseDTO(Loan loan);

    default java.math.BigDecimal calculateTotalInterest(Loan loan) {
        if (loan.getEmiAmount() == null || loan.getPrincipalAmount() == null || loan.getDurationMonths() == null) {
            return java.math.BigDecimal.ZERO;
        }
        java.math.BigDecimal totalRepayable = loan.getEmiAmount().multiply(java.math.BigDecimal.valueOf(loan.getDurationMonths()));
        return totalRepayable.subtract(loan.getPrincipalAmount());
    }
}
