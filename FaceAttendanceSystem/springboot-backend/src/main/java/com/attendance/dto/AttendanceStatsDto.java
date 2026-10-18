package com.attendance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AttendanceStatsDto {
    private Long studentId;
    private String studentName;
    private String rollNo;
    private String subject;
    private long presentDays;
    private long totalDays;
    private double percentage;
    private boolean isDefaulter; // below threshold
}
