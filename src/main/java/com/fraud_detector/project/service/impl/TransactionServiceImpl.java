package com.fraud_detector.project.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraud_detector.project.dto.request.TransactionRequestDTO;
import com.fraud_detector.project.dto.response.TransactionResponseDTO;
import com.fraud_detector.project.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;

    @Value("${aws.sqs.queue-name}")
    private String queueName;

    private volatile String cachedQueueUrl;

    @Override
    public TransactionResponseDTO submit(TransactionRequestDTO dto) {
        String transactionId = UUID.randomUUID().toString();

        try {
            String payloadJson = objectMapper.writeValueAsString(dto);
            String queueUrl = getQueueUrl();

            SendMessageRequest sendMessageRequest = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(payloadJson)
                    .build();

            sqsClient.sendMessage(sendMessageRequest);
            log.info("Transaction {} successfully published to SQS queue {}", transactionId, queueName);

        } catch (JsonProcessingException e) {
            log.error("Error serializing transaction object: ", e);
            throw new RuntimeException("Failed to process transaction payload", e);
        } catch (Exception e) {
            log.error("Error sending message to SQS queue: ", e);
            throw new RuntimeException("Failed to publish transaction to SQS", e);
        }

        return TransactionResponseDTO.success();
    }

    private String getQueueUrl() {
        if (cachedQueueUrl == null) {
            synchronized (this) {
                if (cachedQueueUrl == null) {
                    cachedQueueUrl = sqsClient.getQueueUrl(
                            GetQueueUrlRequest.builder()
                                    .queueName(queueName)
                                    .build()
                    ).queueUrl();
                }
            }
        }
        return cachedQueueUrl;
    }
}