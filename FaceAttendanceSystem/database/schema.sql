CREATE DATABASE IF NOT EXISTS face_attendance;
USE face_attendance;

CREATE TABLE IF NOT EXISTS teacher (
    teacher_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    department VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS student (
    student_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    roll_no VARCHAR(50) NOT NULL UNIQUE,
    branch VARCHAR(100),
    year VARCHAR(20),
    face_encoding TEXT
);

-- Single login table for Admin, Teacher, and Student roles.
-- teacher_id / student_id link back to the profile tables above.
CREATE TABLE IF NOT EXISTS app_user (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    name VARCHAR(255),
    teacher_id BIGINT,
    student_id BIGINT,
    CONSTRAINT fk_user_teacher FOREIGN KEY (teacher_id) REFERENCES teacher(teacher_id),
    CONSTRAINT fk_user_student FOREIGN KEY (student_id) REFERENCES student(student_id)
);

CREATE TABLE IF NOT EXISTS attendance (
    attendance_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    subject VARCHAR(255) NOT NULL,
    classroom VARCHAR(100),
    date DATE NOT NULL,
    time TIME NOT NULL,
    status CHAR(1) NOT NULL,
    scan_number INT,
    latitude DOUBLE,
    longitude DOUBLE,
    liveness_verified BOOLEAN,
    CONSTRAINT fk_attendance_student FOREIGN KEY (student_id) REFERENCES student(student_id),
    CONSTRAINT fk_attendance_teacher FOREIGN KEY (teacher_id) REFERENCES teacher(teacher_id),
    INDEX idx_attendance_subject_date (subject, date),
    INDEX idx_attendance_student_date (student_id, date)
);

CREATE TABLE IF NOT EXISTS unknown_detection (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    detected_at DATETIME NOT NULL,
    classroom VARCHAR(100),
    subject VARCHAR(255),
    latitude DOUBLE,
    longitude DOUBLE,
    snapshot_path VARCHAR(500)
);
