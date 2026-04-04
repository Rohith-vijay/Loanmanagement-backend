package com.loanmanagement.payment.dto;

import com.loanmanagement.payment.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(source = "loan.id", target = "loanId")
    @Mapping(source = "loan.user.name", target = "userName")
    PaymentResponseDTO paymentToPaymentResponseDTO(Payment payment);
}
