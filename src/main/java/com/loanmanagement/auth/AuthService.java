package com.loanmanagement.auth;

import com.loanmanagement.auth.dto.AuthResponseDTO;
import com.loanmanagement.auth.dto.LoginRequestDTO;
import com.loanmanagement.auth.dto.RefreshTokenRequestDTO;
import com.loanmanagement.auth.dto.RegisterRequestDTO;
import com.loanmanagement.email.EmailService;
import com.loanmanagement.exception.BadRequestException;
import com.loanmanagement.security.JwtTokenProvider;
import com.loanmanagement.user.Role;
import com.loanmanagement.user.User;
import com.loanmanagement.user.UserRepository;
import com.loanmanagement.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final EmailService emailService;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;


    @Value("${app.email-verification.enabled:false}")
    private boolean emailVerificationEnabled;

    @Transactional
    public AuthResponseDTO login(LoginRequestDTO loginRequest) {
        User userCheck = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid credentials"));
        
        // Skip email verification check during development
        /*
        if (emailVerificationEnabled && !userCheck.getEmailVerified()) {
            throw new BadRequestException("Please verify your email address before logging in.");
        }
        */

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = (User) authentication.getPrincipal();
        String jwt = tokenProvider.generateToken(authentication);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        log.info("User logged in: {}", user.getEmail());

        return AuthResponseDTO.builder()
                .accessToken(jwt)
                .refreshToken(refreshToken.getToken())
                .user(userService.mapToResponse(user))
                .build();
    }

    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email address already in use");
        }

        boolean autoVerify = !emailVerificationEnabled;

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .address(request.getAddress())
                .role(request.getRole() != null ? request.getRole() : Role.BORROWER)
                .provider("local")
                .active(true)
                .emailVerified(autoVerify)
                .build();

        user = userRepository.save(user);

        if (!autoVerify) {
            log.info("Email verification enabled, generating verification token for: {}", user.getEmail());
            String token = java.util.UUID.randomUUID().toString();
            VerificationToken vt = VerificationToken.builder()
                    .token(token)
                    .user(user)
                    .expiryDate(java.time.LocalDateTime.now().plusHours(24))
                    .build();
            verificationTokenRepository.save(vt);
            emailService.sendVerificationEmail(user.getEmail(), user.getName(), token);

            // Skip login since they must verify first
            return AuthResponseDTO.builder()
                    .accessToken(null)
                    .refreshToken(null)
                    .user(userService.mapToResponse(user))
                    .build();
        } else {
            // Send welcome email (async)
            emailService.sendWelcomeEmail(user.getEmail(), user.getName());
        }

        // Authenticate to generate tokens
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        log.info("New user registered: {}", user.getEmail());

        return AuthResponseDTO.builder()
                .accessToken(jwt)
                .refreshToken(refreshToken.getToken())
                .user(userService.mapToResponse(user))
                .build();
    }

    @Transactional
    public AuthResponseDTO refreshToken(RefreshTokenRequestDTO request) {
        RefreshToken verifiedToken = refreshTokenService.verifyRefreshToken(request.getRefreshToken());
        User user = verifiedToken.getUser();

        // Rotate: delete old, create new
        refreshTokenService.deleteRefreshToken(user);
        String newJwt = tokenProvider.generateTokenForUser(user);
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);

        log.info("Token refreshed for user: {}", user.getEmail());

        return AuthResponseDTO.builder()
                .accessToken(newJwt)
                .refreshToken(newRefreshToken.getToken())
                .user(userService.mapToResponse(user))
                .build();
    }

    @Transactional
    public void logout(User user) {
        refreshTokenService.revokeRefreshToken(user);
        SecurityContextHolder.clearContext();
        log.info("User logged out: {}", user.getEmail());
    }

    @Transactional
    public void verifyEmail(String token) {
        VerificationToken vt = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid verification token"));

        if (vt.getExpiryDate().isBefore(java.time.LocalDateTime.now())) {
            throw new BadRequestException("Verification token has expired");
        }

        User user = vt.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);
        
        verificationTokenRepository.delete(vt);
        log.info("Email verified for user: {}", user.getEmail());
    }

    @Transactional
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("No user found with this email"));

        String token = java.util.UUID.randomUUID().toString();
        PasswordResetToken prt = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(java.time.LocalDateTime.now().plusHours(1))
                .build();
        passwordResetTokenRepository.save(prt);

        emailService.sendPasswordResetEmail(user.getEmail(), user.getName(), token);
        log.info("Password reset email sent to: {}", user.getEmail());
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken prt = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid reset token"));

        if (prt.getExpiryDate().isBefore(java.time.LocalDateTime.now())) {
            throw new BadRequestException("Reset token has expired");
        }

        User user = prt.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        passwordResetTokenRepository.delete(prt);
        log.info("Password reset successfully for user: {}", user.getEmail());
    }
}