package com.attendance.service;

import com.attendance.dto.RecognitionEventDto;
import com.attendance.entity.Attendance;
import com.attendance.entity.Student;
import com.attendance.entity.User;
import com.attendance.repository.AttendanceRepository;
import com.attendance.repository.StudentRepository;
import com.attendance.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GeofenceService geofenceService;

    @Autowired
    private EmailService emailService;

    public static class RecognitionResult {
        public final Attendance attendance;
        public final boolean rejectedByGeofence;

        public RecognitionResult(Attendance attendance, boolean rejectedByGeofence) {
            this.attendance = attendance;
            this.rejectedByGeofence = rejectedByGeofence;
        }
    }

    /**
     * Called from AttendanceController when Python reports a recognized face.
     * Rejects the scan if it falls outside the campus geofence (when
     * geofencing is enabled). Sends a confirmation email if the linked
     * student login has an email and alerts are enabled.
     */
    public RecognitionResult recordRecognition(RecognitionEventDto event) {
        if (!geofenceService.isWithinCampus(event.getLatitude(), event.getLongitude())) {
            return new RecognitionResult(null, true);
        }

        Attendance attendance = new Attendance();
        attendance.setStudentId(event.getStudentId());
        attendance.setTeacherId(event.getTeacherId());
        attendance.setSubject(event.getSubject());
        attendance.setClassroom(event.getClassroom());
        attendance.setDate(LocalDate.now());
        attendance.setTime(LocalTime.now());
        attendance.setStatus("P");
        attendance.setScanNumber(event.getScanNumber());
        attendance.setLatitude(event.getLatitude());
        attendance.setLongitude(event.getLongitude());
        attendance.setLivenessVerified(event.getLivenessVerified());
        attendance = attendanceRepository.save(attendance);

        notifyStudent(event.getStudentId(), event.getSubject());

        return new RecognitionResult(attendance, false);
    }

    private void notifyStudent(Long studentId, String subject) {
        Optional<Student> studentOpt = studentRepository.findById(studentId);
        Optional<User> studentUser = userRepository.findByStudentId(studentId);

        if (studentOpt.isEmpty() || studentUser.isEmpty()) return; // no login exists for this student yet

        emailService.sendAttendanceConfirmation(
                studentUser.get().getEmail(),
                studentOpt.get().getName(),
                subject
        );
    }

    public List<Attendance> getReportForSubjectAndDate(String subject, LocalDate date) {
        return attendanceRepository.findBySubjectAndDate(subject, date);
    }

    public List<Attendance> getForStudentAndDate(Long studentId, LocalDate date) {
        return attendanceRepository.findByStudentIdAndDate(studentId, date);
    }

    public List<Attendance> getAllForStudent(Long studentId) {
        return attendanceRepository.findByStudentId(studentId);
    }
}
