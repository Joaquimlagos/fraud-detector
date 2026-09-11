package com.fraud_detector.project.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraud_detector.project.exception.TransactionPublishException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Component
@RequiredArgsConstructor
public class TransactionEventPublisher {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;

    @Value("${aws.sqs.queue-name}")
    private String queueName;

    private volatile String cachedQueueUrl;

    public void publish(TransactionEvent event) {
        try {
            String payloadJson = objectMapper.writeValueAsString(event);
            SendMessageRequest request = SendMessageRequest.builder()
                    .queueUrl(getQueueUrl())
                    .messageBody(payloadJson)
                    .build();

            sqsClient.sendMessage(request);
        } catch (TransactionPublishException e) {
            throw e;
        } catch (Exception e) {
            throw new TransactionPublishException(
                    "Failed to publish transaction event: " + event.transactionId(), e);
        }
    }

    private String getQueueUrl() {
        if (cachedQueueUrl == null) {
            synchronized (this) {
                if (cachedQueueUrl == null) {
                    cachedQueueUrl = sqsClient.getQueueUrl(
                            GetQueueUrlRequest.builder().queueName(queueName).build()
                    ).queueUrl();
                }
            }
        }
        return cachedQueueUrl;
    }
}