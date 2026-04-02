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

    @Transactional
    public AuthResponseDTO login(LoginRequestDTO loginRequest) {
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

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .address(request.getAddress())
                .role(request.getRole() != null ? request.getRole() : Role.BORROWER)
                .provider("local")
                .active(true)
                .emailVerified(false)
                .build();

        user = userRepository.save(user);

        // Send welcome email (async)
        emailService.sendWelcomeEmail(user.getEmail(), user.getName());

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
}