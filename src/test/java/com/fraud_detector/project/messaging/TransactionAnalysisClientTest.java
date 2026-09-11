package com.fraud_detector.project.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fraud_detector.project.dto.response.TransactionAnalysisResponseDTO;
import com.fraud_detector.project.exception.TransactionAnalysisException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionAnalysisClientTest {

    @Mock
    private LambdaClient lambdaClient;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private TransactionAnalysisClient analysisClient;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(analysisClient, "functionName", "fraud-detector-analysis-dev");
    }

    @Test
    void shouldInvokeLambdaAndReturnAnalysis() {
        String jsonResponse = """
                {
                  "transactionId": "abc-123",
                  "status": "suspicious",
                  "reasoning": "A transação apresenta padrão compatível com alta velocidade de operações.",
                  "similarCases": [
                    {
                      "transactionId": "old-456",
                      "similarity": 0.92
                    }
                  ],
                  "cached": false
                }
                """;

        InvokeResponse invokeResponse = InvokeResponse.builder()
                .payload(SdkBytes.fromUtf8String(jsonResponse))
                .build();

        when(lambdaClient.invoke(any(InvokeRequest.class))).thenReturn(invokeResponse);

        TransactionAnalysisResponseDTO response = analysisClient.invokeAnalysis("abc-123");

        ArgumentCaptor<InvokeRequest> captor = ArgumentCaptor.forClass(InvokeRequest.class);
        verify(lambdaClient).invoke(captor.capture());

        InvokeRequest request = captor.getValue();
        assertEquals("fraud-detector-analysis-dev", request.functionName());
        assertEquals("{\"transactionId\":\"abc-123\"}", request.payload().asUtf8String());

        assertNotNull(response);
        assertEquals("abc-123", response.transactionId());
        assertEquals("suspicious", response.status());
        assertEquals("A transação apresenta padrão compatível com alta velocidade de operações.", response.reasoning());
        assertFalse(response.cached());
        assertEquals(1, response.similarCases().size());
        assertEquals("old-456", response.similarCases().getFirst().transactionId());
        assertEquals(0.92, response.similarCases().getFirst().similarity());
    }

    @Test
    void shouldThrowTransactionAnalysisExceptionWhenLambdaReturnsFunctionError() {
        InvokeResponse invokeResponse = InvokeResponse.builder()
                .functionError("Unhandled")
                .payload(SdkBytes.fromUtf8String("{\"errorMessage\":\"Something went wrong\"}"))
                .build();

        when(lambdaClient.invoke(any(InvokeRequest.class))).thenReturn(invokeResponse);

        assertThrows(TransactionAnalysisException.class, () -> analysisClient.invokeAnalysis("abc-123"));
    }
}
