package com.fraud_detector.project.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraud_detector.project.dto.response.TransactionAnalysisResponseDTO;
import com.fraud_detector.project.exception.TransactionAnalysisException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvocationType;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class TransactionAnalysisClient {

    private final LambdaClient lambdaClient;
    private final ObjectMapper objectMapper;

    @Value("${aws.lambda.function-name:fraud-detector-analysis-dev}")
    private String functionName;

    public TransactionAnalysisResponseDTO invokeAnalysis(String transactionId) {
        try {
            String requestPayload = objectMapper.writeValueAsString(Map.of("transactionId", transactionId));

            InvokeRequest request = InvokeRequest.builder()
                    .functionName(functionName)
                    .invocationType(InvocationType.REQUEST_RESPONSE)
                    .payload(SdkBytes.fromUtf8String(requestPayload))
                    .build();

            InvokeResponse response = lambdaClient.invoke(request);

            if (response.functionError() != null && !response.functionError().isBlank()) {
                throw new TransactionAnalysisException("Lambda error: " + response.functionError());
            }

            String analysisJson = response.payload().asUtf8String();
            return objectMapper.readValue(analysisJson, TransactionAnalysisResponseDTO.class);
        } catch (TransactionAnalysisException e) {
            throw e;
        } catch (Exception e) {
            throw new TransactionAnalysisException(
                    "Failed to invoke fraud analysis for transaction: " + transactionId, e);
        }
    }
}
