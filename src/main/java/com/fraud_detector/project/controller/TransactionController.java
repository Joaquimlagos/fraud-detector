package com.fraud_detector.project.controller;

import com.fraud_detector.project.dto.request.TransactionRequestDTO;
import com.fraud_detector.project.dto.response.TransactionAcceptedResponseDTO;
import com.fraud_detector.project.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Transaction ingestion operations")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    @Operation(summary = "Submits a transaction for fraud analysis")
    public ResponseEntity<TransactionAcceptedResponseDTO> submit(@Valid @RequestBody TransactionRequestDTO dto) {
        TransactionAcceptedResponseDTO accepted = transactionService.submit(dto);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(accepted);
    }
}