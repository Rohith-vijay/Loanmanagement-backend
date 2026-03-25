package com.loanmanagement.analyst;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analyst")
public class AnalystController {

    private final AnalystService analystService;

    public AnalystController(AnalystService analystService) {
        this.analystService = analystService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<AnalystDashboardDTO> getDashboard() {
        AnalystDashboardDTO dto = analystService.getDashboardData();
        return ResponseEntity.ok(dto);
    }
}