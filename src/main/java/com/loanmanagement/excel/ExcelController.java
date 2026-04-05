package com.loanmanagement.excel;

import com.loanmanagement.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;

@RestController
@RequestMapping("/api/excel")
@RequiredArgsConstructor
public class ExcelController {

    private final ExcelService excelService;

    // ─── Download endpoints ──────────────────────────────────────────────────

    @GetMapping("/download/loans")
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<InputStreamResource> downloadLoansExcel() {
        ByteArrayInputStream in = excelService.generateLoansExcel();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=loans_report.xlsx");
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }

    @GetMapping("/download/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InputStreamResource> downloadUsersExcel() {
        ByteArrayInputStream in = excelService.generateUsersExcel();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=users_report.xlsx");
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }

    @GetMapping("/download/template/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InputStreamResource> downloadUsersTemplate() {
        ByteArrayInputStream in = excelService.generateUsersTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=users_upload_template.xlsx");
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }

    @GetMapping("/download/template/loans")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InputStreamResource> downloadLoansTemplate() {
        ByteArrayInputStream in = excelService.generateLoansTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=loans_upload_template.xlsx");
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }

    // ─── Upload endpoints ─────────────────────────────────────────────────────

    @PostMapping(value = "/upload/loans", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> uploadLoansExcel(@RequestParam("file") MultipartFile file) {
        excelService.bulkUploadLoans(file);
        return ResponseEntity.ok(ApiResponse.success(null, "Loans uploaded successfully"));
    }

    @PostMapping(value = "/upload/users", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> uploadUsersExcel(@RequestParam("file") MultipartFile file) {
        excelService.bulkUploadUsers(file);
        return ResponseEntity.ok(ApiResponse.success(null, "Users uploaded successfully"));
    }
}