package com.loanmanagement.user.dto;

import com.loanmanagement.user.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponseDTO {
    private Long id;
    private String name;
    private String email;
    private Role role;
    private String phone;
    private String address;
    private Boolean active;
    private LocalDateTime createdAt;
}