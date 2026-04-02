package com.loanmanagement.user.dto;

import com.loanmanagement.user.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDTO {
    private Long id;
    private String name;
    private String email;
    private Role role;
    private String phone;
    private String address;
    private Boolean active;
    private Boolean emailVerified;
    private String provider;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}