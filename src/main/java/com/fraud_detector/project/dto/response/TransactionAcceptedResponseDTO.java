package com.fraud_detector.project.dto.response;

public record TransactionAcceptedResponseDTO(
        String transactionId,
        String status
) {
    public static TransactionAcceptedResponseDTO accepted(String transactionId) {
        return new TransactionAcceptedResponseDTO(transactionId, "PENDING_ANALYSIS");
    }
}