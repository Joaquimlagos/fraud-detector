package com.fraud_detector.project.service.impl;

import com.fraud_detector.project.dto.request.TransactionRequestDTO;
import com.fraud_detector.project.dto.response.TransactionAcceptedResponseDTO;
import com.fraud_detector.project.dto.response.TransactionAnalysisResponseDTO;
import com.fraud_detector.project.enums.Channel;
import com.fraud_detector.project.enums.PaymentMethod;
import com.fraud_detector.project.messaging.TransactionAnalysisClient;
import com.fraud_detector.project.messaging.TransactionEvent;
import com.fraud_detector.project.messaging.TransactionEventPublisher;
import com.fraud_detector.project.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionEventPublisher publisher;
    private final TransactionAnalysisClient analysisClient;

    @Override
    public TransactionAcceptedResponseDTO submit(TransactionRequestDTO dto) {
        String transactionId = UUID.randomUUID().toString();

        TransactionEvent event = toEvent(transactionId, dto);
        publisher.publish(event);

        return TransactionAcceptedResponseDTO.accepted(transactionId);
    }

    @Override
    public TransactionAnalysisResponseDTO findAnalysis(String transactionId) {
        return analysisClient.invokeAnalysis(transactionId);
    }

    private TransactionEvent toEvent(String transactionId, TransactionRequestDTO dto) {
        return new TransactionEvent(
                transactionId,
                dto.userId(),
                dto.amount(),
                dto.currency(),
                dto.merchant(),
                dto.merchantCategory(),
                paymentMethod(dto),
                dto.cardLastFourDigits(),
                channel(dto),
                dto.ipAddress(),
                dto.deviceId(),
                dto.latitude(),
                dto.longitude(),
                dto.billingCountry(),
                dto.occurredAt(),
                Instant.now()
        );
    }

    private String paymentMethod(TransactionRequestDTO dto) {
        PaymentMethod method = dto.paymentMethod();
        return method == null ? null : method.name();
    }

    private String channel(TransactionRequestDTO dto) {
        Channel channel = dto.channel();
        return channel == null ? null : channel.name();
    }
}