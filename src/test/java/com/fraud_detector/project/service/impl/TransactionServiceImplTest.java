package com.fraud_detector.project.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fraud_detector.project.dto.request.TransactionRequestDTO;
import com.fraud_detector.project.enums.Channel;
import com.fraud_detector.project.enums.PaymentMethod;
import com.fraud_detector.project.dto.response.TransactionResponseDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private SqsClient sqsClient;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Test
    void shouldReturnSuccessMessageWhenTransactionPublished() {
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

        when(sqsClient.getQueueUrl(any(GetQueueUrlRequest.class)))
                .thenReturn(GetQueueUrlResponse.builder()
                        .queueUrl("http://localhost:4566/000000000000/queue-name")
                        .build());
        when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                .thenReturn(SendMessageResponse.builder().build());

        TransactionResponseDTO response = transactionService.submit(request);

        assertNotNull(response);
        assertEquals("Transaction registered successfully", response.message());
        verify(sqsClient).sendMessage(any(SendMessageRequest.class));
    }

    @Test
    void shouldPublishEachRequestToSqs() {
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

        when(sqsClient.getQueueUrl(any(GetQueueUrlRequest.class)))
                .thenReturn(GetQueueUrlResponse.builder()
                        .queueUrl("http://localhost:4566/000000000000/queue-name")
                        .build());
        when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                .thenReturn(SendMessageResponse.builder().build());

        TransactionResponseDTO response1 = transactionService.submit(request);
        TransactionResponseDTO response2 = transactionService.submit(request);

        assertNotNull(response1);
        assertNotNull(response2);
        assertEquals("Transaction registered successfully", response1.message());
        assertEquals("Transaction registered successfully", response2.message());

        verify(sqsClient, org.mockito.Mockito.times(2)).sendMessage(any(SendMessageRequest.class));
    }
}