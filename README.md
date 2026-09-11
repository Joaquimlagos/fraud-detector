# fraud-detector

Event-driven fraud detection system. This repository contains the **fraud-detector-api** — a Java 21 / Spring Boot REST API that receives transactions, validates their shape, publishes them to SQS for asynchronous analysis, and queries the fraud-analysis Lambda synchronously for detailed results. All fraud-detection logic lives in a **separate repository** (Python, AWS Lambda).

## Architecture

```
[this repo]                          [separate Python repo]
API (Spring Boot)                    Lambda (fraud analysis engine)
   │
   ├─ create-user / delete-user ──▶ DynamoDB (users)
   │
   ├─ POST /transactions ──publish──▶ SQS ──consumes──▶ Lambda — Flow 1
   │                                                        general validation + scoring (rule-based)
   │
   └─ GET /transactions/{id}/analysis ──sync invoke──▶ Lambda — Flow 2
                                   ◀─ analysis JSON ──    RAG + LLM detailed analysis
                                                          · status (APPROVED, REJECTED, IN_ANALYSIS, ...)
                                                          · reasoning (human-readable explanation)
                                                          · similar cases from history (similarity scores)
```

The Lambda (separate repo) has two analysis flows:

1. **General validation + scoring (async)** — triggered by the SQS event published from `POST /transactions`. Validates the payload objects and produces a risk score for the transaction.
2. **Detailed analysis (synchronous RAG + LLM)** — invoked directly by the API from `GET /transactions/{id}/analysis` (AWS SDK `InvocationType.REQUEST_RESPONSE`). The Lambda applies a RAG pipeline over the user's transaction history, calls a large language model, and returns a status (`approved`, `rejected`, `in_analysis`, etc.) with a reasoning message explaining *why* and similar historical cases.

## Endpoints

| Method | Path | Description |
|---|---|---|
| POST | `/api/v1/users` | Creates a user |
| GET | `/api/v1/users/history/{userId}` | Returns the user profile and their transaction history |
| DELETE | `/api/v1/users/{userId}` | Deletes a user |
| POST | `/api/v1/transactions` | Submits a transaction for asynchronous analysis — returns `202 Accepted` with a generated `transactionId` |
| GET | `/api/v1/transactions/{transactionId}/analysis` | Invokes the Lambda and returns the detailed analysis (status, reasoning, similar cases, cached flag) |

## Tech stack

- Java 21, Spring Boot 3.3.4, Maven, MVC architecture
- AWS SDK v2 — DynamoDB (Enhanced Client), SQS, Lambda
- DynamoDB as the only data store (no relational database, no JPA/Hibernate)
- Swagger UI via springdoc-openapi (`/docs`)

## Running locally

This repository is the **API only**. The system is split across three repositories, and the API depends on the others at runtime:

| Repository | Role |
|---|---|
| `fraud-detector-api` (this one) | Spring Boot API — validates and publishes transactions to SQS, queries the Lambda for detailed analysis |
| `fraud-detector-infra-aws` | AWS infrastructure (SQS queues, DynamoDB tables, SNS topics, Lambda deployment, etc.) |
| `fraud-detector-lambda` | Python Lambda — fraud analysis engine (Flow 1: validation + scoring; Flow 2: RAG + LLM detailed analysis) |

> **Before running this API** you need the other two up and running (at least for the local/dev flow): the **infrastructure** (LocalStack or real AWS resources, provisioned by `fraud-detector-infra-aws`) and the **Lambda** (`fraud-detector-lambda`). Follow each repository's README for its own setup and run instructions. This API only expects the SQS queue, the DynamoDB tables, and the Lambda function to be reachable (see `application-dev.yml` for the local endpoints/names).

```bash
# full build (includes tests)
mvn clean install

# run locally (dev profile — LocalStack, see application-dev.yml)
mvn spring-boot:run

# run tests only
mvn test
```

The dev profile targets local AWS services on `http://localhost:4566` (LocalStack). Make sure the infrastructure from `fraud-detector-infra-aws` is provisioned and the Lambda from `fraud-detector-lambda` is deployed before exercising SQS-triggered flows.

Swagger UI is available at `http://localhost:8080/docs` while the app is running.

## Configuration

Common settings live in `application.yml`; `application-dev.yml` and `application-prod.yml` override per environment. All sensitive values come from environment variables, never hardcoded:

| Variable | Description | Default |
|---|---|---|
| `AWS_REGION` | AWS region | `us-east-1` |
| `AWS_ACCESS_KEY_ID` / `AWS_SECRET_ACCESS_KEY` | AWS credentials | — |
| `AWS_SQS_ENDPOINT` | SQS endpoint (LocalStack in dev) | — |
| `AWS_SQS_QUEUE_NAME` | Queue that receives transaction events | — |
| `AWS_DYNAMODB_ENDPOINT` | DynamoDB endpoint (LocalStack in dev) | — |
| `AWS_DYNAMODB_USERS_TABLE` | Users table | `users` |
| `AWS_DYNAMODB_TRANSACTIONS_TABLE` | Transactions table | `transactions` |
| `AWS_LAMBDA_ENDPOINT` | Lambda endpoint (LocalStack in dev) | — |
| `AWS_LAMBDA_FUNCTION_NAME` | Analysis Lambda invoked by `GET /transactions/{id}/analysis` | `fraud-detector-analysis-dev` |

## License

See [LICENSE](LICENSE).