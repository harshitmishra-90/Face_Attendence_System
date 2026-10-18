package com.attendance.controller;

import com.attendance.entity.Student;
import com.attendance.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @Value("${internal.api.key}")
    private String internalApiKey;

    @PostMapping
    public ResponseEntity<Student> register(@RequestBody Student student) {
        return ResponseEntity.ok(studentService.register(student));
    }

    @GetMapping
    public ResponseEntity<List<Student>> findAll() {
        return ResponseEntity.ok(studentService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Student> findById(@PathVariable Long id) {
        return studentService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Called by python-face-recognition/train.py after generating an
     * encoding. The raw encoding is encrypted before being stored - see
     * StudentService.updateFaceEncoding / EncryptionService.
     */
    @PutMapping("/{id}/face-encoding")
    public ResponseEntity<?> updateFaceEncoding(
            @PathVariable Long id,
            @RequestHeader("X-Internal-Api-Key") String apiKey,
            @RequestBody String encoding) {

        if (!internalApiKey.equals(apiKey)) {
            return ResponseEntity.status(403).body("Invalid internal API key");
        }
        return studentService.updateFaceEncoding(id, encoding)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
