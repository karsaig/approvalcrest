<img referrerpolicy="no-referrer-when-downgrade" src="https://static.scarf.sh/a.png?x-pxid=8fd3772b-2ddb-4042-9243-eae0fb17b66c" />

# dynamic-values

How to handle values that change between test runs.

## The Problem

UUIDs, auto-incremented IDs, timestamps, random values, and other generated data break approved files because they differ on every run.

## The Best Approach: Control at the Source

The most robust solution is to make the system deterministic rather than working around non-determinism in the assertion. This requires some infrastructure investment but pays back with thoroughness, stability, and a test suite that can run fully in parallel.

### Control Time

Inject a `Clock` (or equivalent abstraction) instead of calling `Instant.now()` directly. In tests, provide a fixed or controllable clock:

```java
// System under test uses an injected clock
class TokenService {
    private final Clock clock;

    TokenService(Clock clock) { this.clock = clock; }

    Token issueToken(String userId) {
        return new Token(userId, clock.instant().plus(Duration.ofHours(1)));
    }

    boolean isExpired(Token token) {
        return clock.instant().isAfter(token.getExpiry());
    }
}

// Test: advancing the clock exercises the expiry code path deterministically
@Test
void expiredTokenIsRejected() {
    MutableClock clock = new MutableClock(Instant.parse("2026-01-01T10:00:00Z"));
    TokenService service = new TokenService(clock);
    Token token = service.issueToken("user-1");

    clock.advance(Duration.ofHours(2));   // move past expiry

    assertThat(service.checkToken(token), sameJsonAsApproved());
}
```

Without clock control, testing the expiry code path means waiting an hour in real time or writing a fragile workaround. With it, it is one line — and the expiry logic is genuinely exercised. The same principle applies to any time-dependent behaviour: scheduled jobs, retry backoff, TTL-based cache eviction, rate limiting windows.

### Reset All State Before Every Test

Set every piece of stateful infrastructure to a known baseline before each test:

- **Database**: truncate tables, roll back transactions, or restore a fixed fixture snapshot
- **Caches**: clear or pre-warm to a defined state
- **Message queues**: drain or seed with known messages
- **ID generators**: seed or stub so generated IDs are predictable and appear as real values in approved files

Full state isolation makes each test independent and reproducible, and allows the entire suite to run **in parallel** safely — no shared mutable state means no race conditions between tests. It also unlocks code paths that would otherwise be hard to reach: the behaviour on the first request vs the nth, an empty cache vs a warm one, a retry after a transient failure.

### Why This Is the Best Option

With full source control:
- Every field in every approved file contains a real, deterministic value — nothing needs to be ignored, aliased, or custom-matched
- No logic escapes automated testing: time-based features, cache invalidation, queue retry behaviour, and scheduling all become ordinary test cases
- The approved files document the actual business output precisely, making them more valuable as living specifications

The three fallback strategies below are useful when you cannot (yet) control the source — for example when testing a legacy system, integrating with a third-party dependency, or working at a level where full infrastructure reset is not practical.

---

## Fallback Strategies

### 1. Ignore

When the value is irrelevant to correctness, exclude it from comparison entirely:

```java
assertThat(actual, sameJsonAsApproved()
    .ignoring("createdAt")
    .ignoring("id"));
```

The field does not appear in the approved file at all. Use this when the field genuinely does not matter to the test — not as a blanket way to silence noise. Overuse makes the approved file less precise and reduces the safety net.

### 2. Alias

When you want the approved file to document the field but not pin the exact value:

```java
assertThat(actual, sameJsonAsApproved()
    .withAlias("2024-05-01T12:00:00Z", "<createdAt>"));
```

The approved file contains `"createdAt": "<createdAt>"` — clearly readable and stable across runs. Use this when the field matters structurally but the exact value is volatile.

### 3. Custom Match

When you want to assert a structural property (non-null, matches a pattern, within a range):

```java
assertThat(actual, sameJsonAsApproved()
    .with("id",    notNullValue())
    .with("email", matchesPattern(".*@example\\.com")));
```

## Decision Guide

| Situation | Preferred approach |
|---|---|
| Can control the source | Fix at source — full determinism |
| Value is totally irrelevant | [`.ignoring()`](ignoring-fields.md) |
| Value is relevant; want a readable approved file | [`.withAlias()`](aliasing.md) |
| Value must satisfy a constraint | [`.with(path, matcher)`](custom-matching.md) |

**Note:** `.ignoring()`, `.withAlias()`, and `.with()` apply to `sameJsonAsApproved` and `sameBeanAs` only — they operate on JSON fields. For `sameContentAsApproved`, the fallback is to pre-process the string before asserting (e.g. replace volatile tokens with a stable placeholder using `String.replaceAll`), or better, control the source so the content is deterministic.

## Related

- [ignoring-fields](ignoring-fields.md)
- [aliasing](aliasing.md)
- [custom-matching](custom-matching.md)
