package com.attendance.controller;

import com.attendance.entity.UnknownDetection;
import com.attendance.repository.UnknownDetectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Read-only view for Admin/Teacher to review recent unrecognized-face events. */
@RestController
@RequestMapping("/api/unknown-detections")
public class UnknownDetectionController {

    @Autowired
    private UnknownDetectionRepository unknownDetectionRepository;

    @GetMapping
    public ResponseEntity<List<UnknownDetection>> recent() {
        return ResponseEntity.ok(unknownDetectionRepository.findTop50ByOrderByDetectedAtDesc());
    }
}
