# java-test-rules.md — Unit Testing Best Practices

Complements `RULES.md` and `java-rules.md`. Applies to JUnit 5 + Mockito tests in this project. All code, comments, and identifiers must be written in English (see `RULES.md` → Language).

## Test structure (AAA / Given-When-Then)

Every test should follow three clear blocks, even without explicit comments:

```java
@Test
void shouldThrowExceptionWhenItemDoesNotExist() {
    // given
    when(itemRepository.findById(1L)).thenReturn(Optional.empty());

    // when / then
    assertThrows(ResourceNotFoundException.class, () -> itemService.findById(1L));
}
```

- **given**: sets up the scenario (mocks, input data).
- **when**: executes the action under test.
- **then**: validates the result.
- Never mix multiple unrelated "when" actions in the same test.

## Test naming

- Method names describe expected behavior, not implementation: `shouldReturnItemWhenIdExists()`, not `testFindById1()`.
- Recommended pattern: `should<ExpectedResult>When<Condition>()`.
- Test class: `ClassNameTest` (e.g. `ItemServiceImplTest`), following Maven/Surefire's naming convention for auto-discovery.

## Isolation

- A unit test must **not** depend on a real database, network, or filesystem — mock (Mockito) all external dependencies.
- Each test must be independent: it should not depend on execution order or on state left by another test.
- Use `@BeforeEach` to reset shared state, avoid mutable shared state across tests in the same class.

## Case coverage

For each tested method, cover at least:
1. **Happy path** — expected behavior with valid input.
2. **Edge cases** — empty collection, zero value, empty string, numeric boundaries.
3. **Error cases** — invalid input, resource not found, expected exception.
- Don't test only the happy path "because it passed" — tests exist to catch regressions, not to hit a coverage metric.

## Assertions

- Use specific assertions (`assertEquals`, `assertThrows`, AssertJ's `assertThat`) instead of `assertTrue(a.equals(b))`.
- Prefer **AssertJ** (`assertThat(result).isEqualTo(...)`) for readability in complex object/collection assertions, if already available in the project.
- A test should validate one specific behavior — multiple `assertEquals` in the same test are acceptable only when they validate facets of the same result (e.g. several fields of the same returned object).
- Avoid vague assertions (`assertNotNull(result)` alone) when it's possible to validate the actual expected value.

## Mocks (Mockito)

- Only mock direct dependencies of the unit under test (e.g. `Repository`, `Mapper` when testing a `Service`) — don't mock the class under test itself.
- Use `verify()` to confirm important interactions (e.g. that `repository.save()` was called), but don't over-verify every trivial call — verify what matters to the method's contract.
- Avoid excessive/generic `@Mock` usage: if a test needs to mock 6+ dependencies, it's a sign the class under test has too much responsibility (revisit the design, not just the test).
- Never use `Mockito.mock()` for simple value classes (DTOs, records) — just instantiate them directly.

## Tests by layer

- **Service**: `@ExtendWith(MockitoExtension.class)`, mocks `Repository` and `Mapper`, focuses on business rules and error handling.
- **Controller**: `@WebMvcTest(ControllerName.class)` + `MockMvc`, mocks the `Service` via `@MockBean`, validates HTTP status, response body, and input validation (`@Valid`).
- **Repository**: `@DataJpaTest` with H2, validates custom queries and real persistence behavior — nothing is mocked here, it's a lightweight integration test.
- Full integration tests (`@SpringBootTest`) are the exception, not the rule — use them only when the tested behavior truly depends on the full Spring context being up.

## Test data

- Use test builders/factories (e.g. `ItemTestBuilder` or `Item.builder()...build()`) to create test objects, avoid duplicating verbose construction in every test method.
- Test data should be obvious and minimal — don't reuse a complex "production-like" dataset when 2-3 fields already prove the behavior.
- Avoid unexplained magic numbers/strings in tests (`assertEquals(42, result)`) — name constants when the value isn't self-explanatory.

## What not to do

- Don't test third-party/framework code (e.g. don't test whether Bean Validation's `@NotBlank` works — that's Spring's responsibility, already tested by them).
- Don't write a test that exists only to "hit coverage" without validating real behavior.
- Don't leave commented-out or `@Disabled` tests without documented justification and a plan/ticket to resolve it.
- Don't use `Thread.sleep()` to synchronize async tests — use proper mechanisms (`Awaitility`, `CompletableFuture.get()` with a timeout).
