# HRMS Test Matrix

This file is the honest coverage map for the current codebase.
It separates modules with live, testable backend code from modules that are still partial or design-only.

## Current automated coverage

| Module | Unit tests | Web/integration tests | Critical flow status |
| --- | --- | --- | --- |
| auth | `JwtServiceTest`, `AuthServiceAuditTest` | `AuthIntegrationTest` | Partial |
| permissions / rbac | existing auth service tests | pending dedicated controller test | Partial |
| employees | existing ESS tests cover self-service read paths | pending dedicated employee controller test | Partial |
| relatives | pending live module | pending | Not started |
| documents | protected file tests cover secure serving only | pending dedicated module tests | Partial |
| orders | pending live module | pending | Not started |
| leaves | pending live module | pending | Not started |
| business trips | `BusinessTripServiceTest` | `BusinessTripControllerTest` | Partial |
| sick leaves | pending live module | pending | Not started |
| attendance | `AttendanceServiceTest` | `AttendanceControllerTest` | Partial |
| scud normalization | `AttendanceServiceTest` | pending dedicated batch ingest controller test | Partial |
| explanations | `ExplanationServiceTest` | `ExplanationControllerTest` | Partial |
| disciplinary actions | covered through explanation service/controller flows | pending dedicated top-level module test | Partial |
| rewards | covered through explanation controller reward path | pending dedicated service test | Partial |
| dismissals | `DismissalServiceTest` | `DismissalControllerTest` | Partial |
| library | pending live module | pending | Not started |
| lms | `LmsServiceTest` | `LmsControllerTest` | Partial |
| reports | pending live module | pending | Not started |
| audit | `ProtectedFileAccessServiceTest`, `RateLimitingFilterTest`, `AuthServiceAuditTest` | pending dedicated audit controller test | Partial |

## Critical E2E flows

These flows should exist as executable end-to-end tests only after the corresponding live backend flows are complete.

| Flow | Status | Notes |
| --- | --- | --- |
| hire employee | Pending | Current repository does not yet expose a stable live hiring workflow end-to-end. |
| leave request | Pending | Leave module is not yet implemented as a live backend flow. |
| lateness -> explanation -> decision | Ready for service/web integration layering | Built from attendance + explanation modules. |
| business trip -> report | Ready for service/web integration layering | Built from business trip module. |
| dismissal -> clearance -> archive | Ready for service/web integration layering | Built from dismissal module. |

## Recommended next increments

1. Add `AuditController` / `SecurityAuditController` web tests.
2. Add dedicated RBAC controller tests for `/api/v1/admin/roles` and `/api/v1/admin/permissions`.
3. Add service-level cross-module tests for:
   - attendance incident -> explanation creation
   - business trip close -> payroll basis handoff
   - dismissal finalize -> account lock/archive
4. Add real executable E2E only after hire and leave modules become live.
