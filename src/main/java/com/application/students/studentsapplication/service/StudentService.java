package com.application.students.studentsapplication.service;

import com.application.students.studentsapplication.dto.StudentRequestDTO;
import com.application.students.studentsapplication.dto.StudentResponseDTO;
import com.application.students.studentsapplication.entity.Student;
import com.application.students.studentsapplication.exception.StudentNotFoundException;
import com.application.students.studentsapplication.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentService {


    private final StudentRepository repository;


    public List<StudentResponseDTO> findAll() {

        return repository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }


    public StudentResponseDTO findById(Long id) {

        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(StudentNotFoundException::new);
    }


    public StudentResponseDTO save(StudentRequestDTO student) {
        return toResponse(
                repository.save(Student.builder()
                        .firstName(student.firstName())
                        .lastName(student.lastName())
                        .age(student.age())
                        .build()));
    }


    public StudentResponseDTO update(Long id, StudentRequestDTO studentReq) {

        Student student = repository.findById(id).orElseThrow(StudentNotFoundException::new);
        student.setFirstName(studentReq.firstName());
        student.setLastName(studentReq.lastName());
        student.setAge(studentReq.age());

        return toResponse(repository.save(student));
    }


    public void delete(Long id) {


        if (!repository.existsById(id)) {
            throw new StudentNotFoundException();
        }
        repository.deleteById(id);
    }

    private StudentResponseDTO toResponse(Student student) {


        return StudentResponseDTO.builder()
                .id(student.getId())
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .age(student.getAge())
                .build();
    }
}
