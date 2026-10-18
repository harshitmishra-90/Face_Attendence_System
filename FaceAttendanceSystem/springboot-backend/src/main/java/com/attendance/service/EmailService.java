package com.attendance.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Value("${attendance.alerts-enabled}")
    private boolean alertsEnabled;

    /**
     * Sends a plain-text email, or silently no-ops (logging instead) when
     * attendance.alerts-enabled=false — which it is by default, since this
     * needs real SMTP credentials in application.properties to actually
     * send anything. Flip it on once you've configured a real mail account.
     */
    public void send(String to, String subject, String body) {
        if (!alertsEnabled) {
            System.out.println("[EmailService] Alerts disabled - would have sent to " + to + ": " + subject);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception e) {
            // Don't let a mail server hiccup break the attendance flow.
            System.err.println("[EmailService] Failed to send email to " + to + ": " + e.getMessage());
        }
    }

    public void sendLowAttendanceWarning(String studentEmail, String studentName, String subject, double percentage) {
        String body = String.format(
                "Hi %s,%n%nYour attendance in %s is currently %.1f%%, which is below the required threshold.%n" +
                "Please reach out to your teacher if you believe this is a mistake.%n%nRegards,%nAttendance System",
                studentName, subject, percentage);
        send(studentEmail, "Low Attendance Warning - " + subject, body);
    }

    public void sendAttendanceConfirmation(String studentEmail, String studentName, String subject) {
        String body = String.format(
                "Hi %s,%n%nYour attendance for %s has been recorded successfully.%n%nRegards,%nAttendance System",
                studentName, subject);
        send(studentEmail, "Attendance Confirmed - " + subject, body);
    }

    public void sendUnknownPersonAlert(String adminEmail, String classroom, String subject) {
        String body = String.format(
                "An unrecognized face was detected in %s during %s. Please review campus security footage.",
                classroom, subject);
        send(adminEmail, "Unknown Person Alert", body);
    }
}
