package com.attendance.dto;

import com.attendance.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtResponseDto {
    private String token;
    private Long userId;
    private String name;
    private String email;
    private Role role;
    private Long teacherId; // null unless role == TEACHER
    private Long studentId; // null unless role == STUDENT
}
