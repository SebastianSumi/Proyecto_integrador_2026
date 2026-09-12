# Tasks: Harden Pedro Modules

## Review Workload Forecast

Delivery strategy: ask-on-risk
Decision needed before apply: Yes
Chained PRs recommended: Yes
Chain strategy: pending
400-line budget risk: High

### Suggested Work Units

| Unit | Goal | Likely PR | Focused test command | Runtime harness | Rollback boundary |
|---|---|---|---|---|---|
| 1 | Secure Oracle runtime | PR1 | `mvn -f saludablemente/pom.xml -Dtest=SecureOracleRuntimeTest test` | Compose missing `ORACLE_PASSWORD`; SQL*Plus missing `ORACLE_JDBC_URL`, `ORACLE_APP_USERNAME`, `ORACLE_APP_PASSWORD`; then 01–08, postflight | Revert Compose, `application-dev.yaml`, provisioning scripts/tests, and runtime docs together |
| 2 | Sanitized API errors | PR2 | `mvn -f saludablemente/pom.xml -Dtest=GlobalExceptionHandlerTest test` | N/A: MVC contract only | Revert handler and its tests |
| 3 | Team identity migration | PR3 | `mvn -f saludablemente/pom.xml -Dtest=TeamServiceTest test` | SQL*Plus collision/upgrade; drop constraint before `NAME_KEY` | Revert Team code/tests and `02_create_teams.sql`/`09_migrate_team_name_key.sql`; reverse Oracle key in order |
| 4 | Oracle proof and operations | PR4 | `mvn -f saludablemente/pom.xml -P oracle-it -Dit.test=PedroOracleIT verify` | Four bounded races, rollback, cleanup, and documented commands | Revert `pom.xml`, Oracle IT files, and affected docs |

## Phase 1: Secure Runtime

- [x] 1.1 RED: Test missing `ORACLE_JDBC_URL`, `ORACLE_APP_USERNAME`, `ORACLE_APP_PASSWORD`, and Compose `ORACLE_PASSWORD`; fail before connection/container start, naming only the missing key without output.
- [x] 1.2 RED: Execute SQL*Plus missing-input cases in `saludablemente/database/oracle/01_provision_users.sql` and `04_provision_activities_user.sql`; assert pre-DDL abort and name-only output.
- [x] 1.3 GREEN: Require placeholders in `saludablemente/src/main/resources/application-dev.yaml`; require external `ORACLE_PASSWORD` in `saludablemente/database/docker/compose-dev.yml`; harden scripts with hidden input, preflight, `SQLERROR EXIT`.
- [x] 1.4 REFACTOR: Verified the catalog-backed 01–08 state, postflight owners/grants, and credential suppression; documented safe injection and rollback without credential values.

## Phase 2: Sanitized Errors

- [ ] 2.1 RED: In `saludablemente/src/test/java/pe/edu/upeu/saludablemente/exception/GlobalExceptionHandlerTest.java`, cover integrity, optimistic-lock, malformed JSON, conversion, validation, missing-resource, and conflict cases; forbid SQL/schema/credential/parser/class/stack-trace leakage.
- [ ] 2.2 GREEN: Add fixed-message mappings and private-cause correlation in `saludablemente/src/main/java/pe/edu/upeu/saludablemente/exception/GlobalExceptionHandler.java`; preserve `ApiError` fields and status semantics.
- [ ] 2.3 REFACTOR: Confirm messages, MDC trace correlation, and no infrastructure text in responses.

## Phase 3: Team Identity

- [ ] 3.1 RED: Test trimmed case-insensitive names and concurrent create in `saludablemente/src/test/java/pe/edu/upeu/saludablemente/teams/team/service/TeamServiceTest.java`; require one success, one 409, one canonical row.
- [ ] 3.2 GREEN: Canonicalize names and flush conflicts in `saludablemente/src/main/java/pe/edu/upeu/saludablemente/teams/team/entity/Team.java` and `saludablemente/src/main/java/pe/edu/upeu/saludablemente/teams/team/service/TeamService.java`.
- [ ] 3.3 GREEN: Add virtual `NAME_KEY` and named uniqueness to `saludablemente/database/oracle/02_create_teams.sql`; add collision-reporting abort-first `09_migrate_team_name_key.sql`.
- [ ] 3.4 REFACTOR: Verify migration preserves data and records collisions; reverse by dropping constraint before virtual column.

## Phase 4: Oracle Proof and Guide

- [ ] 4.1 RED: Add rollback, Activity-place, Enrollment, and Team race assertions to `saludablemente/src/test/java/pe/edu/upeu/saludablemente/oracle/PedroOracleIT.java`; require success/409/cardinality outcomes.
- [ ] 4.2 GREEN: Add `saludablemente/src/test/resources/application-oracle-it.yaml` and isolated `oracle-it` Failsafe profile in `saludablemente/pom.xml`, with prefixes, barriers, bounded futures/timeouts, and FK-order cleanup.
- [ ] 4.3 REFACTOR: Record target/prefix/elapsed/cleanup/final-zero evidence; expose only host/service and keep unit tests isolated.
- [ ] 4.4 GREEN: Update `README.md`, `saludablemente/docs/next-iteration.md`, and `saludablemente/docs/pedro-module-roadmap.md` with only verified Java/Maven/Oracle commands, ownership, rollback, and wrapper deferral.
- [ ] 4.5 REFACTOR: Re-read docs against runtime evidence; label pending Oracle verification and external contracts without claiming OpenSpec-only behavior.

Threat matrix: five boundaries N/A; no automatic RED tests.
