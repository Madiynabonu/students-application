package com.application.students.studentsapplication.exception;

public class StudentNotFoundException extends RuntimeException {
    public StudentNotFoundException() {
        super("Student not found");
    }
}
