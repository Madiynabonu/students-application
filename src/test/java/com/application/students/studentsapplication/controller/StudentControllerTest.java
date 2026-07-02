package com.application.students.studentsapplication.controller;

import com.application.students.studentsapplication.config.SecurityConfig;
import com.application.students.studentsapplication.dto.StudentResponseDTO;
import com.application.students.studentsapplication.exception.StudentNotFoundException;
import com.application.students.studentsapplication.service.StudentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StudentController.class)
@Import(SecurityConfig.class)
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StudentService service;

    private static final String VALID_BODY = """
            {"firstName":"Ali","lastName":"Valiyev","age":"20"}
            """;

    @Test
    void findAll_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/students"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void findAll_withAnyAuthenticatedUser_returnsOk() throws Exception {
        when(service.findAll()).thenReturn(List.of(new StudentResponseDTO(1L, "Ali", "Valiyev", "20")));

        mockMvc.perform(get("/students").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("Ali"));
    }

    @Test
    void findById_whenFound_returnsOk() throws Exception {
        when(service.findById(1L)).thenReturn(new StudentResponseDTO(1L, "Ali", "Valiyev", "20"));

        mockMvc.perform(get("/students/id").param("id", "1").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void findById_whenNotFound_returnsNotFound() throws Exception {
        when(service.findById(99L)).thenThrow(new StudentNotFoundException());

        mockMvc.perform(get("/students/id").param("id", "99").with(jwt()))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(post("/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void create_withUserRole_returnsForbidden() throws Exception {
        mockMvc.perform(post("/students")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    void create_withAdminRole_returnsCreated() throws Exception {
        when(service.save(any())).thenReturn(new StudentResponseDTO(1L, "Ali", "Valiyev", "20"));

        mockMvc.perform(post("/students")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void create_withBlankFirstName_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/students")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"","lastName":"Valiyev","age":"20"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_withAdminRole_returnsOk() throws Exception {
        when(service.update(eq(1L), any())).thenReturn(new StudentResponseDTO(1L, "Sardor", "Valiyev", "22"));

        mockMvc.perform(put("/students/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Sardor","lastName":"Valiyev","age":"22"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Sardor"));
    }

    @Test
    void update_withUserRole_returnsForbidden() throws Exception {
        mockMvc.perform(put("/students/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    void delete_withAdminRole_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/students").param("id", "1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_withUserRole_returnsForbidden() throws Exception {
        mockMvc.perform(delete("/students").param("id", "1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void delete_whenStudentNotFound_returnsNotFound() throws Exception {
        org.mockito.Mockito.doThrow(new StudentNotFoundException()).when(service).delete(99L);

        mockMvc.perform(delete("/students").param("id", "99")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNotFound());
    }
}