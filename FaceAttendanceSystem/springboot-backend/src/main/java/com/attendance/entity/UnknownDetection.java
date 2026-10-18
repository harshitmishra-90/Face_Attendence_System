package com.attendance.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * A face was detected during a scan but didn't match any known student
 * within the recognition tolerance. Logged for campus-security review;
 * see AttendanceController for where alert emails get triggered.
 */
@Entity
@Table(name = "unknown_detection")
@Data
public class UnknownDetection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime detectedAt;

    private String classroom;
    private String subject;
    private Double latitude;
    private Double longitude;

    // Path to the saved snapshot image, if recognize.py was configured to
    // save one. Nullable - snapshot saving is optional.
    private String snapshotPath;
}
