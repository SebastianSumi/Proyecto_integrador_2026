# Tasks: Teams MapStruct Mapper

## Review Workload Forecast

| Field | Value |
|-------|-------|
| Estimated changed lines | 120–220 authored lines |
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
| 1 | Implement and verify the Teams mapper, then align only approved mapper documentation | PR 1 | `./mvnw -Dtest=TeamMapperTest test` | N/A — direct MapStruct unit test intentionally avoids Spring and Oracle | Remove `TeamMapper.java`, `TeamMapperTest.java`, and mapper-only documentation edits |

## Phase 1: RED — Contract Tests First

- [x] 1.1 Create `src/test/java/pe/edu/upeu/saludablemente/teams/team/mapper/TeamMapperTest.java` with direct `Mappers.getMapper` setup; write the request-field/default assertions for a populated request and a null description, proving the tests fail before implementation.
- [x] 1.2 Add response mapping tests for all entity fields and for a null description while preserving `active`; keep scenarios independent and avoid Spring, Oracle, or repository/service/controller dependencies.

## Phase 2: GREEN — Mapper Implementation

- [x] 2.1 Create `src/main/java/pe/edu/upeu/saludablemente/teams/team/mapper/TeamMapper.java` as `@Mapper(componentModel = "spring")` with `toEntity(TeamRequest)` and `toResponse(Team)`; explicitly ignore `id` and `active` on request mapping.
- [x] 2.2 Run `./mvnw -Dtest=TeamMapperTest test`; confirm request name/description mapping, null-description preservation, null id, active=true default, and all response fields.

## Phase 3: REFACTOR — Focused Cleanup

- [x] 3.1 Refactor only mapper-test fixtures/assertions for clarity without changing the contract; rerun `./mvnw -Dtest=TeamMapperTest test` and `git diff --check`.
- [x] 3.2 Confirm the diff contains no changes to `Team`, DTOs, repositories, services, controllers, POM/configuration, SQL, migrations, or unapproved documentation.

## Phase 4: Approved Documentation Alignment

- [x] 4.1 Update only mapper-related statements in `docs/README.md`, `docs/rebuild/07-next-agent-handoff.md`, `docs/rebuild/08-teams-module-contract.md`, `docs/rebuild/09-teams-package-structure.md`, `docs/rebuild/10-teams-layered-module-blueprint.md`, and `docs/rebuild/11-bomerp-class-by-class-reference.md`; do not expand scope.
