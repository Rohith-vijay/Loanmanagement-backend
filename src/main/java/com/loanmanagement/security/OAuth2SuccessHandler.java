package com.loanmanagement.security;

import com.loanmanagement.auth.RefreshTokenService;
import com.loanmanagement.user.User;
import com.loanmanagement.user.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Value("${app.oauth2.success-redirect-url:http://localhost:3000/oauth2/callback}")
    private String redirectUrl;

    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // Detect provider: Google has 'sub', GitHub has 'id' (Integer)
        String providerName = "google";
        String providerId = oAuth2User.getAttribute("sub");
        if (providerId == null) {
            // GitHub uses integer id
            Object githubId = oAuth2User.getAttribute("id");
            providerId = githubId != null ? String.valueOf(githubId) : null;
            providerName = "github";
        }

        // Email: Google always has it; GitHub may not if user set email private
        String email = oAuth2User.getAttribute("email");
        if (email == null || email.isBlank()) {
            // Fallback: use login@users.noreply.github.com for GitHub
            String login = oAuth2User.getAttribute("login");
            email = (login != null) ? login + "@users.noreply.github.com" : providerId + "@oauth2.noreply";
        }

        // Name: Google → "name", GitHub → "name" (may be null) fallback to "login"
        String name = oAuth2User.getAttribute("name");
        if (name == null || name.isBlank()) {
            name = oAuth2User.getAttribute("login");
        }
        if (name == null) name = email;

        final String resolvedProviderName = providerName;
        final String resolvedProviderId = providerId;

        log.info("OAuth2 login successful for email: {}", email);

        // Find or create user
        final String finalEmail = email;
        final String finalName = name;
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = User.builder()
                    .email(finalEmail)
                    .name(finalName)
                    .provider(resolvedProviderName)
                    .providerId(resolvedProviderId)
                    .emailVerified(true)
                    .active(true)
                    .build();
            return userRepository.save(newUser);
        });

        // Update OAuth2 fields if user exists but wasn't linked via OAuth2
        if (user.getProviderId() == null) {
            user.setProvider(resolvedProviderName);
            user.setProviderId(resolvedProviderId);
            user.setEmailVerified(true);
            userRepository.save(user);
        }

        log.info("OAuth2 user resolved: email={}, provider={}, role={}", user.getEmail(), resolvedProviderName, user.getRole());

        // Generate JWT
        String jwt = tokenProvider.generateTokenForUser(user);

        // Generate Refresh Token
        com.loanmanagement.auth.RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        // Redirect to frontend with tokens + role so frontend can route properly
        String role = user.getRole() != null ? user.getRole().name().toLowerCase() : "borrower";
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUrl)
                .queryParam("token", jwt)
                .queryParam("refreshToken", refreshToken.getToken())
                .queryParam("role", role)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
