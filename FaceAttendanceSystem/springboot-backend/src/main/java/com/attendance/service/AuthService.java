package com.attendance.service;

import com.attendance.dto.JwtResponseDto;
import com.attendance.dto.RegisterUserDto;
import com.attendance.entity.Role;
import com.attendance.entity.Teacher;
import com.attendance.entity.User;
import com.attendance.repository.TeacherRepository;
import com.attendance.repository.UserRepository;
import com.attendance.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public Optional<JwtResponseDto> login(String email, String rawPassword) {
        return userRepository.findByEmail(email)
                .filter(u -> passwordEncoder.matches(rawPassword, u.getPassword()))
                .map(u -> {
                    String token = jwtUtil.generateToken(u.getEmail(), u.getRole().name());
                    return new JwtResponseDto(token, u.getUserId(), u.getName(), u.getEmail(),
                            u.getRole(), u.getTeacherId(), u.getStudentId());
                });
    }

    public User register(RegisterUserDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("A user with this email already exists");
        }

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(dto.getRole());

        if (dto.getRole() == Role.TEACHER) {
            Teacher teacher = new Teacher();
            teacher.setName(dto.getName());
            teacher.setEmail(dto.getEmail());
            teacher.setDepartment(dto.getDepartment());
            teacher = teacherRepository.save(teacher);
            user.setTeacherId(teacher.getTeacherId());
        } else if (dto.getRole() == Role.STUDENT) {
            if (dto.getLinkedStudentId() == null) {
                throw new IllegalArgumentException("linkedStudentId is required for STUDENT role - register the student profile via /api/students first");
            }
            user.setStudentId(dto.getLinkedStudentId());
        }

        return userRepository.save(user);
    }
}
