package com.loanmanagement.excel;

import com.loanmanagement.loan.Loan;
import com.loanmanagement.loan.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExcelService {

    private final LoanRepository loanRepository;

    public ByteArrayInputStream generateLoansExcel() {
        List<Loan> loans = loanRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Loans");

            // Header Row
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "User ID", "User Name", "Principal Amount", "EMI", "Remaining Balance", "Status", "Date"};
            
            CellStyle headerCellStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerCellStyle.setFont(headerFont);

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerCellStyle);
            }

            // Data Rows
            int rowIdx = 1;
            for (Loan loan : loans) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(loan.getId());
                row.createCell(1).setCellValue(loan.getUser().getId());
                row.createCell(2).setCellValue(loan.getUser().getName());
                row.createCell(3).setCellValue(loan.getPrincipalAmount().doubleValue());
                if (loan.getEmiAmount() != null) {
                    row.createCell(4).setCellValue(loan.getEmiAmount().doubleValue());
                }
                if (loan.getRemainingBalance() != null) {
                    row.createCell(5).setCellValue(loan.getRemainingBalance().doubleValue());
                }
                row.createCell(6).setCellValue(loan.getStatus());
                if (loan.getCreatedAt() != null) {
                    row.createCell(7).setCellValue(loan.getCreatedAt().toString());
                }
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate excel file", e);
        }
    }
}