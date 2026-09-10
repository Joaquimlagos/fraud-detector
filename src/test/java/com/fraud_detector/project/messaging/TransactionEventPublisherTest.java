package com.fraud_detector.project.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fraud_detector.project.exception.TransactionPublishException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SqsException;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionEventPublisherTest {

    private static final String QUEUE_URL = "http://localhost:4566/000000000000/queue-name";

    @Mock
    private SqsClient sqsClient;

    private TransactionEventPublisher publisher;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        publisher = new TransactionEventPublisher(sqsClient, objectMapper);
        ReflectionTestUtils.setField(publisher, "queueName", "transaction-queue");
    }

    @Test
    void shouldSendMessageWithResolvedQueueUrlAndSerializableBody() throws Exception {
        when(sqsClient.getQueueUrl(any(GetQueueUrlRequest.class)))
                .thenReturn(GetQueueUrlResponse.builder().queueUrl(QUEUE_URL).build());

        TransactionEvent event = new TransactionEvent(
                "txn-123",
                "user-1",
                new BigDecimal("250.00"),
                "BRL",
                "Amazon BR",
                "retail",
                "CREDIT_CARD",
                "1234",
                "MOBILE_APP",
                "192.168.0.1",
                "device-1",
                -23.5505,
                -46.6333,
                "BR",
                Instant.parse("2026-09-09T14:00:00Z"),
                Instant.parse("2026-09-09T14:00:01Z")
        );

        publisher.publish(event);

        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(sqsClient).sendMessage(captor.capture());

        SendMessageRequest request = captor.getValue();
        assertEquals(QUEUE_URL, request.queueUrl());

        TransactionEvent deserialized = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .readValue(request.messageBody(), TransactionEvent.class);
        assertEquals(event, deserialized);
    }

    @Test
    void shouldThrowTransactionPublishExceptionWhenSqsFails() {
        when(sqsClient.getQueueUrl(any(GetQueueUrlRequest.class)))
                .thenReturn(GetQueueUrlResponse.builder().queueUrl(QUEUE_URL).build());
        when(sqsClient.sendMessage(any(SendMessageRequest.class)))
                .thenThrow(SqsException.builder().build());

        TransactionEvent event = new TransactionEvent(
                "txn-123",
                "user-1",
                new BigDecimal("250.00"),
                "BRL",
                "Amazon BR",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                Instant.parse("2026-09-09T14:00:00Z"),
                Instant.now()
        );

        assertThrows(TransactionPublishException.class, () -> publisher.publish(event));
    }
}