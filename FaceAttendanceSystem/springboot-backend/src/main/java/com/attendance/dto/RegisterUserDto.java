package com.attendance.dto;

import com.attendance.entity.Role;
import lombok.Data;

/**
 * Used by an Admin to create a new login for a teacher or student.
 * For TEACHER: also creates a Teacher profile row.
 * For STUDENT: linkedStudentId must reference an existing Student record
 * (created separately via /api/students).
 */
@Data
public class RegisterUserDto {
    private String name;
    private String email;
    private String password;
    private Role role;
    private String department;      // used when role == TEACHER
    private Long linkedStudentId;   // used when role == STUDENT
}
