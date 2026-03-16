package com.loanmanagement.user.dto;

import com.loanmanagement.user.Role;

public class CreateUserRequestDTO {

    private String name;
    private String email;
    private String password;
    private Role role;
    private String phone;
    private String address;

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }
}