package com.attendance.controller;

import com.attendance.dto.JwtResponseDto;
import com.attendance.dto.LoginRequestDto;
import com.attendance.dto.RegisterUserDto;
import com.attendance.entity.User;
import com.attendance.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto request) {
        return authService.login(request.getEmail(), request.getPassword())
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(401).body("Invalid email or password"));
    }

    /** Admin-only: create a new Teacher or Student login. See SecurityConfig. */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterUserDto dto) {
        try {
            User user = authService.register(dto);
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
