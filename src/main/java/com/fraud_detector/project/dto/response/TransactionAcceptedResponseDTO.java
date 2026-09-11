package com.fraud_detector.project.dto.response;

public record TransactionAcceptedResponseDTO(
        String transactionId
) {
    public static TransactionAcceptedResponseDTO accepted(String transactionId) {
        return new TransactionAcceptedResponseDTO(transactionId);
    }
}