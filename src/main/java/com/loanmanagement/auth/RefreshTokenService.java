package com.loanmanagement.auth;

import com.loanmanagement.exception.BadRequestException;
import com.loanmanagement.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    @Value("${app.jwt-refresh-expiration-days:7}")
    private int refreshExpirationDays;

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public RefreshToken createRefreshToken(User user) {
        // Delete existing token if present to satisfy unique key constraints
        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusSeconds((long) refreshExpirationDays * 24 * 3600))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verifyRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid refresh token"));

        if (Boolean.TRUE.equals(refreshToken.getRevoked())) {
            throw new BadRequestException("Refresh token has been revoked. Please log in again.");
        }

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new BadRequestException("Refresh token has expired. Please log in again.");
        }

        return refreshToken;
    }

    @Transactional
    public void revokeRefreshToken(User user) {
        refreshTokenRepository.revokeAllByUser(user);
        log.info("Revoked all refresh tokens for user: {}", user.getEmail());
    }

    @Transactional
    public void deleteRefreshToken(User user) {
        refreshTokenRepository.deleteByUser(user);
    }
}
