# Proposal: Add the Teams Repository

## Intent

Introduce the minimal Spring Data persistence boundary for `Team` so later application-service work can use inherited CRUD operations and the documented active-status query without accessing another module's repository.

## Scope

### In Scope
- Create `TeamRepository` extending `JpaRepository<Team, Long>`.
- Declare only `List<Team> findAllByActive(boolean active)` beyond inherited operations.
- Update only Teams status/rebuild documentation needed to mark the repository layer accurately.
- Require later Maven validation and `git diff --check`.

### Out of Scope
- Entity, DTO, mapper, service, controller, configuration, POM, SQL, migration, Docker, Oracle, or other module repository changes.
- Uniqueness, deletion, cross-module repository access, eager fetching, or `@EntityGraph` behavior.
- Repository behavioral tests in this change: the current POM has no embedded test database, and test-infrastructure changes are not authorized. An isolated test-infrastructure change must enable those tests later; superficial mock tests are explicitly excluded.

## Capabilities

### New Capabilities
- `teams-persistence-repository`: Spring Data persistence contract for Teams CRUD and filtering by active status.

### Modified Capabilities
None.

## Approach

Add one repository interface in the Teams-owned repository package and rely on Spring Data query derivation for `findAllByActive`. This follows the historical `InscripcionRepository` precedent from commit `e627de9`, where module-owned repositories extend `JpaRepository` and expose typed derived queries. The archived `teams-mapstruct-mapper` change is evidence that entity, DTO, and mapper prerequisites are complete and verified.

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `src/main/java/pe/edu/upeu/saludablemente/teams/team/repository/TeamRepository.java` | New | Minimal Teams-owned Spring Data contract. |
| `docs/README.md` | Modified | Teams layer status only. |
| `docs/rebuild/07-next-agent-handoff.md` | Modified | Current handoff and repository guardrails. |
| `docs/rebuild/09-teams-package-structure.md` | Modified | Repository package status only. |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| Derived query contract is not behaviorally exercised. | Medium | Defer integration testing explicitly; compile with Maven now and add repository tests only with authorized isolated test infrastructure. |
| Scope expands into persistence or service design. | Low | Enforce the single-file contract and path-restricted diff review. |

## Rollback Plan

Remove `TeamRepository.java` and revert only its Teams status documentation updates. No schema or persisted-data rollback is required.

## Dependencies

- Existing `Team` entity and Spring Data JPA dependency.
- Archived `teams-mapstruct-mapper` completion evidence.
- Documented query contract in `docs/rebuild/10-teams-layered-module-blueprint.md` and `11-bomerp-class-by-class-reference.md`.

## Success Criteria

- [ ] `TeamRepository` extends `JpaRepository<Team, Long>` and declares only `findAllByActive(boolean active)`.
- [ ] Maven validation succeeds without Oracle or new test infrastructure.
- [ ] `git diff --check` passes and the diff contains only the repository plus necessary Teams status/rebuild documentation.
- [ ] Deferred repository behavioral testing is tracked for a separately authorized isolated test-infrastructure change.
