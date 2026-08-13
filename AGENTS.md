# AGENTS.md

Context guide for AI agents (opencode, Claude Code, etc.) working in this repository.

## Language

**English is the project's primary language.** All code, comments, commit messages, and documentation must be written in English. See `agents/rules/RULES.md` for full details.

## Overview

Project: **fraud-detector**
Stack: Java 21, Spring Boot 3.3.4, Maven, MVC architecture.
Database: H2 in-memory (`dev` profile), PostgreSQL (`prod` profile), migrations via Flyway.

## Code rules and skills

Before writing or reviewing code, consult:
- `agents/rules/RULES.md` — general Clean Code principles
- `agents/rules/java-rules.md` — Java-specific conventions
- `agents/rules/java-test-rules.md` — unit testing conventions
- `agents/skills/` — task-specific how-to guides (step-by-step for recurring tasks, e.g. adding a new CRUD endpoint or a new migration)

## Essential commands

```bash
# full build
mvn clean install

# run locally (dev profile, in-memory H2)
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
├── config/         # Configuration beans (CORS, OpenAPI, Security, etc.)
├── controller/      # REST controllers — orchestration only, no business logic
├── service/         # Service interfaces (contracts)
│   └── impl/         # Implementations
├── repository/       # Spring Data JPA interfaces
├── model/            # JPA entities
├── dto/              # Input/output records (Request/Response)
├── mapper/           # MapStruct interfaces (Entity <-> DTO)
├── exception/         # Custom exceptions + GlobalExceptionHandler
└── security/          # Authentication/authorization (when applicable)
```

## Project conventions

- **Controllers** never access `Repository` directly — always go through `Service`.
- **DTOs are `record`**, never reuse the JPA entity as a request/response body.
- **Mapping**: all Entity↔DTO conversion happens via a MapStruct interface in `mapper/`, no manual mapping in Service.
- **Validation** of input happens on request DTOs via Bean Validation (`@NotBlank`, `@Min`, etc.), never inside the Service.
- **Business exceptions** must extend or be handled by `GlobalExceptionHandler` in `exception/`, always returning a standardized `ApiError`.
- **Migrations**: any schema change goes in as a new file under `src/main/resources/db/migration`, following the `V{n}__description.sql` pattern. Never edit a migration that's already been committed.
- **Profiles**: `application.yml` holds common config; `application-dev.yml` and `application-prod.yml` override per environment. Production secrets come from environment variables (`DB_URL`, `DB_USER`, `DB_PASSWORD`), never hardcoded.

## Adding a new feature (e.g. a new "Transaction" domain)

Recommended order to keep things consistent:
1. `model/Transaction.java` — JPA entity
2. `db/migration/V{n}__create_transactions_table.sql` — corresponding migration
3. `dto/TransactionRequestDTO.java` and `dto/TransactionResponseDTO.java`
4. `mapper/TransactionMapper.java`
5. `repository/TransactionRepository.java`
6. `service/TransactionService.java` (interface) + `service/impl/TransactionServiceImpl.java`
7. `controller/TransactionController.java`
8. Corresponding tests under `src/test/java/.../{controller,service,repository}`

## Testing

- Service tests: mock `Repository` and `Mapper`, focus on business rules.
- Controller tests: use `@WebMvcTest` + `MockMvc`, mock the `Service`.
- Repository tests: use `@DataJpaTest` with H2.
- Full context (`@SpringBootTest`) is not needed unless it's a genuine integration test.

## What NOT to do

- Don't put business logic in `Controller`.
- Don't expose a JPA entity directly on a REST endpoint (always go through a DTO).
- Don't change `ddl-auto` to `update`/`create` in the `prod` profile — schema is controlled only via Flyway.
- Don't commit credentials in the `application-*.yml` files.

## Domain context (fraud-detector)

> Fill in as the domain evolves: which types of fraud the system detects, which external integrations (e.g. credit bureaus, payment gateways), critical business rules, response SLAs, etc. This helps the agent make design decisions that are better aligned with the system's real purpose.
