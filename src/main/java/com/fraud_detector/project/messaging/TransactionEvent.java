package com.fraud_detector.project.messaging;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Message contract with the fraud-detector-lambda (Python). Field names must
 * stay flat and stable — changing them is a cross-repo breaking change.
 * The API adds a stable, unique transactionId so the Lambda can implement
 * idempotency/dedup (see AGENTS.md).
 */
public record TransactionEvent(
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
        Instant occurredAt,
        Instant publishedAt
) {}