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
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String providerId = oAuth2User.getAttribute("sub");

        log.info("OAuth2 login successful for email: {}", email);

        // Find or create user
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = User.builder()
                    .email(email)
                    .name(name)
                    .provider("google")
                    .providerId(providerId)
                    .emailVerified(true)
                    .active(true)
                    .build();
            return userRepository.save(newUser);
        });

        // Update OAuth2 fields if user exists
        if (user.getProviderId() == null) {
            user.setProvider("google");
            user.setProviderId(providerId);
            user.setEmailVerified(true);
            userRepository.save(user);
        }

        // Generate JWT
        String jwt = tokenProvider.generateTokenForUser(user);

        // Generate Refresh Token
        com.loanmanagement.auth.RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        // Redirect to frontend with tokens
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUrl)
                .queryParam("token", jwt)
                .queryParam("refreshToken", refreshToken.getToken())
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
