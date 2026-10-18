package com.attendance.repository;

import com.attendance.entity.UnknownDetection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UnknownDetectionRepository extends JpaRepository<UnknownDetection, Long> {
    List<UnknownDetection> findTop50ByOrderByDetectedAtDesc();
}
