package com.fraud_detector.project.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record TransactionItemDTO(
        String transactionId,
        String userId,
        BigDecimal amount,
        String currency,
        String merchant,
        String merchantCategory,
        String paymentMethod,
        String cardLastFourDigits,
        String channel,
        String ipAddress,
        String deviceId,
        Double latitude,
        Double longitude,
        String billingCountry,
        String status,
        List<String> reasons,
        Instant analyzedAt,
        Instant occurredAt,
        Instant createdAt
) {}