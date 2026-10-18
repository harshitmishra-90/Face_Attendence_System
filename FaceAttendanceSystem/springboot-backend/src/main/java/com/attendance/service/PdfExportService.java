package com.attendance.service;

import com.attendance.entity.Attendance;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class PdfExportService {

    public byte[] generateAttendanceReportPdf(String subject, String date, List<Attendance> records) throws IOException, DocumentException {
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

        document.add(new Paragraph("Attendance Report", titleFont));
        document.add(new Paragraph("Subject: " + subject, cellFont));
        document.add(new Paragraph("Date: " + date, cellFont));
        document.add(Chunk.NEWLINE);

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        for (String header : new String[]{"Student ID", "Time", "Scan #", "Status"}) {
            table.addCell(new Phrase(header, headerFont));
        }

        for (Attendance a : records) {
            table.addCell(new Phrase(String.valueOf(a.getStudentId()), cellFont));
            table.addCell(new Phrase(String.valueOf(a.getTime()), cellFont));
            table.addCell(new Phrase(a.getScanNumber() != null ? String.valueOf(a.getScanNumber()) : "-", cellFont));
            table.addCell(new Phrase("P".equals(a.getStatus()) ? "Present" : "Absent", cellFont));
        }

        document.add(table);
        document.close();
        return out.toByteArray();
    }
}
