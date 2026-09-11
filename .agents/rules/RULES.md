# RULES.md — General Code Best Practices

Clean Code rules applicable to any language in this project. Java-specific rules live in `java-rules.md`; testing rules live in `java-test-rules.md`.

## Language

- **English is the project's primary language.** All code, comments, commit messages, documentation, variable/method/class names, log messages, and exception messages must be written in English.
- Do not mix languages in the same file. If existing legacy code has non-English content, prioritize translating it when touching that file, but don't do unrelated mass renames outside the scope of the current task.

## Naming

- Names must reveal intent: `daysSinceLastPurchase` instead of `d`.
- Avoid obscure abbreviations. `qty` is acceptable if it's a team convention; unclear ones are not.
- Classes/types: nouns (`OrderService`, not `ProcessOrder`).
- Methods/functions: verbs (`calculateTotal()`, not `totalCalculation()`).
- Booleans: `is`/`has`/`should` prefix (`isValid`, `hasPermission`).
- Avoid magic numbers and loose strings — extract them into named constants.

## Functions and methods

- A function does **one thing**. If the name needs an "and" (`saveAndSendEmail`), it's probably two functions.
- Prefer few parameters (ideally ≤3). Too many parameters → group them into an object/DTO.
- Avoid boolean flow-control parameters (`process(true)`) — split into two clearly named methods instead.
- No hidden side effects: the method name should describe everything it does.
- Prefer early returns (guard clauses) over deeply nested `if/else`.

## Comments

- Code should explain itself through names and structure; a comment is not an excuse for confusing code.
- Useful comments explain *why* (a non-obvious decision), not *what* (the code already says what).
- Remove commented-out code — that's what Git history is for, not the file.
- Avoid redundant comments (`// increment i` above `i++`).

## Structure and organization

- One responsibility per class (Single Responsibility Principle).
- Avoid "do-everything" classes/methods (God Object/God Method).
- Duplication is the main enemy — extract repeated logic (DRY rule), but don't force premature abstraction for 2 occurrences; from the 3rd occurrence on, consider extracting.
- Keep a consistent abstraction level within a method (don't mix high-level business logic with low-level details in the same function).

## Error handling

- Never catch an exception generically and silently ignore it (`catch (Exception e) {}`).
- Prefer specific exceptions over magic return codes (`-1`, `null` for "not found").
- Fail fast: validate preconditions at the start of the function.
- Error messages should be actionable — say what went wrong and, when possible, how to fix it.

## Formatting and readability

- Consistent indentation and spacing (let the IDE/linter's auto-formatter decide, don't argue style in code review).
- Short files and methods are easier to understand — if a method exceeds ~30-40 lines, question whether it should be split.
- Order code by importance/usage level: public methods before private helper methods, when it makes sense.

## Design

- Prefer composition over inheritance when the relationship isn't clearly "is-a".
- Depend on abstractions (interfaces), not concrete implementations, when it brings real flexibility value — don't abstract preemptively without a concrete need (YAGNI).
- Immutability by default: prefer immutable objects over mutable ones when possible.
- Avoid global state and mutable static variables.

## Version control

- Small, atomic commits with messages describing *why*, not just *what*.
- Never commit secrets, credentials, or local environment config files.
- Feature branch per task; avoid direct commits to the main branch.

## Code review (also applicable to agent-performed review)

- Before considering a task done, re-read the code asking: "is this as simple as it could be?"
- Question all duplication, every generic name (`data`, `info`, `temp`, `helper`), every function with more than one responsibility.
- Prefer readable solutions over "clever" ones — premature performance optimization at the cost of readability only when there's real evidence of need.

## Related files

- `agents/rules/java-rules.md` — Java-specific conventions
- `agents/rules/java-test-rules.md` — unit testing conventions
- `agents/skills/` — task-specific how-to guides (e.g., how to add a new CRUD endpoint, how to add a migration)
