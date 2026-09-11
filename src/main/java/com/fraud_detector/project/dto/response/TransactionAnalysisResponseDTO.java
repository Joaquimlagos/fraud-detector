package com.fraud_detector.project.dto.response;

import java.util.List;

public record TransactionAnalysisResponseDTO(
        String transactionId,
        String status,
        String reasoning,
        List<SimilarCaseDTO> similarCases,
        Boolean cached
) {}
