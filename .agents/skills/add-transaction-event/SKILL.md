---
name: add-transaction-event
description: Step-by-step guide for building the transaction ingestion flow in fraud-detector-api — validate the request, publish a TransactionEvent to SQS, and return 202 Accepted. Use this for POST /transaction and any other endpoint that follows the "validate then publish, don't persist, don't decide" async pattern. Do NOT use for simple CRUD resources (Card, Merchant, Blocklist) that read/write DynamoDB directly within the request — use the add-dynamodb-endpoint skill for those instead.
---

# Add a transaction event (validate → publish to SQS → 202)

> **Scope check — read this before doing anything else.**
>
> This skill is for the **asynchronous, event-publishing flow** — currently `POST /transaction`. The rules below are absolute, not suggestions:
>
> - **Never** write the transaction to DynamoDB from this endpoint. Only the Lambda (separate repo) persists the transaction record, after fraud analysis, with its final `status`.
> - **Never** determine or guess whether the transaction is suspicious. No time-of-day checks, no amount thresholds, no velocity logic — nothing that evaluates fraud risk.
> - **Never** publish to SNS or send any notification from here. SNS is only triggered by the Lambda, only when it finds a transaction suspicious.
> - **Never** implement any fraud rule "just this once" even if it looks trivial (e.g. "just check if the hour is between 2 and 7"). That logic belongs exclusively to the Lambda repo. See `AGENTS.md` → "System architecture" and "What NOT to do".
> - This endpoint's entire job is: validate shape → build a stable event → publish to SQS → return `202 Accepted`. Nothing else.
>
> If the resource you're adding actually reads/writes DynamoDB synchronously and returns a real result in the same request (e.g. `Card`, `Merchant`, `Blocklist`), **stop and use `add-dynamodb-endpoint` instead** — this skill doesn't apply.

Package base: `com.frauddetector.project` (adjust if the real groupId differs). All code below uses `Transaction` as the example — this is the actual `/transaction` endpoint, not a template to rename, but the same shape applies if another async/event-publishing endpoint is added later.

## Step 1 — Request DTO (`dto/TransactionRequestDTO.java`)

Validates only the shape of the payload — no business meaning is attached to any field here.

```java
package com.frauddetector.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Instant;

public record TransactionRequestDTO(
        @NotBlank(message = "userId is required")
        String userId,

        @NotNull(message = "amount is required")
        @Positive(message = "amount must be positive")
        java.math.BigDecimal amount,

        @NotBlank(message = "currency is required")
        String currency,

        @NotBlank(message = "merchant is required")
        String merchant,

        @NotNull(message = "occurredAt is required")
        Instant occurredAt
) {}
```

> `occurredAt` is the timestamp the client claims the transaction happened at — this is a data field the Lambda will use for its own rules (e.g. time-of-day). Do not interpret or validate its "riskiness" here, only that it's present and well-formed.

## Step 2 — Response DTO (`dto/TransactionAcceptedResponseDTO.java`)

The API can only confirm receipt, not a verdict — the response reflects that.

```java
package com.frauddetector.project.dto;

public record TransactionAcceptedResponseDTO(
        String transactionId,
        String status
) {
    public static TransactionAcceptedResponseDTO accepted(String transactionId) {
        return new TransactionAcceptedResponseDTO(transactionId, "PENDING_ANALYSIS");
    }
}
```

## Step 3 — Event payload (`messaging/TransactionEvent.java`)

This is the message contract with the Lambda repo (Python). Keep it a plain, stable, serializable shape — changing field names/types here is a cross-repo breaking change, so treat it deliberately.

```java
package com.frauddetector.project.messaging;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionEvent(
        String transactionId,
        String userId,
        BigDecimal amount,
        String currency,
        String merchant,
        Instant occurredAt,
        Instant publishedAt
) {}
```

## Step 4 — SQS producer (`messaging/TransactionEventPublisher.java`)

One producer class per event type. Uses the AWS SDK v2 `SqsClient`, injected as a bean (configured once in `config/AwsConfig.java` — reuse it, don't build clients inline).

```java
package com.frauddetector.project.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Component
public class TransactionEventPublisher {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private final String queueUrl;

    public TransactionEventPublisher(
            SqsClient sqsClient,
            ObjectMapper objectMapper,
            @Value("${aws.sqs.transaction-queue-url}") String queueUrl) {
        this.sqsClient = sqsClient;
        this.objectMapper = objectMapper;
        this.queueUrl = queueUrl;
    }

    public void publish(TransactionEvent event) {
        try {
            String body = objectMapper.writeValueAsString(event);

            SendMessageRequest request = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(body)
                    .build();

            sqsClient.sendMessage(request);
        } catch (Exception e) {
            throw new TransactionPublishException("Failed to publish transaction event: " + event.transactionId(), e);
        }
    }
}
```

`aws.sqs.transaction-queue-url` goes in `application-dev.yml`/`application-prod.yml`, sourced from an environment variable in `prod` — never hardcode the queue URL.

> If the queue is SQS FIFO (see `AGENTS.md` for the current decision on FIFO vs standard), add `.messageGroupId(event.userId())` and a `.messageDeduplicationId(event.transactionId())` to the request builder.

## Step 5 — Exception (`exception/TransactionPublishException.java`)

```java
package com.frauddetector.project.exception;

public class TransactionPublishException extends RuntimeException {
    public TransactionPublishException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

Handled by the existing `GlobalExceptionHandler`'s generic `Exception` handler (returns 500) — no special-casing needed unless you want a distinct status code for publish failures.

## Step 6 — Service (`service/TransactionService.java` + `service/impl/TransactionServiceImpl.java`)

The service's only real job is generating the `transactionId` and assembling the event — there is no repository call here.

```java
package com.frauddetector.project.service;

import com.frauddetector.project.dto.TransactionAcceptedResponseDTO;
import com.frauddetector.project.dto.TransactionRequestDTO;

public interface TransactionService {
    TransactionAcceptedResponseDTO submit(TransactionRequestDTO dto);
}
```

```java
package com.frauddetector.project.service.impl;

import com.frauddetector.project.dto.TransactionAcceptedResponseDTO;
import com.frauddetector.project.dto.TransactionRequestDTO;
import com.frauddetector.project.messaging.TransactionEvent;
import com.frauddetector.project.messaging.TransactionEventPublisher;
import com.frauddetector.project.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionEventPublisher publisher;

    @Override
    public TransactionAcceptedResponseDTO submit(TransactionRequestDTO dto) {
        String transactionId = UUID.randomUUID().toString();

        TransactionEvent event = new TransactionEvent(
                transactionId,
                dto.userId(),
                dto.amount(),
                dto.currency(),
                dto.merchant(),
                dto.occurredAt(),
                Instant.now()
        );

        publisher.publish(event);

        return TransactionAcceptedResponseDTO.accepted(transactionId);
    }
}
```

## Step 7 — Controller (`controller/TransactionController.java`)

```java
package com.frauddetector.project.controller;

import com.frauddetector.project.dto.TransactionAcceptedResponseDTO;
import com.frauddetector.project.dto.TransactionRequestDTO;
import com.frauddetector.project.service.TransactionService;
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
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionAcceptedResponseDTO> submit(@Valid @RequestBody TransactionRequestDTO dto) {
        TransactionAcceptedResponseDTO accepted = transactionService.submit(dto);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(accepted);
    }
}
```

Note there is no `GET /transactions/{id}` here — whether/how the client retrieves the final status is still an open decision (see `AGENTS.md` → "System architecture"). Don't add a status-retrieval endpoint as part of this skill without checking that decision first.

## Step 8 — Tests

Follow `.agents/rules/java-test-rules.md`.

- `TransactionServiceImplTest` — mock `TransactionEventPublisher`; assert a `TransactionEvent` with all DTO fields correctly mapped is passed to `publish()`; assert the returned `transactionId` is a valid UUID and status is `PENDING_ANALYSIS`.
- `TransactionControllerTest` — `@WebMvcTest(TransactionController.class)` + `MockMvc`; mock `TransactionService`; cover 202 on valid body, 400 on each missing/invalid field (`userId`, `amount` ≤ 0, missing `occurredAt`, etc.).
- `TransactionEventPublisherTest` — mock `SqsClient`; verify `sendMessage` is called with the correct queue URL and a body that deserializes back into the expected event; verify `TransactionPublishException` is thrown when the SQS call fails.
- **Do not** write a test asserting anything about fraud status, DynamoDB persistence, or SNS from this endpoint — those don't exist here by design, so there's nothing to test.

## Checklist before considering this done

- [ ] Controller has no business logic — only delegates to `Service`
- [ ] Service does not call any `Repository` or write to DynamoDB
- [ ] No field in `TransactionRequestDTO`/`TransactionEvent` is evaluated for "riskiness" anywhere in this repo
- [ ] No SNS client or notification code was added here
- [ ] `TransactionEvent` field names/types match what the Lambda repo expects — check the message contract before renaming anything
- [ ] Queue URL comes from config/environment variable, never hardcoded
- [ ] Response is `202 Accepted` with `transactionId`, not an approve/deny verdict
- [ ] Confirmed this resource IS the async/SQS-publishing flow — if it's actually a synchronous CRUD resource, use `add-dynamodb-endpoint` instead
