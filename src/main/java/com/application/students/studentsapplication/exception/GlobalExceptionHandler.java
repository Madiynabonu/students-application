package com.application.students.studentsapplication.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Optional;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler
    public ResponseEntity<String> handleNotFound(StudentNotFoundException e) {
        return ResponseEntity.status(404).body(e.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<Optional<FieldError>> handleNotValidIdException(MethodArgumentNotValidException e) {
        return ResponseEntity.badRequest().body(e.getBindingResult().getFieldErrors().stream().findFirst());
    }
}
