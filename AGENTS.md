# AGENTS.md

Context guide for AI agents (opencode, Claude Code, etc.) working in this repository.

## Language

**English is the project's primary language.** All code, comments, commit messages, and documentation must be written in English. See `agents/rules/RULES.md` for full details.

## Overview

Project: **fraud-detector-api**
Scope: **this repository is the API only.** It exposes REST endpoints, manages users, and publishes transaction events to SQS. It does **not** contain fraud-detection logic.
Stack: Java 21, Spring Boot 3.3.4, Maven, MVC architecture.
Database: DynamoDB (AWS SDK v2, DynamoDB Enhanced Client) — no relational database, no JPA/Hibernate, no Flyway.

## System architecture (cross-repo)

This is one piece of a larger event-driven system. The other piece — the fraud analysis engine — lives in a **separate repository, written in Python, running as an AWS Lambda**. This repo has no code dependency on it, only a message-contract dependency via SQS/SNS.

```
[this repo]                          [separate Python repo]
API (Spring Boot) ──publishes──▶ SQS ──consumes──▶ Lambda (fraud rules engine)
   │                                                    │
   ├─ create-user / delete-user ──▶ DynamoDB (users)     ├─ reads history ──▶ DynamoDB
   │                                                    ├─ writes result ──▶ DynamoDB (transactions, always — every status)
   └─ /transaction ──▶ publishes raw payload to SQS       └─ if SUSPICIOUS ──▶ SNS ──▶ email
```

Key decisions from architecture review (see conversation history / update this section if they change):
- **The API never decides whether a transaction is suspicious.** It only validates the request shape (required fields, types) and publishes to SQS. All fraud-detection logic — including simple rules like time-of-day — lives exclusively in the Lambda, to avoid duplicating/diverging business rules across two codebases.
- **Every transaction is persisted**, regardless of the Lambda's verdict. The Lambda writes the transaction to DynamoDB with a `status` field (`APPROVED`, `SUSPICIOUS`, `REJECTED`). Suspicious transactions are never discarded — the history is needed for future pattern-based rules (e.g. velocity checks) and for audit purposes.
- **SNS/email is only triggered when `status = SUSPICIOUS`**, and only fires after the DynamoDB write succeeds.
- **Idempotency and message ordering (SQS FIFO vs standard, DLQ, dedup key) are the Lambda repo's concern**, but the API must publish a stable, unique `transactionId` with every message so the Lambda can implement them.
- **API response contract**: since processing is asynchronous, `/transaction` cannot return an approve/deny verdict synchronously. It returns `202 Accepted` with the generated `transactionId`. Whether the client polls a `GET /transactions/{id}` endpoint, or another mechanism is used to retrieve the final status, is still open — check for updates to this section before assuming.

## Code rules and skills

Before writing or reviewing code, consult:
- `.agents/rules/RULES.md` — general Clean Code principles
- `.agents/rules/java-rules.md` — Java-specific conventions
- `.agents/rules/java-test-rules.md` — unit testing conventions
- `.agents/skills/` — task-specific how-to guides (step-by-step for recurring tasks, e.g. adding a new CRUD endpoint or a new migration)

## Essential commands

```bash
# full build
mvn clean install

# run locally (dev profile — DynamoDB Local / LocalStack, see application-dev.yml)
mvn spring-boot:run

# run tests
mvn test

# run a specific test
mvn test -Dtest=ClassNameTest

# compile only, no tests
mvn compile
```

Swagger UI is available at `http://localhost:8080/docs` while the app is running.

## Package structure (MVC)

```
src/main/java/com/frauddetector/project/
├── config/         # Configuration beans (CORS, OpenAPI, AWS SDK clients — DynamoDB/SQS, Security, etc.)
├── controller/      # REST controllers — orchestration only, no business logic
├── service/         # Service interfaces (contracts)
│   └── impl/         # Implementations
├── repository/       # DynamoDB access (AWS SDK v2 Enhanced Client), one class per table/entity
├── model/            # DynamoDB item classes (@DynamoDbBean)
├── dto/              # Input/output records (Request/Response)
├── mapper/           # MapStruct interfaces (Model <-> DTO)
├── messaging/         # SQS producer(s) — builds and sends transaction events
├── exception/         # Custom exceptions + GlobalExceptionHandler
└── security/          # Authentication/authorization (when applicable)
```

## Project conventions

- **Controllers** never access `Repository` or SQS clients directly — always go through `Service`.
- **DTOs are `record`**, never reuse the DynamoDB item class (`@DynamoDbBean`) as a request/response body.
- **Mapping**: all model↔DTO conversion happens via a MapStruct interface in `mapper/`, no manual mapping in Service.
- **Validation** of input happens on request DTOs via Bean Validation (`@NotBlank`, `@Min`, etc.), never inside the Service.
- **Business exceptions** must extend or be handled by `GlobalExceptionHandler` in `exception/`, always returning a standardized `ApiError`.
- **This API does not implement fraud rules.** `/transaction` validates and publishes to SQS only — do not add time-of-day checks, velocity checks, or any suspicious-activity logic here even if it seems convenient. That logic belongs exclusively in the Lambda repo.
- **DynamoDB table/key design**: document actual table names and partition/sort keys here once finalized (currently undecided — check with the user before assuming a schema).
- **Profiles**: `application.yml` holds common config; `application-dev.yml` and `application-prod.yml` override per environment (e.g. local DynamoDB via LocalStack/DynamoDB Local for `dev`, real AWS resources for `prod`). AWS credentials/region and SQS queue URLs come from environment variables, never hardcoded.

## Adding a new feature (e.g. a new endpoint/domain)

Recommended order to keep things consistent:
1. `model/EntityName.java` — `@DynamoDbBean` item class
2. `dto/EntityNameRequestDTO.java` and `dto/EntityNameResponseDTO.java`
3. `mapper/EntityNameMapper.java`
4. `repository/EntityNameRepository.java` — wraps `DynamoDbEnhancedClient` calls for this table
5. `service/EntityNameService.java` (interface) + `service/impl/EntityNameServiceImpl.java`
6. `controller/EntityNameController.java`
7. If the feature publishes an event (like `/transaction`), add the producer under `messaging/`
8. Corresponding tests under `src/test/java/.../{controller,service,repository}`

## Testing

- Service tests: mock `Repository` and `Mapper`, focus on request validation and orchestration (this API has no business/fraud rules to test — that's the Lambda repo's job).
- Controller tests: use `@WebMvcTest` + `MockMvc`, mock the `Service`.
- Repository tests: mock `DynamoDbEnhancedClient`/`DynamoDbTable`, or use DynamoDB Local/Testcontainers for lightweight integration coverage of key conditions and queries.
- Messaging tests: mock the SQS client, verify the correct message shape/attributes are sent — never hit real AWS in unit tests.
- Full context (`@SpringBootTest`) is not needed unless it's a genuine integration test.

## What NOT to do

- Don't put business logic in `Controller`.
- Don't expose a DynamoDB item class (`@DynamoDbBean`) directly on a REST endpoint (always go through a DTO).
- **Don't implement fraud-detection rules in this repo** — no time-of-day checks, velocity checks, blocklist checks, or anything that decides "is this suspicious". That logic belongs exclusively to the Lambda in the separate Python repo. This API's job ends at validating input and publishing to SQS.
- Don't have `/transaction` write directly to the transactions table — only the Lambda persists transaction records (with their final `status`), after analysis. The API only publishes to SQS.
- Don't commit AWS credentials, account IDs, or queue/topic ARNs in the `application-*.yml` files — use environment variables.

## Domain context (fraud-detector)

Types of fraud the Lambda (separate repo) currently evaluates or plans to evaluate — kept here for API context, since the API's DTOs and SQS payload must carry whatever fields the rules need:
- Time-of-day anomaly (e.g. transactions between 2:00–7:00 AM flagged as higher risk)
- Velocity/pattern checks based on the user's transaction history in DynamoDB (planned)

> Fill in further as the domain evolves: additional rule types, external integrations, response SLAs, etc.
