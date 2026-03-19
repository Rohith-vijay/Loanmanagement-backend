
package com.loanmanagement.config;                                              
                                                                                
import com.loanmanagement.security.JwtAuthenticationFilter;

import jakarta.servlet.Filter;

import org.springframework.context.annotation.*;                                
import org.springframework.security.config.annotation.web.builders.HttpSecurity;                            
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;                    
                                                                                
@Configuration                                                                  
public class SecurityConfig {                                                   
                                                                                
    private final JwtAuthenticationFilter jwtFilter;                            
                                                                                
    public SecurityConfig(JwtAuthenticationFilter jwtFilter) {                  
        this.jwtFilter = jwtFilter;                                             
    }                                                                           
                                                                                
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/api/auth/**").permitAll()
                    .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter,
                    (Class<? extends Filter>) UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }                                                                      
}                                                                               