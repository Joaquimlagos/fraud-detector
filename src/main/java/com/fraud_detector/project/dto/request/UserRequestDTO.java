package com.fraud_detector.project.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

/**
 * Payload de entrada de POST /users (create-user).
 *
 * Campos escolhidos para servir de base às regras de fraude que dependem
 * do perfil do usuário (não da transação isolada) — ex.: comparar o país
 * de cobrança do usuário com o país de uma transação, ou usar a idade da
 * conta como fator de risco (contas muito novas fazendo transações de
 * valor alto são um sinal comum).
 *
 * document, dateOfBirth e phoneNumber são dados sensíveis (PII) — a forma
 * como são armazenados/expostos deve respeitar a legislação de proteção
 * de dados aplicável (ex. LGPD). Fora de escopo deste DTO em si, mas
 * relevante ao decidir logging, mascaramento em resposta, e retenção.
 */
public record UserRequestDTO(

        @NotBlank(message = "fullName is required")
        String fullName,

        @NotBlank(message = "email is required")
        @Email(message = "email must be a valid email address")
        String email,

        // Documento de identificação (CPF, SSN, etc., conforme o mercado
        // do projeto). Sem validação de formato específica de país aqui —
        // se o projeto for focado em um único país, vale trocar por um
        // @Pattern adequado (ex. CPF: \\d{11}).
        @NotBlank(message = "document is required")
        String document,

        @NotBlank(message = "phoneNumber is required")
        @Pattern(
                regexp = "^\\+[1-9]\\d{1,14}$",
                message = "phoneNumber must be in E.164 format (e.g. +5511999999999)"
        )
        String phoneNumber,

        @NotNull(message = "dateOfBirth is required")
        @Past(message = "dateOfBirth must be in the past")
        LocalDate dateOfBirth,

        // País de residência/cobrança do usuário (ISO 3166-1 alpha-2).
        // Usado por regras futuras que comparam com a geolocalização da
        // transação (ver TransactionRequestDTO.billingCountry).
        @NotBlank(message = "country is required")
        @Pattern(regexp = "[A-Z]{2}", message = "country must be a 2-letter ISO 3166-1 country code")
        String country

) {}