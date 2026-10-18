package com.attendance.service;

import com.attendance.dto.AttendanceStatsDto;
import com.attendance.entity.Student;
import com.attendance.repository.AttendanceRepository;
import com.attendance.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Value("${attendance.low-attendance-threshold}")
    private double defaulterThreshold;

    public AttendanceStatsDto getStatsForStudent(Long studentId, String subject) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        long present = attendanceRepository.countPresentDays(studentId, subject);
        long total = attendanceRepository.countTotalClassDays(subject);
        double percentage = total == 0 ? 0.0 : (present * 100.0 / total);

        return new AttendanceStatsDto(studentId, student.getName(), student.getRollNo(),
                subject, present, total, round1(percentage), percentage < defaulterThreshold);
    }

    /** Stats for every student, for a given subject - powers the analytics dashboard. */
    public List<AttendanceStatsDto> getStatsForAllStudents(String subject) {
        return studentRepository.findAll().stream()
                .map(s -> getStatsForStudent(s.getStudentId(), subject))
                .collect(Collectors.toList());
    }

    public List<AttendanceStatsDto> getDefaulters(String subject) {
        return getStatsForAllStudents(subject).stream()
                .filter(AttendanceStatsDto::isDefaulter)
                .collect(Collectors.toList());
    }

    public List<String> getAllSubjects() {
        return attendanceRepository.findDistinctSubjects();
    }

    private double round1(double value) {
        return Math.round(value * 10) / 10.0;
    }
}
