package com.attendance.dto;

import lombok.Data;

/**
 * Payload sent by python-face-recognition/api_sender.py whenever a face is
 * matched to a student during a scan.
 *
 * This is the API contract between the two halves of the system —
 * change it here AND in api_sender.py together.
 */
@Data
public class RecognitionEventDto {
    private Long studentId;
    private Long teacherId;
    private String subject;
    private String classroom;
    private Integer scanNumber;
    private Double latitude;
    private Double longitude;
    private Boolean livenessVerified;
}
