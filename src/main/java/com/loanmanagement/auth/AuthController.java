package com.loanmanagement.auth;

import com.loanmanagement.auth.dto.AuthResponseDTO;
import com.loanmanagement.auth.dto.LoginRequestDTO;
import com.loanmanagement.auth.dto.RegisterRequestDTO;
import com.loanmanagement.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        AuthResponseDTO authResponse = authService.login(loginRequest);
        return ResponseEntity.ok(ApiResponse.success(authResponse, "User logged in successfully"));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> register(
            @Valid @RequestBody RegisterRequestDTO registerRequest) {
        AuthResponseDTO authResponse = authService.register(registerRequest);
        return ResponseEntity.ok(ApiResponse.success(authResponse, "User registered successfully"));
    }
}