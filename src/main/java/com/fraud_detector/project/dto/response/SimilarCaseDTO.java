package com.fraud_detector.project.dto.response;

public record SimilarCaseDTO(
        String transactionId,
        Double similarity
) {}
