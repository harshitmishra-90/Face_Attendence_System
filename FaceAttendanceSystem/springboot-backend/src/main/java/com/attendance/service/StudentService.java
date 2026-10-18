package com.attendance.service;

import com.attendance.entity.Student;
import com.attendance.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private EncryptionService encryptionService;

    public Student register(Student student) {
        return studentRepository.save(student);
    }

    public List<Student> findAll() {
        return studentRepository.findAll();
    }

    public Optional<Student> findById(Long id) {
        return studentRepository.findById(id);
    }

    public Optional<Student> findByRollNo(String rollNo) {
        return studentRepository.findByRollNo(rollNo);
    }

    /** Encrypts the raw encoding (a JSON array string from train.py) before storing. */
    public Optional<Student> updateFaceEncoding(Long id, String rawEncodingJson) {
        return studentRepository.findById(id).map(student -> {
            student.setFaceEncoding(encryptionService.encrypt(rawEncodingJson));
            return studentRepository.save(student);
        });
    }
}
