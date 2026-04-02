package com.loanmanagement.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class SystemOverviewDTO {
    private String systemStatus;
    private String version;
    private Map<String, Long> userDistributionByRole;
    private Map<String, Long> loanDistributionByStatus;
    private Map<String, Long> paymentDistributionByStatus;
    private long totalActiveUsers;
    private long totalAuditEvents; // Mock for now, until Audit module is built
}
