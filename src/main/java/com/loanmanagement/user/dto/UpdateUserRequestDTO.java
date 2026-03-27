package com.loanmanagement.user.dto;

import com.loanmanagement.user.Role;
import lombok.Data;

@Data
public class UpdateUserRequestDTO {
    private String name;
    private String phone;
    private String address;
    private Role role;
    private Boolean active;
}