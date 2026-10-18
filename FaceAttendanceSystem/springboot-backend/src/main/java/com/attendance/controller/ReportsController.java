package com.attendance.controller;

import com.attendance.entity.Attendance;
import com.attendance.service.AttendanceService;
import com.attendance.service.ExcelExportService;
import com.attendance.service.PdfExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportsController {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private PdfExportService pdfExportService;

    @Autowired
    private ExcelExportService excelExportService;

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> exportPdf(
            @RequestParam String subject,
            @RequestParam(required = false) String date) throws Exception {

        String targetDate = (date != null) ? date : LocalDate.now().toString();
        List<Attendance> records = attendanceService.getReportForSubjectAndDate(subject, LocalDate.parse(targetDate));
        byte[] pdf = pdfExportService.generateAttendanceReportPdf(subject, targetDate, records);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=attendance_" + subject + "_" + targetDate + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/excel")
    public ResponseEntity<byte[]> exportExcel(
            @RequestParam String subject,
            @RequestParam(required = false) String date) throws Exception {

        String targetDate = (date != null) ? date : LocalDate.now().toString();
        List<Attendance> records = attendanceService.getReportForSubjectAndDate(subject, LocalDate.parse(targetDate));
        byte[] excel = excelExportService.generateAttendanceReportExcel(subject, records);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=attendance_" + subject + "_" + targetDate + ".xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }
}
