# Design: Harden Pedro Modules for Safe Sharing

## Technical Approach

Harden paths without changing `/api/v1` DTOs or ownership. Oracle decides concurrency; services remain transaction boundaries; opt-in Maven supplies Oracle evidence. `Inscripcion` remains Activities-owned current/latest state; `Asistencia` stays out of scope.

## Architecture Decisions

| Decision | Choice | Alternative / tradeoff | Rationale |
|---|---|---|---|
| Credentials | Require environment placeholders across Spring, Compose, and provisioning; Compose requires `ORACLE_PASSWORD` with no committed default, while provisioning uses `ACCEPT ... HIDE`, validates before DDL, and stops on SQL errors | Defaults commit secrets; arguments expose passwords | Secrets stay out of files, arguments, persisted configuration, and output |
| API failures | Ordered fixed-message handlers; log causes with MDC correlation ID | Provider messages leak internals | Stable `ApiError`; private diagnostics |
| Team identity | Trim in-domain; retain prechecks; use `ConflictException` and `saveAndFlush` | Prechecks race; locks over-serialize | Oracle decides races; flush exposes HTTP 409 |
| Oracle uniqueness | Virtual `NAME_KEY = UPPER(TRIM(NAME))`, named uniqueness; upgrade reports collisions before DDL | Function index hides the key; cleanup destroys data | Inspectable key needs no entity field |
| Oracle verification | Opt-in `oracle-it` uses connections, barriers, timeouts, and cleanup | H2 cannot prove Oracle semantics | Unit/Modulith tests stay isolated |

## Data Flow

`HTTP -> DTO/controller -> @Transactional service -> repository -> Oracle constraint/lock -> exception advice -> correlated ApiError`

Activity overlap retains its `PLACE_KEY` lock. Enrollment retains the Activity `PESSIMISTIC_WRITE` lock and unique `(ACTIVITY_ID, PERSON_ID)`. Repositories stay module-local; person integration stays scalar `id_persona`/`PERSON_ID`.

## File Changes

| File(s) | Action | Description |
|---|---|---|
| `saludablemente/src/main/resources/application-dev.yaml` | Modify | Required Oracle environment placeholders |
| `saludablemente/database/docker/compose-dev.yml` | Modify | Required external `ORACLE_PASSWORD` with no committed default or secret |
| `saludablemente/database/oracle/01_provision_users.sql`, `04_provision_activities_user.sql` | Modify | Hidden input and fail-fast preflight |
| `saludablemente/database/oracle/02_create_teams.sql` | Modify | Canonical virtual key and fresh-install uniqueness |
| `saludablemente/database/oracle/09_migrate_team_name_key.sql` | Create | Abort-first collision migration |
| `saludablemente/src/main/java/pe/edu/upeu/saludablemente/exception/GlobalExceptionHandler.java` | Modify | Sanitized correlated translation |
| `saludablemente/src/main/java/pe/edu/upeu/saludablemente/teams/team/entity/Team.java`, `teams/team/service/TeamService.java` | Modify | Canonical trim, conflicts, forced flush |
| `saludablemente/src/test/java/pe/edu/upeu/saludablemente/exception/GlobalExceptionHandlerTest.java` | Create | Envelope/non-leakage contracts |
| `saludablemente/src/test/resources/application-oracle-it.yaml`, `saludablemente/src/test/java/pe/edu/upeu/saludablemente/oracle/PedroOracleIT.java` | Create | Opt-in configuration and scenarios |
| `saludablemente/pom.xml` | Modify | Isolated `oracle-it` Failsafe profile |
| `saludablemente/README.md`, `docs/next-iteration.md`, `docs/pedro-module-roadmap.md` | Create/Modify | Verified runtime, provisioning, ownership, test, and rollback guidance |

## Interfaces / Contracts

Keep `ApiError(timestamp,status,error,message,path,traceId,violations)`: 400 for malformed JSON/conversion, 404 for missing resources, and 409 for business, integrity, and optimistic-lock conflicts. Never publish infrastructure exception text.

## Runtime and Provisioning Verification

`application-dev.yaml` uses required `${ORACLE_JDBC_URL}`, `${ORACLE_APP_USERNAME}`, and `${ORACLE_APP_PASSWORD}` placeholders without defaults. A parameterized context test omits each independently and asserts startup fails naming only that key, before opening a connection; values never enter logs or assertions.

`saludablemente/database/docker/compose-dev.yml` requires operator-supplied `ORACLE_PASSWORD` without a committed default. RED invokes Compose without it: failure names only `ORACLE_PASSWORD`, starts no container, and neither prints nor writes secrets. GREEN supplies the operator environment value and starts Oracle without repository persistence. Operational guidance documents injection without example secrets.

SQL*Plus scripts enable `WHENEVER SQLERROR EXIT`, validate inputs/schema before DDL, and document fresh order `01` through `08` (`09` is upgrade-only). Postflight verifies owners, `SAL_TEAMS.TEAMS`, Activities/lock/enrollment tables, and application-user grants; output records status, never credentials.

## Testing Strategy

| Layer | Evidence |
|---|---|
| Unit/MVC | Team trim/conflict; exception mappings; absence of SQL, schema, credential, parser, class, and stack-trace fragments |
| Oracle integration | Rollback leaves zero requested enrollments and unchanged Activity. Activity, Enrollment, and Team races each prove one success, one 409, and the required single valid result |
| Isolation | Per-run prefix, barriers, bounded futures/timeouts, and FK-order cleanup. Evidence records scenario, prefix, elapsed time, cleanup/final-zero counts; target logging exposes only host/service |

## Threat Matrix

| Boundary | Applicability | Design response / RED tests |
|---|---|---|
| Documentation-like paths | N/A — no path classifier or automatic executor | SQL scripts are operator-selected; no matrix RED test |
| Git repository selection | N/A — no Git command is introduced | None |
| Commit state | N/A — no commit/index automation | None |
| Push state | N/A — no automation | None |
| PR commands | N/A — no automation | None |

## Migration / Rollout

Back up `SAL_TEAMS.TEAMS`; run the upgrade with server output. Collisions stop before DDL. After success, deploy and run the Oracle profile. Database rollback drops the constraint then virtual column, weakening the invariant. Cleanup never targets unprefixed rows.

Runtime rollback reverts `saludablemente/database/docker/compose-dev.yml`, `saludablemente/src/main/resources/application-dev.yaml`, both provisioning scripts, secure-runtime tests, and related operations docs together, without touching Team, API-error, or Oracle-harness units.

Maven Wrapper repair is deferred: documentation uses verified system `mvn` commands unless a later bounded work unit authorizes repair.

## Open Questions

None.
