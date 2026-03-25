package com.loanmanagement.analyst;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/analyst/risk")
public class AnalystRiskController {
    
    private final AnalystRiskService riskService;

    public AnalystRiskController(AnalystRiskService riskService) {
        this.riskService = riskService;
    }

    @GetMapping("/assessments")
    public ResponseEntity<List<RiskProfileDTO>> getRiskAssessments() {
        return ResponseEntity.ok(riskService.getRiskAssessments());
    }
}