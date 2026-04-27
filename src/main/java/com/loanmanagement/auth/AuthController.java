package com.loanmanagement.auth;

import com.loanmanagement.auth.dto.AuthResponseDTO;
import com.loanmanagement.auth.dto.LoginRequestDTO;
import com.loanmanagement.auth.dto.RefreshTokenRequestDTO;
import com.loanmanagement.auth.dto.RegisterRequestDTO;
import com.loanmanagement.auth.dto.ForgotPasswordRequestDTO;
import com.loanmanagement.auth.dto.ResetPasswordRequestDTO;
import com.loanmanagement.user.User;
import com.loanmanagement.user.UserService;
import com.loanmanagement.user.dto.UserResponseDTO;
import com.loanmanagement.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> login(
            @Valid @RequestBody LoginRequestDTO loginRequest) {
        AuthResponseDTO authResponse = authService.login(loginRequest);
        return ResponseEntity.ok(ApiResponse.success(authResponse, "Login successful"));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> register(
            @Valid @RequestBody RegisterRequestDTO registerRequest) {
        AuthResponseDTO authResponse = authService.register(registerRequest);
        return ResponseEntity.ok(ApiResponse.success(authResponse, "Registration successful"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponseDTO>> refresh(
            @Valid @RequestBody RefreshTokenRequestDTO request) {
        AuthResponseDTO authResponse = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success(authResponse, "Token refreshed successfully"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal User currentUser) {
        authService.logout(currentUser);
        return ResponseEntity.ok(ApiResponse.success(null, "Logged out successfully"));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getCurrentUser(
            @AuthenticationPrincipal User currentUser) {
        UserResponseDTO userResponse = userService.mapToResponse(currentUser);
        return ResponseEntity.ok(ApiResponse.success(userResponse, "Current user retrieved"));
    }

    @GetMapping("/oauth2/failure")
    public ResponseEntity<ApiResponse<Void>> oAuth2Failure() {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("OAuth2 login failed. Please try again."));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok(ApiResponse.success(null, "Email verified successfully"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO request) {
        authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success(null, "Password reset link sent to your email"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO request) {
        authService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success(null, "Password reset successfully"));
    }
}