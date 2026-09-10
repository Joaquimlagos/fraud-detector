package com.fraud_detector.project.service;

import com.fraud_detector.project.dto.request.TransactionRequestDTO;
import com.fraud_detector.project.dto.response.TransactionAcceptedResponseDTO;

public interface TransactionService {
    TransactionAcceptedResponseDTO submit(TransactionRequestDTO dto);
}