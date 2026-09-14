# Apply Progress: Add the Teams Repository

## Status

Completed. All eight tasks are complete; behavioral repository query testing remains deferred to a separately authorized isolated test-infrastructure change.

## Completed Tasks

- [x] 1.1 Created `TeamRepository` in the Teams-owned repository package. It extends `JpaRepository<Team, Long>` and declares only `List<Team> findAllByActive(boolean active)`.
- [x] 1.2 Confirmed the derived query uses the existing `Team.active` property and made no service, controller, entity, DTO, mapper, configuration, POM, SQL, migration, Oracle, or cross-module repository change.
- [x] 2.1 Updated `docs/README.md` with repository-layer status and deferred behavioral-test caveat.
- [x] 2.2 Updated `docs/rebuild/07-next-agent-handoff.md` for the repository handoff and no-Oracle/no-test-infrastructure guardrails.
- [x] 2.3 Updated `docs/rebuild/09-teams-package-structure.md` with the minimal repository contract without claiming runtime query coverage.
- [x] 3.1 Acquired the native SDD runtime attempt before validation.
- [x] 3.2 Completed validation and final-diff inspection.
- [x] 3.3 Settled the native attempt successfully.

## Work Unit Evidence

| Evidence | Result |
|---|---|
| Focused validation | `mvn test` exited 0; 11 tests passed. |
| Scope validation | `git diff --check` exited 0. |
| Runtime harness | N/A — a Spring Data behavioral repository test needs embedded database/test configuration changes, which are outside this change's authorized scope. Mock or reflection substitutes were not added. |
| Rollback boundary | Remove `src/main/java/pe/edu/upeu/saludablemente/teams/team/repository/TeamRepository.java` and revert only `docs/README.md`, `docs/rebuild/07-next-agent-handoff.md`, and `docs/rebuild/09-teams-package-structure.md`. |

## Attempt Evidence

- Native attempt settlement: `complete`.
- Evidence revision: `sha256:32c48297fbf4be23490c0180fdc3d0aac6a8296d9be6dfa1669a219db30ce9b8`.

## Deferred Verification

`findAllByActive(true)` and `findAllByActive(false)` require a real Spring Data repository integration test. That work is deferred pending separate authorization for isolated embedded test infrastructure; this change does not modify the POM, test configuration, or database setup.
