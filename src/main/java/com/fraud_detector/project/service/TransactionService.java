package com.fraud_detector.project.service;

import com.fraud_detector.project.dto.request.TransactionRequestDTO;
import com.fraud_detector.project.dto.response.TransactionResponseDTO;

public interface TransactionService {
    TransactionResponseDTO submit(TransactionRequestDTO dto);
}