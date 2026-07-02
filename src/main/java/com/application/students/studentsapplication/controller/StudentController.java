package com.application.students.studentsapplication.controller;

import com.application.students.studentsapplication.dto.StudentRequestDTO;
import com.application.students.studentsapplication.dto.StudentResponseDTO;
import com.application.students.studentsapplication.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
public class StudentController {


    private final StudentService service;


    @GetMapping
    public List<StudentResponseDTO> findAll() {
        return service.findAll();
    }

    @GetMapping("/id")
    public StudentResponseDTO findById(@RequestParam Long id) {
        return service.findById(id);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StudentResponseDTO create(@RequestBody StudentRequestDTO student) {

        return service.save(student);
    }


    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public StudentResponseDTO update(@PathVariable Long id, @Valid @RequestBody StudentRequestDTO student) {
        return service.update(id, student);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@RequestParam Long id) {
        service.delete(id);
    }
}
