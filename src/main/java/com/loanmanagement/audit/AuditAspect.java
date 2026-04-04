package com.loanmanagement.audit;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuditAspect {

    @Autowired
    private AuditService auditService;

    @AfterReturning(pointcut = "execution(* com.loanmanagement.*.*Service.*(..)) && !execution(* com.loanmanagement.audit.AuditService.*(..))", returning = "result")
    public void logServiceAccess(JoinPoint joinPoint, Object result) {
        String action = joinPoint.getSignature().toShortString();
        String userEmail = "system";

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !authentication.getPrincipal().equals("anonymousUser")) {
            userEmail = authentication.getName();
        }

        auditService.logAction(userEmail, action, "Method executed successfully", "N/A");
    }
}
