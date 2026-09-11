package com.fraud_detector.project.service;

import com.fraud_detector.project.dto.request.TransactionRequestDTO;
import com.fraud_detector.project.dto.response.TransactionAcceptedResponseDTO;
import com.fraud_detector.project.dto.response.TransactionAnalysisResponseDTO;

public interface TransactionService {
    TransactionAcceptedResponseDTO submit(TransactionRequestDTO dto);

    TransactionAnalysisResponseDTO findAnalysis(String transactionId);
}