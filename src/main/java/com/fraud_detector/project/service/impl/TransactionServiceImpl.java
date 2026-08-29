package com.fraud_detector.project.service.impl;

import com.fraud_detector.project.dto.request.TransactionRequestDTO;
import com.fraud_detector.project.dto.response.TransactionResponseDTO;
import com.fraud_detector.project.service.TransactionService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TransactionServiceImpl implements TransactionService {

    @Override
    public TransactionResponseDTO submit(TransactionRequestDTO dto) {
        String transactionId = UUID.randomUUID().toString();
        return TransactionResponseDTO.accepted(transactionId);
    }
}