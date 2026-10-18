package com.attendance.controller;

import com.attendance.dto.RecognitionEventDto;
import com.attendance.dto.UnknownDetectionDto;
import com.attendance.entity.Attendance;
import com.attendance.entity.UnknownDetection;
import com.attendance.repository.UnknownDetectionRepository;
import com.attendance.service.AttendanceService;
import com.attendance.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import com.attendance.dto.StartSessionDto;
import com.attendance.service.ProcessLauncherService;
@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    @Autowired
    private ProcessLauncherService processLauncherService;
    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private UnknownDetectionRepository unknownDetectionRepository;

    @Autowired
    private EmailService emailService;

    @Value("${internal.api.key}")
    private String internalApiKey;

    @Value("${spring.mail.username}")
    private String adminAlertEmail;

    /**
     * Hit by python-face-recognition/api_sender.py every time a face is
     * matched during a scan. Protected by a shared-secret header since this
     * is an internal, service-to-service call.
     *
     * Rejects the event (403) if it falls outside the campus geofence.
     * The Python side should treat a 403 here the same as "not recognized"
     * - don't retry indefinitely.
     */
    @PostMapping("/recognize")
    public ResponseEntity<?> recordRecognition(
            @RequestHeader("X-Internal-Api-Key") String apiKey,
            @RequestBody RecognitionEventDto event) {

        if (!internalApiKey.equals(apiKey)) {
            return ResponseEntity.status(403).body("Invalid internal API key");
        }

        AttendanceService.RecognitionResult result = attendanceService.recordRecognition(event);

        if (result.rejectedByGeofence) {
            return ResponseEntity.status(422).body("Rejected: location outside allowed campus geofence");
        }

        return ResponseEntity.ok(result.attendance);
    }

    /**
     * Hit when recognize.py detects a face that doesn't match any known
     * student. Logs it for security review and fires an alert email to
     * the configured admin address (if alerts are enabled).
     */
    @PostMapping("/unknown-detection")
    public ResponseEntity<UnknownDetection> reportUnknown(
            @RequestHeader("X-Internal-Api-Key") String apiKey,
            @RequestBody UnknownDetectionDto dto) {

        if (!internalApiKey.equals(apiKey)) {
            return ResponseEntity.status(403).build();
        }

        UnknownDetection detection = new UnknownDetection();
        detection.setDetectedAt(LocalDateTime.now());
        detection.setClassroom(dto.getClassroom());
        detection.setSubject(dto.getSubject());
        detection.setLatitude(dto.getLatitude());
        detection.setLongitude(dto.getLongitude());
        detection.setSnapshotPath(dto.getSnapshotPath());
        detection = unknownDetectionRepository.save(detection);

        emailService.sendUnknownPersonAlert(adminAlertEmail, dto.getClassroom(), dto.getSubject());

        return ResponseEntity.ok(detection);
    }

    @GetMapping("/report")
    public ResponseEntity<List<Attendance>> report(
            @RequestParam String subject,
            @RequestParam(required = false) String date) {
        LocalDate targetDate = (date != null) ? LocalDate.parse(date) : LocalDate.now();
        return ResponseEntity.ok(attendanceService.getReportForSubjectAndDate(subject, targetDate));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Attendance>> forStudent(
            @PathVariable Long studentId,
            @RequestParam(required = false) String date) {
        if (date != null) {
            return ResponseEntity.ok(attendanceService.getForStudentAndDate(studentId, LocalDate.parse(date)));
        }
        return ResponseEntity.ok(attendanceService.getAllForStudent(studentId));
    }
    @PostMapping("/start-session")
    public ResponseEntity<?> startSession(@RequestBody StartSessionDto dto) {
        try {
            processLauncherService.startRecognition(dto.getTeacherId(), dto.getSubject());
            return ResponseEntity.ok("Recognition session started");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to start: " + e.getMessage());
        }
    }

    @PostMapping("/stop-session")
    public ResponseEntity<?> stopSession() {
        boolean stopped = processLauncherService.stopRecognition();
        return ResponseEntity.ok(stopped ? "Stopped" : "Nothing was running");
    }
}
