package com.attendance.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Profile data for a teacher. Login credentials live in User, linked via
 * User.teacherId -> Teacher.teacherId.
 */
@Entity
@Table(name = "teacher")
@Data
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long teacherId;

    @Column(nullable = false)
    private String name;


    @Column(nullable = false, unique = true)
    private String email;

    private String department;
}
