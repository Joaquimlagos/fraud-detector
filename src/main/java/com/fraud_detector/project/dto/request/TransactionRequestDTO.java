package com.fraud_detector.project.dto.request;

import com.fraud_detector.project.enums.Channel;
import com.fraud_detector.project.enums.PaymentMethod;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Payload de entrada de POST /transaction.
 *
 * IMPORTANTE (ver AGENTS.md e a skill add-transaction-event): esta API
 * NUNCA avalia se algum desses campos indica fraude — apenas valida o
 * formato/presença dos dados e publica tudo no SQS. Toda a análise de
 * risco (incluindo quais destes campos realmente importam para cada
 * regra) é responsabilidade exclusiva do fraud-detector-lambda.
 *
 * Os campos além dos quatro "essenciais" (userId, amount, currency,
 * merchant) existem para dar à Lambda sinais de fraude comuns no mundo
 * real, mesmo que nenhuma regra hoje os utilize ainda — a ideia é não ter
 * que fazer uma mudança cross-repo (API + Lambda) só para começar a usar
 * um dado que já poderia estar disponível desde o início.
 */
public record TransactionRequestDTO(

        @NotBlank(message = "userId is required")
        String userId,

        @NotNull(message = "amount is required")
        @Positive(message = "amount must be positive")
        @DecimalMax(value = "1000000.00", message = "amount exceeds the maximum allowed value")
        BigDecimal amount,

        @NotBlank(message = "currency is required")
        @Pattern(regexp = "[A-Z]{3}", message = "currency must be a 3-letter ISO 4217 code (e.g. USD, BRL, EUR)")
        String currency,

        @NotBlank(message = "merchant is required")
        String merchant,

        // Categoria do estabelecimento (livre ou código MCC). Sinal de
        // risco: certas categorias (jogos de azar, câmbio de cripto) têm
        // taxa de fraude historicamente maior.
        String merchantCategory,

        @NotNull(message = "paymentMethod is required")
        PaymentMethod paymentMethod,

        // Só relevante quando paymentMethod é CREDIT_CARD/DEBIT_CARD.
        // Não há validação cruzada de "obrigatório se for cartão" aqui de
        // propósito — a API não deve carregar regra condicional de
        // negócio; só valida o formato quando o campo vier preenchido.
        @Pattern(regexp = "\\d{4}", message = "cardLastFourDigits must be exactly 4 digits")
        String cardLastFourDigits,

        @NotNull(message = "channel is required")
        Channel channel,

        // IPv4 simples. Sinal de risco: geolocalização do IP divergente do
        // país de cobrança, ou IP associado a VPN/proxy conhecido.
        @Pattern(
                regexp = "^(\\d{1,3}\\.){3}\\d{1,3}$",
                message = "ipAddress must be a valid IPv4 address"
        )
        String ipAddress,

        // Identificador do dispositivo (fingerprint gerado pelo cliente).
        // Sinal de risco: mesmo dispositivo usado por múltiplos usuários,
        // ou dispositivo novo nunca visto para este usuário.
        String deviceId,

        // Geolocalização da transação (se o cliente/app fornecer). Sinal
        // de risco clássico: "impossible travel" — duas transações do
        // mesmo usuário em locais fisicamente incompatíveis com o tempo
        // decorrido entre elas.
        @DecimalMin(value = "-90.0", message = "latitude must be between -90 and 90")
        @DecimalMax(value = "90.0", message = "latitude must be between -90 and 90")
        Double latitude,

        @DecimalMin(value = "-180.0", message = "longitude must be between -180 and 180")
        @DecimalMax(value = "180.0", message = "longitude must be between -180 and 180")
        Double longitude,

        // País de cobrança do usuário (ISO 3166-1 alpha-2, ex: "BR", "US").
        // Comparado com a geolocalização/IP para detectar divergência.
        @Pattern(regexp = "[A-Z]{2}", message = "billingCountry must be a 2-letter ISO 3166-1 country code")
        String billingCountry,

        @NotNull(message = "occurredAt is required")
        Instant occurredAt

) {}
