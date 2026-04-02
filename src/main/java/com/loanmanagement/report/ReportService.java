package com.loanmanagement.report;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.loanmanagement.loan.Loan;
import com.loanmanagement.loan.LoanRepository;
import com.opencsv.CSVWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final LoanRepository loanRepository;

    public ByteArrayInputStream generateFinancialSummaryPdf() {
        log.info("Generating Financial Summary PDF");
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("Loan Management System - Financial Summary").setBold().setFontSize(18));
            document.add(new Paragraph("Generated at: " + java.time.LocalDateTime.now()));
            document.add(new Paragraph("\n"));

            List<Loan> loans = loanRepository.findAll();

            float[] columnWidths = {50F, 100F, 100F, 100F, 100F, 100F};
            Table table = new Table(columnWidths);
            
            table.addHeaderCell("ID");
            table.addHeaderCell("User");
            table.addHeaderCell("Principal");
            table.addHeaderCell("Remaining");
            table.addHeaderCell("Status");
            table.addHeaderCell("Start Date");

            for (Loan loan : loans) {
                table.addCell(loan.getId().toString());
                table.addCell(loan.getUser().getName());
                table.addCell(loan.getPrincipalAmount().toString());
                table.addCell(loan.getRemainingBalance() != null ? loan.getRemainingBalance().toString() : "0");
                table.addCell(loan.getStatus().name());
                table.addCell(loan.getStartDate() != null ? loan.getStartDate().toString() : "N/A");
            }

            document.add(table);
            document.close();
            
        } catch (Exception e) {
            log.error("Error generating PDF", e);
            throw new RuntimeException("Failed to generate PDF report", e);
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    public ByteArrayInputStream generateFinancialSummaryCsv() {
        log.info("Generating Financial Summary CSV");
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(out))) {
            String[] header = {"Loan ID", "Borrower Name", "Principal Amount", "Remaining Balance", "Status", "Start Date"};
            writer.writeNext(header);

            List<Loan> loans = loanRepository.findAll();
            for (Loan loan : loans) {
                String[] data = {
                        loan.getId().toString(),
                        loan.getUser().getName(),
                        loan.getPrincipalAmount().toString(),
                        loan.getRemainingBalance() != null ? loan.getRemainingBalance().toString() : "0",
                        loan.getStatus().name(),
                        loan.getStartDate() != null ? loan.getStartDate().toString() : "N/A"
                };
                writer.writeNext(data);
            }
        } catch (Exception e) {
            log.error("Error generating CSV", e);
            throw new RuntimeException("Failed to generate CSV report", e);
        }

        return new ByteArrayInputStream(out.toByteArray());
    }
}
