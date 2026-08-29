package com.fraud_detector.project.dto.response;

public record TransactionResponseDTO(
        String transactionId,
        String status
) {
    public static TransactionResponseDTO accepted(String transactionId) {
        return new TransactionResponseDTO(transactionId, "PENDING_ANALYSIS");
    }
}
