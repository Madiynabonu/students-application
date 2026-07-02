package com.application.students.studentsapplication.dto;

import jakarta.validation.constraints.NotBlank;

public record StudentRequestDTO(
        @NotBlank(message = "Firstname cannot be null") String firstName,
        @NotBlank(message = "Lastname cannot be null") String lastName,
        @NotBlank(message = "Age cannot be null") String age
) {
}
