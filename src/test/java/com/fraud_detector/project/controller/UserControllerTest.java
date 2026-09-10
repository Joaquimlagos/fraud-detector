package com.fraud_detector.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraud_detector.project.dto.request.UserRequestDTO;
import com.fraud_detector.project.dto.response.TransactionItemDTO;
import com.fraud_detector.project.dto.response.UserHistoryResponseDTO;
import com.fraud_detector.project.dto.response.UserResponseDTO;
import com.fraud_detector.project.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
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
    void shouldFindUserHistory() throws Exception {
        String userId = UUID.randomUUID().toString();
        TransactionItemDTO transaction = new TransactionItemDTO(
                UUID.randomUUID().toString(), userId, new BigDecimal("100.00"), "USD", "Acme Store",
                "ECOM", "CREDIT_CARD", "1234", "ONLINE", "192.168.0.1", "device-1",
                40.7128, -74.0060, "BR", "APPROVED", List.of("low amount anomaly"),
                Instant.parse("2024-01-01T10:01:00Z"), Instant.parse("2024-01-01T10:00:00Z"),
                Instant.parse("2024-01-01T10:05:00Z")
        );
        UserHistoryResponseDTO response = new UserHistoryResponseDTO(
                userId, "John Doe", List.of(transaction)
        );

        when(userService.findHistory(userId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/users/history/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.transactions[0].transactionId").value(transaction.transactionId()))
                .andExpect(jsonPath("$.transactions[0].amount").value(100.00))
                .andExpect(jsonPath("$.transactions[0].status").value("APPROVED"))
                .andExpect(jsonPath("$.transactions[0].reasons[0]").value("low amount anomaly"))
                .andExpect(jsonPath("$.transactions[0].analyzedAt").value("2024-01-01T10:01:00Z"));
    }

    @Test
    void shouldReturn404WhenUserHistoryNotFound() throws Exception {
        String userId = UUID.randomUUID().toString();
        when(userService.findHistory(userId)).thenThrow(new com.fraud_detector.project.exception.ResourceNotFoundException("User not found"));

        mockMvc.perform(get("/api/v1/users/history/" + userId))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteUser() throws Exception {
        String userId = UUID.randomUUID().toString();

        mockMvc.perform(delete("/api/v1/users/" + userId))
                .andExpect(status().isNoContent());
    }
}