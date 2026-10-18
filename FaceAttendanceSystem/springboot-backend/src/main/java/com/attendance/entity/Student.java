package com.attendance.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "student")
@Data
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long studentId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String rollNo;

    private String branch;

    private String year;

    // AES-encrypted, base64-encoded face encoding. Decrypted only in
    // EncryptionService when actually needed (e.g. for debugging) - the
    // recognition matching itself is done Python-side against the local
    // encodings.pkl file, so this column is a secure backup copy, not
    // used for live matching.
    @Lob
    @Column(columnDefinition = "TEXT")
    private String faceEncoding;
}
