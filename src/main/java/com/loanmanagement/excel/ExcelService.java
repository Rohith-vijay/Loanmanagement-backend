package com.loanmanagement.excel;

import com.loanmanagement.exception.BadRequestException;
import com.loanmanagement.loan.Loan;
import com.loanmanagement.loan.LoanRepository;
import com.loanmanagement.loan.LoanStatus;
import com.loanmanagement.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelService {

    private final LoanRepository loanRepository;
    private final UserRepository userRepository;

    public ByteArrayInputStream generateLoansExcel() {
        log.info("Generating Loans Excel export");
        List<Loan> loans = loanRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Loans");

            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "User ID", "User Name", "Principal Amount", "Interest Rate", "Duration (Months)", "EMI", "Remaining Balance", "Status", "Date"};
            
            CellStyle headerCellStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerCellStyle.setFont(headerFont);

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerCellStyle);
            }

            int rowIdx = 1;
            for (Loan loan : loans) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(loan.getId());
                row.createCell(1).setCellValue(loan.getUser().getId());
                row.createCell(2).setCellValue(loan.getUser().getName());
                row.createCell(3).setCellValue(loan.getPrincipalAmount().doubleValue());
                row.createCell(4).setCellValue(loan.getInterestRate().doubleValue());
                row.createCell(5).setCellValue(loan.getDurationMonths());
                row.createCell(6).setCellValue(loan.getEmiAmount() != null ? loan.getEmiAmount().doubleValue() : 0.0);
                row.createCell(7).setCellValue(loan.getRemainingBalance() != null ? loan.getRemainingBalance().doubleValue() : 0.0);
                row.createCell(8).setCellValue(loan.getStatus().name());
                row.createCell(9).setCellValue(loan.getCreatedAt().toString());
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            log.error("Failed to generate excel file", e);
            throw new RuntimeException("Failed to generate excel file", e);
        }
    }

    public void bulkUploadLoans(MultipartFile file) {
        if (!isValidExcelFile(file)) {
            throw new BadRequestException("Please upload a valid Excel file (.xlsx)");
        }

        log.info("Processing bulk upload of loans");
        List<Loan> loans = new ArrayList<>();

        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheet("Loans");
            if (sheet == null) {
                sheet = workbook.getSheetAt(0); // fallback to first sheet
            }
            Iterator<Row> rows = sheet.iterator();

            int rowNumber = 0;
            while (rows.hasNext()) {
                Row currentRow = rows.next();

                // Skip header
                if (rowNumber == 0) {
                    rowNumber++;
                    continue;
                }

                try {
                    Long userId = (long) currentRow.getCell(0).getNumericCellValue();
                    BigDecimal principalAmount = BigDecimal.valueOf(currentRow.getCell(1).getNumericCellValue());
                    BigDecimal interestRate = BigDecimal.valueOf(currentRow.getCell(2).getNumericCellValue());
                    int duration = (int) currentRow.getCell(3).getNumericCellValue();
                    String statusStr = currentRow.getCell(4).getStringCellValue();
                    
                    userRepository.findById(userId).ifPresent(user -> {
                        Loan loan = Loan.builder()
                                .user(user)
                                .principalAmount(principalAmount)
                                .interestRate(interestRate)
                                .durationMonths(duration)
                                .remainingBalance(principalAmount)
                                .status(LoanStatus.valueOf(statusStr))
                                .purpose("Bulk Upload")
                                .build();
                        loans.add(loan);
                    });
                } catch (Exception e) {
                    log.warn("Failed to parse row {}: {}", rowNumber, e.getMessage());
                }

                rowNumber++;
            }

            if (!loans.isEmpty()) {
                loanRepository.saveAll(loans);
                log.info("Successfully bulk uploaded {} loans", loans.size());
            } else {
                throw new BadRequestException("No valid loans found to upload");
            }

        } catch (IOException e) {
            log.error("Failed to parse Excel file", e);
            throw new BadRequestException("Failed to parse Excel file: " + e.getMessage());
        }
    }

    private boolean isValidExcelFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && 
               (contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") || 
                contentType.equals("application/vnd.ms-excel"));
    }
}