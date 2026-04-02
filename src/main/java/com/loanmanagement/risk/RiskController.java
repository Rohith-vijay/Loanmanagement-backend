package com.loanmanagement.risk;

import com.loanmanagement.risk.dto.RiskScoreDTO;
import com.loanmanagement.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/risk")
@RequiredArgsConstructor
public class RiskController {

    private final RiskService riskService;

    @GetMapping("/users/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST', 'LENDER') or @customSecurityExp.isOwner(authentication, #userId)")
    public ResponseEntity<ApiResponse<RiskScoreDTO>> getUserRiskScore(@PathVariable Long userId) {
        RiskScoreDTO riskScore = riskService.calculateRiskScore(userId);
        return ResponseEntity.ok(ApiResponse.success(riskScore, "Risk score calculated successfully"));
    }
}
