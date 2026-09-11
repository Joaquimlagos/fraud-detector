package com.fraud_detector.project.service.impl;

import com.fraud_detector.project.dto.request.TransactionRequestDTO;
import com.fraud_detector.project.dto.response.TransactionAcceptedResponseDTO;
import com.fraud_detector.project.dto.response.TransactionAnalysisResponseDTO;
import com.fraud_detector.project.dto.response.SimilarCaseDTO;
import com.fraud_detector.project.enums.Channel;
import com.fraud_detector.project.enums.PaymentMethod;
import com.fraud_detector.project.messaging.TransactionAnalysisClient;
import com.fraud_detector.project.messaging.TransactionEvent;
import com.fraud_detector.project.messaging.TransactionEventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionEventPublisher publisher;

    @Mock
    private TransactionAnalysisClient analysisClient;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Test
    void shouldPublishEventWithAllDtoFieldsMapped() {
        Instant occurredAt = Instant.parse("2026-09-09T14:00:00Z");
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
                occurredAt
        );

        TransactionAcceptedResponseDTO response = transactionService.submit(request);

        ArgumentCaptor<TransactionEvent> captor = ArgumentCaptor.forClass(TransactionEvent.class);
        verify(publisher).publish(captor.capture());
        TransactionEvent event = captor.getValue();

        assertNotNull(event.transactionId());
        assertNotNull(UUID.fromString(event.transactionId()));
        assertEquals(request.userId(), event.userId());
        assertEquals(request.amount(), event.amount());
        assertEquals(request.currency(), event.currency());
        assertEquals(request.merchant(), event.merchant());
        assertEquals(request.merchantCategory(), event.merchantCategory());
        assertEquals(request.paymentMethod().name(), event.paymentMethod());
        assertEquals(request.cardLastFourDigits(), event.cardLastFourDigits());
        assertEquals(request.channel().name(), event.channel());
        assertEquals(request.ipAddress(), event.ipAddress());
        assertEquals(request.deviceId(), event.deviceId());
        assertEquals(request.latitude(), event.latitude());
        assertEquals(request.longitude(), event.longitude());
        assertEquals(request.billingCountry(), event.billingCountry());
        assertEquals(occurredAt, event.occurredAt());
        assertNotNull(event.publishedAt());

        assertNotNull(response.transactionId());
        assertEquals(event.transactionId(), response.transactionId());
    }

    @Test
    void shouldGenerateDifferentTransactionIdPerSubmission() {
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

        TransactionAcceptedResponseDTO first = transactionService.submit(request);
        TransactionAcceptedResponseDTO second = transactionService.submit(request);

        assertNotNull(first.transactionId());
        assertNotNull(second.transactionId());
        org.junit.jupiter.api.Assertions.assertNotEquals(first.transactionId(), second.transactionId());
    }

    @Test
    void shouldDelegateToAnalysisClientWhenFindingAnalysis() {
        TransactionAnalysisResponseDTO expected = new TransactionAnalysisResponseDTO(
                "abc-123",
                "suspicious",
                "A transação apresenta padrão compatível com alta velocidade de operações.",
                List.of(new SimilarCaseDTO("old-456", 0.92)),
                false
        );
        when(analysisClient.invokeAnalysis("abc-123")).thenReturn(expected);

        TransactionAnalysisResponseDTO result = transactionService.findAnalysis("abc-123");

        assertEquals(expected.transactionId(), result.transactionId());
        assertEquals("suspicious", result.status());
        assertEquals(1, result.similarCases().size());
        verify(analysisClient).invokeAnalysis("abc-123");
    }
}