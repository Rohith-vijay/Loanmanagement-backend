package com.loanmanagement.auth;

import com.loanmanagement.auth.dto.LoginRequestDTO;
import com.loanmanagement.auth.dto.RegisterRequestDTO;
import com.loanmanagement.auth.dto.AuthResponseDTO;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // REGISTER
    @PostMapping("/register")
    public AuthResponseDTO register(@RequestBody RegisterRequestDTO request) {

        String message = authService.register(request);

        return new AuthResponseDTO(message);
    }

    // LOGIN
    @PostMapping("/login")
    public AuthResponseDTO login(@RequestBody LoginRequestDTO request) {

        String message = authService.login(request);

        return new AuthResponseDTO(message);
    }
}