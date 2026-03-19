package com.loanmanagement.excel;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

@RestController
@RequestMapping("/api/excel")
@RequiredArgsConstructor
public class ExcelController {

    private final ExcelService excelService;

    // 🔹 Upload Excel
    @PostMapping("/upload")
    public List<List<String>> uploadExcel(@RequestParam("file") MultipartFile file) {

        return excelService.uploadExcel(file);
    }

    // 🔹 Download Excel
    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> downloadExcel() {

        InputStream stream = excelService.downloadExcel();

        InputStreamResource file = new InputStreamResource(stream);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=loans.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(file);
    }
}