package com.attendance.repository;

import com.attendance.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findByStudentIdAndDate(Long studentId, LocalDate date);

    List<Attendance> findBySubjectAndDate(String subject, LocalDate date);

    List<Attendance> findByStudentId(Long studentId);

    // Distinct class-days a student was marked present in, for a subject -
    // used for percentage calculation (one scan != one class day).
    @Query("SELECT COUNT(DISTINCT a.date) FROM Attendance a " +
           "WHERE a.studentId = :studentId AND a.subject = :subject AND a.status = 'P'")
    long countPresentDays(@Param("studentId") Long studentId, @Param("subject") String subject);

    @Query("SELECT COUNT(DISTINCT a.date) FROM Attendance a WHERE a.subject = :subject")
    long countTotalClassDays(@Param("subject") String subject);

    @Query("SELECT DISTINCT a.subject FROM Attendance a")
    List<String> findDistinctSubjects();
}
