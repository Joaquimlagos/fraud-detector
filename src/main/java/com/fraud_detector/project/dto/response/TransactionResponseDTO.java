package com.fraud_detector.project.dto.response;

public record TransactionResponseDTO(
        String message
) {
    public static TransactionResponseDTO success() {
        return new TransactionResponseDTO("Transaction registered successfully");
    }
}
