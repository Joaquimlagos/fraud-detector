---
name: add-dynamodb-endpoint
description: Step-by-step guide for adding a new simple, synchronous, DynamoDB-backed CRUD-style REST endpoint to the fraud-detector-api (e.g. Card, Merchant, Blocklist) — a resource that is validated and persisted directly to DynamoDB within the request. Do NOT use for the transaction ingestion endpoint (POST /transaction) or any other asynchronous/SQS-publishing endpoint — those follow a validate-then-publish flow with no direct persistence and belong to a different skill (add-transaction-event).
---

# Add a DynamoDB-backed REST endpoint

> **Scope check — read this before doing anything else.**
>
> **DO NOT use this skill for the transaction ingestion endpoint (`POST /transaction`)** or for any other endpoint that follows the async/SQS-publishing pattern.
>
> The transaction endpoint is asynchronous and SQS-backed:
> - It must **not** persist the transaction directly to DynamoDB — only the Lambda (separate repo) writes the final transaction record, after analysis.
> - It must **not** implement fraud rules (time-of-day, velocity, blocklist, or any suspicious-activity logic) — see `AGENTS.md` → "What NOT to do".
> - It must validate the payload, publish to SQS, and return `202 Accepted` with the generated `transactionId`. Nothing else.
> - Use the `add-transaction-event` skill for that flow instead.
>
> This skill below is only for **simple, synchronous, CRUD-style resources** — the endpoint receives a request, validates it, reads/writes DynamoDB directly, and returns a response in the same request cycle. If the resource you're adding needs to publish an event and defer processing, stop and use the event-publishing pattern instead.

Package base: `com.frauddetector.project` (adjust if the real groupId differs).

## Step 1 — Model (`model/Card.java`)

Use the AWS SDK v2 Enhanced Client annotations. Every field that maps to the partition key/sort key must be explicitly annotated.

```java
package com.frauddetector.project.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.time.Instant;

@DynamoDbBean
public class Card {

    private String cardId;
    private String userId;
    private String lastFourDigits;
    private String status;
    private Instant createdAt;

    @DynamoDbPartitionKey
    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLastFourDigits() {
        return lastFourDigits;
    }

    public void setLastFourDigits(String lastFourDigits) {
        this.lastFourDigits = lastFourDigits;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
```

> `@DynamoDbBean` requires a no-args constructor and public getters/setters — it does **not** work with Lombok's `@Value` or with `record`. Don't fight this; plain getters/setters are correct here, unlike the DTOs below.

If the table needs a sort key (e.g. querying all cards for a `userId`), add `@DynamoDbSecondaryPartitionKey` / a GSI — document the actual key schema in `AGENTS.md` once decided, since key design affects query patterns project-wide.

## Step 2 — DTOs (`dto/CardRequestDTO.java`, `dto/CardResponseDTO.java`)

DTOs are always `record`, with Bean Validation on the request side.

```java
package com.frauddetector.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CardRequestDTO(
        @NotBlank(message = "userId is required")
        String userId,

        @NotBlank(message = "lastFourDigits is required")
        @Pattern(regexp = "\\d{4}", message = "lastFourDigits must be exactly 4 digits")
        String lastFourDigits
) {}
```

```java
package com.frauddetector.project.dto;

import java.time.Instant;

public record CardResponseDTO(
        String cardId,
        String userId,
        String lastFourDigits,
        String status,
        Instant createdAt
) {}
```

## Step 3 — Mapper (`mapper/CardMapper.java`)

```java
package com.frauddetector.project.mapper;

import com.frauddetector.project.dto.CardRequestDTO;
import com.frauddetector.project.dto.CardResponseDTO;
import com.frauddetector.project.model.Card;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CardMapper {

    Card toModel(CardRequestDTO dto);

    CardResponseDTO toResponseDTO(Card model);
}
```

## Step 4 — Repository (`repository/CardRepository.java`)

Wraps the `DynamoDbEnhancedClient`. One repository class per table, injected with the pre-configured `DynamoDbTable<Card>` bean (defined once in `config/DynamoDbConfig.java` — reuse it, don't recreate table references per repository).

```java
package com.frauddetector.project.repository;

import com.frauddetector.project.model.Card;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;

import java.util.Optional;

@Repository
public class CardRepository {

    private final DynamoDbTable<Card> table;

    public CardRepository(DynamoDbTable<Card> cardTable) {
        this.table = cardTable;
    }

    public Card save(Card card) {
        table.putItem(card);
        return card;
    }

    public Optional<Card> findById(String cardId) {
        Card card = table.getItem(Key.builder().partitionValue(cardId).build());
        return Optional.ofNullable(card);
    }

    public void deleteById(String cardId) {
        table.deleteItem(Key.builder().partitionValue(cardId).build());
    }
}
```

If the `DynamoDbTable<Card>` bean doesn't exist yet in `config/DynamoDbConfig.java`, add it there — don't instantiate `DynamoDbEnhancedClient` inline in the repository.

## Step 5 — Service (`service/CardService.java` + `service/impl/CardServiceImpl.java`)

```java
package com.frauddetector.project.service;

import com.frauddetector.project.dto.CardRequestDTO;
import com.frauddetector.project.dto.CardResponseDTO;

public interface CardService {
    CardResponseDTO create(CardRequestDTO dto);
    CardResponseDTO findById(String cardId);
    void delete(String cardId);
}
```

```java
package com.frauddetector.project.service.impl;

import com.frauddetector.project.dto.CardRequestDTO;
import com.frauddetector.project.dto.CardResponseDTO;
import com.frauddetector.project.exception.ResourceNotFoundException;
import com.frauddetector.project.mapper.CardMapper;
import com.frauddetector.project.model.Card;
import com.frauddetector.project.repository.CardRepository;
import com.frauddetector.project.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final CardMapper cardMapper;

    @Override
    public CardResponseDTO create(CardRequestDTO dto) {
        Card card = cardMapper.toModel(dto);
        card.setCardId(UUID.randomUUID().toString());
        card.setStatus("ACTIVE");
        card.setCreatedAt(Instant.now());

        Card saved = cardRepository.save(card);
        return cardMapper.toResponseDTO(saved);
    }

    @Override
    public CardResponseDTO findById(String cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found with id: " + cardId));
        return cardMapper.toResponseDTO(card);
    }

    @Override
    public void delete(String cardId) {
        findById(cardId); // throws ResourceNotFoundException if it doesn't exist
        cardRepository.deleteById(cardId);
    }
}
```

## Step 6 — Controller (`controller/CardController.java`)

Reuses the existing `GlobalExceptionHandler` — no new exception-handling code needed here.

```java
package com.frauddetector.project.controller;

import com.frauddetector.project.dto.CardRequestDTO;
import com.frauddetector.project.dto.CardResponseDTO;
import com.frauddetector.project.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @PostMapping
    public ResponseEntity<CardResponseDTO> create(@Valid @RequestBody CardRequestDTO dto) {
        CardResponseDTO created = cardService.create(dto);
        return ResponseEntity.created(URI.create("/api/v1/cards/" + created.cardId())).body(created);
    }

    @GetMapping("/{cardId}")
    public ResponseEntity<CardResponseDTO> findById(@PathVariable String cardId) {
        return ResponseEntity.ok(cardService.findById(cardId));
    }

    @DeleteMapping("/{cardId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String cardId) {
        cardService.delete(cardId);
    }
}
```

## Step 7 — Tests

Follow `.agents/rules/java-test-rules.md`. Minimum coverage for a new resource:

- `CardServiceImplTest` — mock `CardRepository` and `CardMapper`; cover create (happy path), `findById` not found (throws `ResourceNotFoundException`), delete not found.
- `CardControllerTest` — `@WebMvcTest(CardController.class)` + `MockMvc`; mock `CardService` via `@MockBean`; cover 201 on create, 400 on invalid body (missing `userId`, malformed `lastFourDigits`), 404 on not-found.
- Repository test is optional for simple key-based access; add one if the repository grows custom queries (GSI, filter expressions) beyond `save`/`findById`/`deleteById`.

## Checklist before considering the feature done

- [ ] Model has no business logic — it's a plain data holder
- [ ] DTOs are `record`, request DTO has validation annotations
- [ ] Controller never touches `CardRepository` directly
- [ ] `DynamoDbTable<Card>` bean added to `config/DynamoDbConfig.java`, not created inline
- [ ] Partition key (and sort key, if any) documented in `AGENTS.md` under project conventions
- [ ] Tests added for service and controller layers
- [ ] Confirmed this resource is NOT the transaction ingestion endpoint or any other async/SQS-publishing flow (see scope check at the top of this file)
