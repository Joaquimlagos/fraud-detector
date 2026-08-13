package com.fraud_detector.project.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ItemRequestDTO(
        @NotBlank(message = "nome é obrigatório")
        String nome,

        String descricao,

        @NotNull(message = "quantidade é obrigatória")
        @Min(value = 0, message = "quantidade não pode ser negativa")
        Integer quantidade
) {}
