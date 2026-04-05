package com.loanmanagement.audit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAction(String userEmail, String action, String details, String ipAddress) {
        AuditLog log = AuditLog.builder()
                .userEmail(userEmail)
                .action(action)
                .details(details)
                .ipAddress(ipAddress)
                .build();
        auditLogRepository.save(log);
    }

    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAll();
    }

    public List<AuditLog> getRecentLogs(int limit) {
        return auditLogRepository.findAll(
                org.springframework.data.domain.PageRequest.of(0, limit,
                        org.springframework.data.domain.Sort.by("timestamp").descending())
        ).getContent();
    }
}
