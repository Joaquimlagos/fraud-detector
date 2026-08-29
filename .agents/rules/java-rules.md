# java-rules.md — Java-Specific Best Practices

Complements `RULES.md`. Applies to all Java code in this project (Java 21). All code, comments, and identifiers must be written in English (see `RULES.md` → Language).

## Modern Java features (17-21)

- Use **records** for DTOs and immutable value objects (`ItemRequestDTO`, `ApiError`), never manual getter/setter classes for that purpose.
- Use **pattern matching for switch** and **switch expressions** instead of long `if/else if` chains, when the case fits.
- Use **text blocks** (`"""`) for multiline strings (SQL, sample JSON, long messages) instead of `+` concatenation.
- Prefer `var` for local types that are obvious from the right-hand side (`var list = new ArrayList<Item>()`), but avoid it when it reduces readability (`var result = service.process(x)` without knowing the type).
- Use the Streams API for collection transformations, but don't force a stream where a plain `for` loop is more readable — don't sacrifice clarity for "functional style".

## Null safety

- Avoid returning `null` from public methods — prefer `Optional<T>` for "may not exist", or throw an exception when absence is actually an error.
- Never pass `null` as a parameter unnecessarily — consider a method overload or `Optional` as the parameter type.
- Annotate parameters/returns with `@NonNull`/`@Nullable` when the team uses an annotation library (e.g. JSR-305, Jakarta Validation) to make the contract explicit.
- `Optional` is for return values, not for JPA entity fields or method parameters.

## Immutability and concurrency

- Class fields: `final` whenever possible.
- Publicly exposed collections: return immutable copies (`List.copyOf(...)`) or unmodifiable views, to avoid leaking an internal mutable reference.
- Be careful with shared state in Spring `@Service`/`@Component` beans (singletons by default) — don't store mutable per-request state in instance fields.

## Exceptions

- Create domain-specific custom exceptions (e.g. `ResourceNotFoundException`), avoid throwing generic `RuntimeException`.
- Business exceptions should extend `RuntimeException` (unchecked) in this project, to avoid polluting method signatures with chained `throws` — centralized handling lives in `GlobalExceptionHandler`.
- Never swallow a stack trace (`catch (Exception e) { }`) — at minimum, log it with enough context for debugging.

## Dependency injection (Spring)

- Always use **constructor injection** (`@RequiredArgsConstructor` from Lombok or an explicit constructor), never field-level `@Autowired`.
- Dependencies should be `private final`.
- A service class should depend on interfaces (`ItemService`), not concrete implementations, to allow test mocks and swap implementations without touching consumers.

## JPA entities

- Never expose a JPA entity directly in a REST response — always convert to a DTO via a mapper.
- `equals()`/`hashCode()` on a JPA entity: base them on a business identifier (e.g. UUID/business key) when one exists; avoid basing them on all fields or on the generated `id` before persisting (causes bugs in collections before save).
- Avoid `FetchType.EAGER` by default on relationships — prefer `LAZY` and load explicitly when needed, to avoid silent N+1 queries.
- Be careful with Lombok's `@Data` on entities with bidirectional relationships — it can generate recursive `toString()`/`equals()` and a `StackOverflowError`. Prefer explicit `@Getter`/`@Setter` or exclude relationship fields from `toString`.

## Lombok

- Use in moderation: `@Getter`, `@Builder`, `@RequiredArgsConstructor` are safe and recommended.
- Avoid `@Data` on JPA entities (see above). Prefer it on simple DTO/value classes without relationships.
- Don't hide business logic behind annotations — Lombok is for reducing boilerplate, not for replacing explicit code when custom behavior exists.

## Logging

- Use a structured logger (SLF4J via Lombok's `@Slf4j`), never `System.out.println` in production code.
- Don't log sensitive data (passwords, tokens, full personal data) — mask or omit it.
- Correct log level: `ERROR` for real failures, `WARN` for recoverable but abnormal situations, `INFO` for relevant business events, `DEBUG` for development-level detail.

## Performance and general best practices

- Avoid String concatenation in a loop (`+=`) — use `StringBuilder` when the loop volume is relevant.
- Prefer `List.of()`, `Map.of()` for small, fixed immutable collections.
- Close resources (`InputStream`, `Connection`, etc.) always with try-with-resources.
- Avoid business logic inside getters/setters — they should be simple accessors.
