package com.fraud_detector.project.dto;

import java.time.Instant;

public record ItemResponseDTO(
        Long id,
        String nome,
        String descricao,
        Integer quantidade,
        Instant criadoEm,
        Instant atualizadoEm
) {}
