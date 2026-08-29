package com.fraud_detector.project.service.impl;

import com.fraud_detector.project.dto.request.TransactionRequestDTO;
import com.fraud_detector.project.enums.Channel;
import com.fraud_detector.project.enums.PaymentMethod;
import com.fraud_detector.project.dto.response.TransactionResponseDTO;
import com.fraud_detector.project.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Test
    void shouldReturnAcceptedWithTransactionId() {
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

        TransactionResponseDTO response = transactionService.submit(request);

        assertNotNull(response);
        assertNotNull(response.transactionId());
        assertEquals("PENDING_ANALYSIS", response.status());

        // Verify it's a valid UUID
        UUID.fromString(response.transactionId());
    }

    @Test
    void shouldGenerateDifferentIdsForMultipleRequests() {
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

        TransactionResponseDTO response1 = transactionService.submit(request);
        TransactionResponseDTO response2 = transactionService.submit(request);

        assertNotNull(response1.transactionId());
        assertNotNull(response2.transactionId());
        assertEquals("PENDING_ANALYSIS", response1.status());
        assertEquals("PENDING_ANALYSIS", response2.status());

        // Each call should generate a different transaction ID
        assertNotEquals(response1.transactionId(), response2.transactionId());
    }
}