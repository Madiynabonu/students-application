package com.application.students.studentsapplication.service;

import com.application.students.studentsapplication.dto.StudentRequestDTO;
import com.application.students.studentsapplication.dto.StudentResponseDTO;
import com.application.students.studentsapplication.entity.Student;
import com.application.students.studentsapplication.exception.StudentNotFoundException;
import com.application.students.studentsapplication.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository repository;

    @InjectMocks
    private StudentService service;

    @Test
    void findAll_returnsAllStudentsAsResponseDto() {
        Student student = Student.builder().id(1L).firstName("Ali").lastName("Valiyev").age("20").build();
        when(repository.findAll()).thenReturn(List.of(student));

        List<StudentResponseDTO> result = service.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(0).firstName()).isEqualTo("Ali");
    }

    @Test
    void findAll_returnsEmptyListWhenNoStudents() {
        when(repository.findAll()).thenReturn(List.of());

        assertThat(service.findAll()).isEmpty();
    }

    @Test
    void findById_returnsStudentWhenFound() {
        Student student = Student.builder().id(1L).firstName("Ali").lastName("Valiyev").age("20").build();
        when(repository.findById(1L)).thenReturn(Optional.of(student));

        StudentResponseDTO result = service.findById(1L);

        assertThat(result.firstName()).isEqualTo("Ali");
        assertThat(result.age()).isEqualTo("20");
    }

    @Test
    void findById_throwsWhenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(StudentNotFoundException.class);
    }

    @Test
    void save_persistsNewStudentAndReturnsResponseDto() {
        StudentRequestDTO request = new StudentRequestDTO("Ali", "Valiyev", "20");
        Student saved = Student.builder().id(1L).firstName("Ali").lastName("Valiyev").age("20").build();
        when(repository.save(any(Student.class))).thenReturn(saved);

        StudentResponseDTO result = service.save(request);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.firstName()).isEqualTo("Ali");
        assertThat(result.lastName()).isEqualTo("Valiyev");
        assertThat(result.age()).isEqualTo("20");
    }

    @Test
    void update_updatesExistingStudentFields() {
        Student existing = Student.builder().id(1L).firstName("Ali").lastName("Valiyev").age("20").build();
        StudentRequestDTO request = new StudentRequestDTO("Sardor", "Valiyev", "22");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Student.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StudentResponseDTO result = service.update(1L, request);

        assertThat(result.firstName()).isEqualTo("Sardor");
        assertThat(result.age()).isEqualTo("22");
    }

    @Test
    void update_throwsWhenStudentNotFound() {
        StudentRequestDTO request = new StudentRequestDTO("Sardor", "Valiyev", "22");
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, request))
                .isInstanceOf(StudentNotFoundException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void delete_removesExistingStudent() {
        when(repository.existsById(1L)).thenReturn(true);

        service.delete(1L);

        verify(repository, times(1)).deleteById(1L);
    }

    @Test
    void delete_throwsWhenStudentNotFound() {
        when(repository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(StudentNotFoundException.class);

        verify(repository, never()).deleteById(any());
    }
}