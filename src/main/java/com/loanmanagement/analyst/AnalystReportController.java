package com.loanmanagement.analyst;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analyst/report")
public class AnalystReportController {

    private final AnalystReportService reportService;

    public AnalystReportController(AnalystReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/csv")
    public ResponseEntity<byte[]> downloadCsvReport() {
        String csvData = reportService.generateCsvReport();
        byte[] output = csvData.getBytes();
        
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=analyst_monthly_report.csv");
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(output);
    }
}