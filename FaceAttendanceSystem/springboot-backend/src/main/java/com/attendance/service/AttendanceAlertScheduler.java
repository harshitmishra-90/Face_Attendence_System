package com.attendance.service;

import com.attendance.dto.AttendanceStatsDto;
import com.attendance.entity.User;
import com.attendance.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Runs once a day and emails any student who has dropped below the
 * configured attendance threshold, across every subject with recorded
 * attendance. Disabled by default via attendance.alerts-enabled=false in
 * application.properties until real SMTP credentials are configured.
 */
@Component
public class AttendanceAlertScheduler {

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Value("${attendance.alerts-enabled}")
    private boolean alertsEnabled;

    // Runs daily at 6 PM. Change the cron expression to suit your class schedule.
    @Scheduled(cron = "0 0 18 * * *")
    public void sendDailyLowAttendanceWarnings() {
        if (!alertsEnabled) return;

        for (String subject : analyticsService.getAllSubjects()) {
            for (AttendanceStatsDto stats : analyticsService.getDefaulters(subject)) {
                Optional<User> studentUser = userRepository.findByStudentId(stats.getStudentId());
                studentUser.ifPresent(user -> emailService.sendLowAttendanceWarning(
                        user.getEmail(), stats.getStudentName(), subject, stats.getPercentage()));
            }
        }
    }
}
