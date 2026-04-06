package com.loanmanagement.excel;

import com.loanmanagement.exception.BadRequestException;
import com.loanmanagement.loan.Loan;
import com.loanmanagement.loan.LoanRepository;
import com.loanmanagement.loan.LoanStatus;
import com.loanmanagement.payment.Payment;
import com.loanmanagement.payment.PaymentRepository;
import com.loanmanagement.user.Role;
import com.loanmanagement.user.User;
import com.loanmanagement.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final PasswordEncoder passwordEncoder;
    private final PaymentRepository paymentRepository;

    // ─── GENERATE EXPORTS ─────────────────────────────────────────────────────

    public ByteArrayInputStream generateLoansExcel() {
        log.info("Generating Loans Excel export");
        List<Loan> loans = loanRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Loans");
            CellStyle headerStyle = createHeaderStyle(workbook);

            String[] columns = {"ID", "User ID", "User Name", "Principal Amount", "Interest Rate",
                    "Duration (Months)", "EMI", "Remaining Balance", "Status", "Purpose", "Created At"};
            createHeaderRow(sheet, headerStyle, columns);

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
                row.createCell(9).setCellValue(loan.getPurpose() != null ? loan.getPurpose() : "");
                row.createCell(10).setCellValue(loan.getCreatedAt().toString());
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            log.error("Failed to generate loans excel", e);
            throw new RuntimeException("Failed to generate excel file", e);
        }
    }

    public ByteArrayInputStream generatePaymentsExcel() {
        log.info("Generating Payments Excel export");
        List<Payment> payments = paymentRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Payments");
            CellStyle headerStyle = createHeaderStyle(workbook);

            String[] columns = {"ID", "Loan ID", "User ID", "User Name", "Amount", "Status",
                    "Payment Method", "Transaction Reference", "Payment Date", "Created At"};
            createHeaderRow(sheet, headerStyle, columns);

            int rowIdx = 1;
            for (Payment payment : payments) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(payment.getId());
                row.createCell(1).setCellValue(payment.getLoan().getId());
                row.createCell(2).setCellValue(payment.getLoan().getUser().getId());
                row.createCell(3).setCellValue(payment.getLoan().getUser().getName());
                row.createCell(4).setCellValue(payment.getAmount().doubleValue());
                row.createCell(5).setCellValue(payment.getStatus().name());
                row.createCell(6).setCellValue(payment.getPaymentMethod() != null ? payment.getPaymentMethod().name() : "");
                row.createCell(7).setCellValue(payment.getTransactionReference() != null ? payment.getTransactionReference() : "");
                row.createCell(8).setCellValue(payment.getPaymentDate() != null ? payment.getPaymentDate().toString() : "");
                row.createCell(9).setCellValue(payment.getCreatedAt() != null ? payment.getCreatedAt().toString() : "");
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            log.error("Failed to generate payments excel", e);
            throw new RuntimeException("Failed to generate payments excel", e);
        }
    }

    public ByteArrayInputStream generateUsersExcel() {
        log.info("Generating Users Excel export");
        List<User> users = userRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Users");
            CellStyle headerStyle = createHeaderStyle(workbook);

            String[] columns = {"ID", "Name", "Email", "Role", "Phone", "Address", "Active", "Email Verified", "Provider", "Created At"};
            createHeaderRow(sheet, headerStyle, columns);

            int rowIdx = 1;
            for (User user : users) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(user.getId());
                row.createCell(1).setCellValue(user.getName());
                row.createCell(2).setCellValue(user.getEmail());
                row.createCell(3).setCellValue(user.getRole().name());
                row.createCell(4).setCellValue(user.getPhone() != null ? user.getPhone() : "");
                row.createCell(5).setCellValue(user.getAddress() != null ? user.getAddress() : "");
                row.createCell(6).setCellValue(Boolean.TRUE.equals(user.getActive()) ? "Yes" : "No");
                row.createCell(7).setCellValue(Boolean.TRUE.equals(user.getEmailVerified()) ? "Yes" : "No");
                row.createCell(8).setCellValue(user.getProvider() != null ? user.getProvider() : "local");
                row.createCell(9).setCellValue(user.getCreatedAt().toString());
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            log.error("Failed to generate users excel", e);
            throw new RuntimeException("Failed to generate users excel", e);
        }
    }

    public ByteArrayInputStream generateUsersTemplate() {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Users");
            CellStyle headerStyle = createHeaderStyle(workbook);

            String[] columns = {"Name (required)", "Email (required)", "Password (required)", "Role (BORROWER/LENDER/ANALYST/ADMIN)", "Phone", "Address"};
            createHeaderRow(sheet, headerStyle, columns);

            // Sample row
            Row sample = sheet.createRow(1);
            sample.createCell(0).setCellValue("John Doe");
            sample.createCell(1).setCellValue("john.doe@example.com");
            sample.createCell(2).setCellValue("SecurePass123!");
            sample.createCell(3).setCellValue("BORROWER");
            sample.createCell(4).setCellValue("+91-9876543210");
            sample.createCell(5).setCellValue("123 Main Street, City");

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate template", e);
        }
    }

    public ByteArrayInputStream generateLoansTemplate() {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Loans");
            CellStyle headerStyle = createHeaderStyle(workbook);

            String[] columns = {"User ID (required)", "Principal Amount (required)", "Interest Rate (required)",
                    "Duration Months (required)", "Status (PENDING/APPROVED/ACTIVE)", "Purpose (PERSONAL/HOME/BUSINESS/EDUCATION/AUTO)"};
            createHeaderRow(sheet, headerStyle, columns);

            // Sample row
            Row sample = sheet.createRow(1);
            sample.createCell(0).setCellValue(1);
            sample.createCell(1).setCellValue(50000);
            sample.createCell(2).setCellValue(10.5);
            sample.createCell(3).setCellValue(24);
            sample.createCell(4).setCellValue("PENDING");
            sample.createCell(5).setCellValue("PERSONAL");

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate template", e);
        }
    }

    // ─── BULK UPLOADS ─────────────────────────────────────────────────────────

    @Transactional
    public void bulkUploadLoans(MultipartFile file) {
        if (!isValidExcelFile(file)) {
            throw new BadRequestException("Please upload a valid Excel file (.xlsx)");
        }

        log.info("Processing bulk upload of loans from Excel");
        List<Loan> loans = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheet("Loans");
            if (sheet == null) sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            int rowNumber = 0;
            while (rows.hasNext()) {
                Row currentRow = rows.next();
                if (rowNumber == 0) { rowNumber++; continue; } // Skip header

                try {
                    Long userId = (long) currentRow.getCell(0).getNumericCellValue();
                    BigDecimal principalAmount = BigDecimal.valueOf(currentRow.getCell(1).getNumericCellValue());
                    BigDecimal interestRate = BigDecimal.valueOf(currentRow.getCell(2).getNumericCellValue());
                    int duration = (int) currentRow.getCell(3).getNumericCellValue();
                    String statusStr = getCellStringValue(currentRow.getCell(4), "PENDING");
                    String purposeStr = getCellStringValue(currentRow.getCell(5), "PERSONAL");

                    userRepository.findById(userId).ifPresent(user -> {
                        BigDecimal emi = calculateEMI(principalAmount, interestRate, duration);
                        Loan loan = Loan.builder()
                                .user(user)
                                .principalAmount(principalAmount)
                                .interestRate(interestRate)
                                .durationMonths(duration)
                                .emiAmount(emi)
                                .remainingBalance(principalAmount)
                                .status(LoanStatus.valueOf(statusStr.toUpperCase()))
                                .purpose(purposeStr.toUpperCase())
                                .build();
                        loans.add(loan);
                    });
                } catch (Exception e) {
                    errors.add("Row " + (rowNumber + 1) + ": " + e.getMessage());
                    log.warn("Failed to parse row {}: {}", rowNumber, e.getMessage());
                }
                rowNumber++;
            }

            if (!loans.isEmpty()) {
                loanRepository.saveAll(loans);
                log.info("Successfully bulk uploaded {} loans ({} errors)", loans.size(), errors.size());
            } else {
                throw new BadRequestException("No valid loans found to upload. Errors: " + String.join("; ", errors));
            }

        } catch (IOException e) {
            log.error("Failed to parse Excel file", e);
            throw new BadRequestException("Failed to parse Excel file: " + e.getMessage());
        }
    }

    @Transactional
    public void bulkUploadUsers(MultipartFile file) {
        if (!isValidExcelFile(file)) {
            throw new BadRequestException("Please upload a valid Excel file (.xlsx)");
        }

        log.info("Processing bulk upload of users from Excel");
        List<User> users = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheet("Users");
            if (sheet == null) sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            int rowNumber = 0;
            while (rows.hasNext()) {
                Row currentRow = rows.next();
                if (rowNumber == 0) { rowNumber++; continue; } // Skip header

                try {
                    String name = currentRow.getCell(0).getStringCellValue().trim();
                    String email = currentRow.getCell(1).getStringCellValue().trim().toLowerCase();
                    String password = currentRow.getCell(2).getStringCellValue().trim();
                    String roleStr = getCellStringValue(currentRow.getCell(3), "BORROWER").toUpperCase().trim();
                    String phone = currentRow.getCell(4) != null ? getCellStringValue(currentRow.getCell(4), "") : "";
                    String address = currentRow.getCell(5) != null ? getCellStringValue(currentRow.getCell(5), "") : "";

                    if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                        errors.add("Row " + (rowNumber + 1) + ": Name, Email, and Password are required");
                        rowNumber++;
                        continue;
                    }

                    if (userRepository.existsByEmail(email)) {
                        errors.add("Row " + (rowNumber + 1) + ": Email " + email + " already exists");
                        rowNumber++;
                        continue;
                    }

                    Role role;
                    try {
                        role = Role.valueOf(roleStr);
                    } catch (IllegalArgumentException e) {
                        role = Role.BORROWER;
                    }

                    User user = User.builder()
                            .name(name)
                            .email(email)
                            .password(passwordEncoder.encode(password))
                            .role(role)
                            .phone(phone.isEmpty() ? null : phone)
                            .address(address.isEmpty() ? null : address)
                            .provider("local")
                            .active(true)
                            .emailVerified(false)
                            .build();

                    users.add(user);
                } catch (Exception e) {
                    errors.add("Row " + (rowNumber + 1) + ": " + e.getMessage());
                    log.warn("Failed to parse user row {}: {}", rowNumber, e.getMessage());
                }
                rowNumber++;
            }

            if (!users.isEmpty()) {
                userRepository.saveAll(users);
                log.info("Successfully bulk uploaded {} users ({} skipped/errors)", users.size(), errors.size());
            } else {
                throw new BadRequestException("No valid users found to upload. Errors: " + String.join("; ", errors));
            }

        } catch (IOException e) {
            log.error("Failed to parse Excel file", e);
            throw new BadRequestException("Failed to parse Excel file: " + e.getMessage());
        }
    }

    // ─── HELPERS ──────────────────────────────────────────────────────────────

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private void createHeaderRow(Sheet sheet, CellStyle style, String[] columns) {
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(style);
            sheet.autoSizeColumn(i);
        }
    }

    private String getCellStringValue(Cell cell, String defaultValue) {
        if (cell == null) return defaultValue;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            default -> defaultValue;
        };
    }

    private BigDecimal calculateEMI(BigDecimal principal, BigDecimal annualRate, int months) {
        java.math.BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(1200), 10, java.math.RoundingMode.HALF_UP);
        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(BigDecimal.valueOf(months), 2, java.math.RoundingMode.HALF_UP);
        }
        BigDecimal onePlusRToN = BigDecimal.ONE.add(monthlyRate).pow(months);
        BigDecimal numerator = principal.multiply(monthlyRate).multiply(onePlusRToN);
        BigDecimal denominator = onePlusRToN.subtract(BigDecimal.ONE);
        return numerator.divide(denominator, 2, java.math.RoundingMode.HALF_UP);
    }

    private boolean isValidExcelFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null &&
               (contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") ||
                contentType.equals("application/vnd.ms-excel"));
    }
}