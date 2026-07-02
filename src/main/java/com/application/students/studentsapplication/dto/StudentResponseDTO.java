package com.application.students.studentsapplication.dto;

import lombok.Builder;

@Builder
public record StudentResponseDTO(
        Long id,
        String firstName,
        String lastName,
        String age) {

}