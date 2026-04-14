# Coverage Strategy

## Goals

- Prefer reliable coverage over inflated numbers.
- Keep test layers aligned with the actual maturity of each module.
- Avoid fake E2E tests for workflows that do not yet exist as stable runtime code.

## Test layers

### 1. Unit tests

Scope:
- service business rules
- policy helpers
- security helpers
- storage adapters
- transformation and normalization logic

Examples:
- JWT generation and parsing
- attendance normalization edge cases
- explanation decision transitions
- dismissal finalization rules

### 2. Web / integration tests

Scope:
- controller contract
- validation
- HTTP status mapping
- serialization
- RBAC and basic access denial behavior

Recommended style:
- `@WebMvcTest` for controllers
- `MockMvc`
- mocked services
- minimal request factories for DTO payloads

### 3. Full integration tests

Scope:
- selected high-value module flows with Spring context + H2/Flyway
- repository + service + controller interaction

Recommended candidates:
- auth login / refresh
- business trip report submission
- dismissal finalization
- attendance daily processing

### 4. E2E tests

Scope:
- only business-critical, stable, end-to-end workflows
- should run after modules are live and contracts are settled

Target critical flows:
- hire employee
- leave request
- lateness -> explanation -> decision
- business trip -> report
- dismissal -> clearance -> archive

## Coverage reporting

JaCoCo is enabled in Maven:

```bash
cd /Users/user/Desktop/MyProjects/HR/backend
mvn test
```

Expected report path:

```text
/Users/user/Desktop/MyProjects/HR/backend/target/site/jacoco/index.html
```

## Coverage policy

Recommended policy for the current stage:

- core auth/security/services: target `80%+` line coverage
- active HR modules with live endpoints: target `70%+`
- controllers: target contract coverage for every public route
- unfinished modules: track in the matrix, do not force artificial coverage

## What not to do

- Do not write brittle tests against private implementation details.
- Do not fake external systems with unrealistic responses that hide integration gaps.
- Do not count unimplemented modules as covered.
