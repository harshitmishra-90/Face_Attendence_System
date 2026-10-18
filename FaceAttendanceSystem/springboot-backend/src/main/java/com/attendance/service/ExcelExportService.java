package com.attendance.service;

import com.attendance.entity.Attendance;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class ExcelExportService {

    public byte[] generateAttendanceReportExcel(String subject, List<Attendance> records) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(subject.length() > 25 ? subject.substring(0, 25) : subject);

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);
            String[] headers = {"Student ID", "Subject", "Date", "Time", "Scan #", "Status"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (Attendance a : records) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(a.getStudentId());
                row.createCell(1).setCellValue(a.getSubject());
                row.createCell(2).setCellValue(String.valueOf(a.getDate()));
                row.createCell(3).setCellValue(String.valueOf(a.getTime()));
                row.createCell(4).setCellValue(a.getScanNumber() != null ? a.getScanNumber() : 0);
                row.createCell(5).setCellValue("P".equals(a.getStatus()) ? "Present" : "Absent");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }
}
