# Proposal: Harden Pedro Modules for Safe Sharing

## Intent

Harden Team, Activity, and Enrollment for sharing through credential removal, sanitized failures, concurrency proof, and truthful commands. Commits `f839d75`, `f89f007`, `e627de9`, and `4e67894` remain pre-SDD baseline work.

## Scope

### In Scope
- Move Oracle credentials to environment configuration and restore the excluded `database/oracle/04_provision_activities_user.sql` workflow.
- Map Oracle integrity, optimistic-lock, malformed JSON, and conversion failures to sanitized `ApiError` responses.
- Enforce trimmed, case-insensitive Team-name uniqueness for installs and upgrades. Collisions abort safely without renaming, deleting, or merging data.
- Add an opt-in Oracle harness for Enrollment rollback, Activity place concurrency, Enrollment concurrency, and Team uniqueness races, using barriers, timeouts, and cleanup.
- Align documentation and Maven commands with verified behavior.

### Out of Scope
- Goals, Personal/Auth, Attendance, Notifications, outbox, Team count/delete, full Enrollment history, cross-module persistence, PRs, and pushes.
- Maven Wrapper repair unless bounded; otherwise document system Maven and defer it.

## Capabilities

### New Capabilities
- `secure-oracle-runtime`: Credential-free provisioning and environment-driven Oracle execution.
- `sanitized-api-errors`: Stable, non-sensitive REST failure contracts.
- `normalized-team-identity`: Oracle-authoritative Team-name uniqueness and safe migration.
- `oracle-transaction-verification`: Deterministic rollback and concurrency proof.
- `pedro-operations-guide`: Consistent operational guidance.

### Modified Capabilities
None; no baseline OpenSpec capabilities exist.

## Approach

Keep security/provisioning and build tooling as separate root classes. Use verifiable work units: secure runtime, API errors, Team migration, Oracle harness, then documentation. The forecast exceeds 400 lines, so tasks prepare chained slices under `ask-on-risk`; chain strategy remains undecided.

## Affected Areas

| Area | Impact | Description |
|---|---|---|
| `saludablemente/database/oracle/` | Modified | Provisioning and migrations |
| `saludablemente/src/main/` | Modified | Configuration, errors, invariants |
| `saludablemente/src/test/` | Modified | Oracle-focused tests |
| `saludablemente/docs/`, `saludablemente/README.md`, `saludablemente/pom.xml` | Modified | Verified operations |

## Risks

| Risk | Likelihood | Mitigation |
|---|---|---|
| Team collisions block upgrade | Medium | Preflight diagnostics; fail safely |
| Concurrency tests leak or flake | Medium | Barriers, bounded waits, cleanup |
| Errors expose Oracle details | Medium | Central mapping and contract tests |

## Rollback Plan

Revert work units independently. After uniqueness migration, require an explicit reverse migration and retain collision evidence. Disabling the Oracle profile must not affect unit tests.

## Dependencies

- Java 21, Spring Boot 4.0.7, Maven/JAR, Oracle 23, and operator-supplied Oracle values.

## Success Criteria

- [ ] No committed credential remains; provisioning and harness commands use environment values.
- [ ] API failures are sanitized and consistent.
- [ ] Fresh and upgraded schemas enforce normalized Team uniqueness without destructive collision handling.
- [ ] Four Oracle scenarios pass deterministically with cleanup evidence.
- [ ] Documentation matches verified commands and states wrapper status.
