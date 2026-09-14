
# Tasks: Add the Teams Repository

## Review Workload Forecast

| Field | Value |
|-------|-------|
| Estimated changed lines | 60–100 authored lines |
| 400-line budget risk | Low |
| Chained PRs recommended | No |
| Suggested split | Single PR |
| Delivery strategy | ask-on-risk |
| Chain strategy | pending |

Decision needed before apply: No
Chained PRs recommended: No
Chain strategy: pending
400-line budget risk: Low

### Suggested Work Units

| Unit | Goal | Likely PR | Focused test command | Runtime harness | Rollback boundary |
|------|------|-----------|----------------------|-----------------|-------------------|
| 1 | Add the Team repository contract and synchronize the three Teams status documents | PR 1 | `mvn -DskipTests compile`; `git diff --check` | N/A: behavioral repository tests are deferred until authorized embedded infrastructure exists | Remove `TeamRepository.java` and revert only its Teams documentation status updates |

## Phase 1: Repository Contract

- [x] 1.1 Create `src/main/java/pe/edu/upeu/saludablemente/teams/team/repository/TeamRepository.java` with the Teams package, `JpaRepository<Team, Long>`, and only `List<Team> findAllByActive(boolean active)`; do not redeclare CRUD methods or add fetch annotations.
- [x] 1.2 Confirm the declaration uses the existing `Team.active` property and introduces no service, controller, entity, DTO, mapper, configuration, POM, SQL, migration, Oracle, or cross-module repository changes.

## Phase 2: Teams Status Documentation

- [x] 2.1 Update `docs/README.md` to mark the Teams repository layer as implemented while stating that behavioral repository testing remains deferred.
- [x] 2.2 Update `docs/rebuild/07-next-agent-handoff.md` to hand off from mapper to repository and preserve the no-Oracle, no-test-infrastructure, and path-restricted guardrails.
- [x] 2.3 Update `docs/rebuild/09-teams-package-structure.md` to mark `repository/` as implemented with the minimal contract; do not claim runtime query coverage.

## Phase 3: Verification and Attempt Settlement

- [x] 3.1 Acquire the native SDD runtime attempt for this work unit before running external validation; proceed only when the provider returns `state: proceed` and retain its opaque token.
- [x] 3.2 Run `mvn -DskipTests compile` and `git diff --check`; inspect the final diff for only the repository and the three necessary Teams status documents. Tasks cannot be marked complete until both checks pass.
- [x] 3.3 Settle the native attempt with the required passed/failed outcome, evidence revision, diagnosis, harness disposition, cleanup evidence, and process evidence; preserve deferred behavioral testing for a separately authorized test-infrastructure change.
