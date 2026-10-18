package com.attendance.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Single login identity for Admin, Teacher, and Student roles.
 *
 * teacherId / studentId link a login to the corresponding profile record
 * (Teacher has extra fields like name; Student has roll_no, face_encoding,
 * etc). ADMIN users have both null - they don't need a profile beyond
 * the login itself.
 */
@Entity
@Table(name = "app_user")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String email;

    // BCrypt hash, never plaintext.
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private String name;

    // Set when role == TEACHER
    private Long teacherId;

    // Set when role == STUDENT
    private Long studentId;
}
