package com.loanmanagement.excel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class ExcelHelper {

    // 🔹 Convert Excel → List of Rows (Generic)
    public List<List<String>> readExcel(InputStream inputStream) {

        List<List<String>> data = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);

            for (Row row : sheet) {

                List<String> rowData = new ArrayList<>();

                for (Cell cell : row) {
                    rowData.add(cell.toString());
                }

                data.add(rowData);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to read Excel file");
        }

        return data;
    }

    // 🔹 Create Excel File from Data
    public ByteArrayInputStream writeExcel(List<List<String>> data) {

        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("Data");

            int rowNum = 0;

            for (List<String> rowData : data) {

                Row row = sheet.createRow(rowNum++);

                int colNum = 0;

                for (String value : rowData) {
                    row.createCell(colNum++).setCellValue(value);
                }
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);

            return new ByteArrayInputStream(out.toByteArray());

        } catch (Exception e) {
            throw new RuntimeException("Failed to write Excel file");
        }
    }
}