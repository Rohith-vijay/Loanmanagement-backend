package com.loanmanagement.excel;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExcelService {

    private final ExcelHelper excelHelper;

    // 🔹 Upload Excel
    public List<List<String>> uploadExcel(MultipartFile file) {

        try {
            InputStream is = file.getInputStream();
            return excelHelper.readExcel(is);

        } catch (Exception e) {
            throw new RuntimeException("Error processing file");
        }
    }

    // 🔹 Download Excel
    public InputStream downloadExcel() {

        // Sample data (replace later with DB data)
        List<List<String>> data = List.of(
                List.of("ID", "Name", "Amount"),
                List.of("1", "Loan A", "50000"),
                List.of("2", "Loan B", "75000")
        );

        return excelHelper.writeExcel(data);
    }
}