package com.fraud_detector.project.dto.response;

import java.util.List;

public record UserHistoryResponseDTO(
        String userId,
        String name,
        List<TransactionItemDTO> transactions
) {}
