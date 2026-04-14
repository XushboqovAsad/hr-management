# Mocks And Factories

## Request factories

Shared request factory:

- `/Users/user/Desktop/MyProjects/HR/backend/src/test/java/uz/hrms/TestRequestFactory.java`

Use it for DTO/request payload creation instead of building long request objects inline in every test.

## External integrations to mock

### Storage / document serving

Mock:
- `ProtectedFileAccessService`

Why:
- controller tests should validate routing and authorization, not actual file IO

### Payroll handoff

Mock:
- payroll export / sync adapters when they become explicit beans

Why:
- HR tests should verify basis generation, not payroll engine behavior

### SCUD input

Mock or simulate:
- ingest requests
- raw event batches

Why:
- normalization logic should be tested with deterministic timestamps and badge/device data

### Notifications

Mock:
- downstream email / telegram dispatchers if they are introduced

Why:
- service tests should assert notification intent, not network delivery

## Factory guidance

Use factories for:
- requests
- DTO fixtures
- common timestamps
- common UUIDs when deterministic comparisons help readability

Avoid factories for:
- hidden mutable entity graphs that mirror production internals too closely
- large object mother classes that make tests harder to read

## Future additions

Recommended next utility files:
- `TestResponseFactory.java`
- `IntegrationTestDataLoader.java`
- `SecurityTestUsers.java`
