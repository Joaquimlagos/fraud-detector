package com.fraud_detector.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraud_detector.project.dto.request.TransactionRequestDTO;
import com.fraud_detector.project.dto.response.TransactionAcceptedResponseDTO;
import com.fraud_detector.project.dto.response.TransactionAnalysisResponseDTO;
import com.fraud_detector.project.dto.response.SimilarCaseDTO;
import com.fraud_detector.project.enums.Channel;
import com.fraud_detector.project.enums.PaymentMethod;
import com.fraud_detector.project.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionService transactionService;

    @Test
    void shouldReturnAcceptedWhenTransactionIsValid() throws Exception {
        String transactionId = UUID.randomUUID().toString();
        TransactionAcceptedResponseDTO response = TransactionAcceptedResponseDTO.accepted(transactionId);

        when(transactionService.submit(any())).thenReturn(response);

        TransactionRequestDTO request = new TransactionRequestDTO(
                "user-123",
                new BigDecimal("100.50"),
                "USD",
                "Store ABC",
                "retail",
                PaymentMethod.CREDIT_CARD,
                "1234",
                Channel.MOBILE_APP,
                "192.168.0.1",
                "device-1",
                -23.5505,
                -46.6333,
                "BR",
                Instant.now()
        );

        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.transactionId").value(transactionId));
    }

    @Test
    void shouldReturnBadRequestWhenFieldsAreMissing() throws Exception {
        TransactionRequestDTO request = new TransactionRequestDTO(
                "",
                null,
                "",
                "",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnAnalysisWhenTransactionIdExists() throws Exception {
        String transactionId = "abc-123";
        TransactionAnalysisResponseDTO response = new TransactionAnalysisResponseDTO(
                transactionId,
                "suspicious",
                "A transação apresenta padrão compatível com alta velocidade de operações.",
                List.of(new SimilarCaseDTO("old-456", 0.92)),
                false
        );

        when(transactionService.findAnalysis(transactionId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/transactions/" + transactionId + "/analysis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value(transactionId))
                .andExpect(jsonPath("$.status").value("suspicious"))
                .andExpect(jsonPath("$.reasoning").value("A transação apresenta padrão compatível com alta velocidade de operações."))
                .andExpect(jsonPath("$.similarCases[0].transactionId").value("old-456"))
                .andExpect(jsonPath("$.similarCases[0].similarity").value(0.92))
                .andExpect(jsonPath("$.cached").value(false));
    }
}