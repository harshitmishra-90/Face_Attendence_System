package com.attendance.controller;

import com.attendance.dto.AttendanceStatsDto;
import com.attendance.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/student/{studentId}")
    public ResponseEntity<AttendanceStatsDto> studentStats(
            @PathVariable Long studentId, @RequestParam String subject) {
        return ResponseEntity.ok(analyticsService.getStatsForStudent(studentId, subject));
    }

    @GetMapping("/subject")
    public ResponseEntity<List<AttendanceStatsDto>> subjectStats(
            @RequestParam String subject) {

        return ResponseEntity.ok(
                analyticsService.getStatsForAllStudents(subject)
        );
    }

    @GetMapping("/defaulters")
    public ResponseEntity<List<AttendanceStatsDto>> defaulters(@RequestParam String subject) {
        return ResponseEntity.ok(analyticsService.getDefaulters(subject));
    }

    @GetMapping("/subjects")
    public ResponseEntity<List<String>> subjects() {
        return ResponseEntity.ok(analyticsService.getAllSubjects());
    }
}
