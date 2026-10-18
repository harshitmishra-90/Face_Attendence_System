package com.attendance.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "attendance")
@Data
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long attendanceId;

    @Column(nullable = false)
    private Long studentId;

    @Column(nullable = false)
    private Long teacherId;

    @Column(nullable = false)
    private String subject;

    // Which physical classroom/camera this scan came from - supports
    // multiple simultaneous classrooms.
    private String classroom;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private LocalTime time;

    // "P" or "A"
    @Column(nullable = false, length = 1)
    private String status;

    private Integer scanNumber;

    // Geofencing - where the scan was reported from.
    private Double latitude;
    private Double longitude;

    // Whether the Python side's basic liveness check (blink detection)
    // passed for this specific scan. See recognize.py for how this is
    // computed - it's a basic anti-spoofing measure, not a guarantee.
    private Boolean livenessVerified;
}
