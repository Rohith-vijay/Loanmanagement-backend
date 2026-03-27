package com.loanmanagement.security;

import com.loanmanagement.user.User;
import com.loanmanagement.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component("customSecurityExp")
@RequiredArgsConstructor
public class CustomSecurityExpression {

    private final UserRepository userRepository;

    public boolean isOwner(Authentication authentication, Long userId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserDetails)) {
            return false;
        }

        String email = ((UserDetails) principal).getUsername();
        User user = userRepository.findByEmail(email).orElse(null);
        
        return user != null && user.getId().equals(userId);
    }
}
