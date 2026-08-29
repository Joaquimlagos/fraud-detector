package com.fraud_detector.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraud_detector.project.dto.request.UserRequestDTO;
import com.fraud_detector.project.dto.response.UserResponseDTO;
import com.fraud_detector.project.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void shouldCreateUserAndReturn201() throws Exception {
        String userId = UUID.randomUUID().toString();
        UserResponseDTO response = new UserResponseDTO(
                userId, "John Doe", "john@example.com", "12345678901",
                "+5511999999999", LocalDate.of(1990, 1, 1), "BR", Instant.now(), Instant.now()
        );

        when(userService.create(any())).thenReturn(response);

        UserRequestDTO request = new UserRequestDTO(
                "John Doe",
                "john@example.com",
                "12345678901",
                "+5511999999999",
                LocalDate.of(1990, 1, 1),
                "BR"
        );

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.fullName").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void shouldReturn400WhenFullNameIsMissing() throws Exception {
        UserRequestDTO request = new UserRequestDTO(
                "",
                "john@example.com",
                "12345678901",
                "+5511999999999",
                LocalDate.of(1990, 1, 1),
                "BR"
        );

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenEmailIsInvalid() throws Exception {
        UserRequestDTO request = new UserRequestDTO(
                "John Doe",
                "invalid-email",
                "12345678901",
                "+5511999999999",
                LocalDate.of(1990, 1, 1),
                "BR"
        );

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenPhoneNumberIsInvalid() throws Exception {
        UserRequestDTO request = new UserRequestDTO(
                "John Doe",
                "john@example.com",
                "12345678901",
                "11999999999",
                LocalDate.of(1990, 1, 1),
                "BR"
        );

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFindUserById() throws Exception {
        String userId = UUID.randomUUID().toString();
        UserResponseDTO response = new UserResponseDTO(
                userId, "John Doe", "john@example.com", "12345678901",
                "+5511999999999", LocalDate.of(1990, 1, 1), "BR", Instant.now(), Instant.now()
        );

        when(userService.findById(userId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.fullName").value("John Doe"));
    }

    @Test
    void shouldReturn404WhenUserNotFound() throws Exception {
        String userId = UUID.randomUUID().toString();
        when(userService.findById(userId)).thenThrow(new com.fraud_detector.project.exception.ResourceNotFoundException("User not found"));

        mockMvc.perform(get("/api/v1/users/" + userId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteUser() throws Exception {
        String userId = UUID.randomUUID().toString();

        mockMvc.perform(delete("/api/v1/users/" + userId))
                .andExpect(status().isNoContent());
    }
}