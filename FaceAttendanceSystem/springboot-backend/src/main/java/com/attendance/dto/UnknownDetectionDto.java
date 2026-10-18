package com.attendance.dto;

import lombok.Data;

@Data
public class UnknownDetectionDto {
    private String classroom;
    private String subject;
    private Double latitude;
    private Double longitude;
    private String snapshotPath;
}
