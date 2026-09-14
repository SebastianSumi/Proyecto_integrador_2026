# Proposal: Add the Teams MapStruct Mapper

## Intent

Introduce a compile-time, Spring-managed mapping boundary between Teams DTOs and the `Team` entity. This removes manual conversion ambiguity while preserving entity-owned defaults for create requests.

## Scope

### In Scope
- Add `TeamMapper` with `@Mapper(componentModel = "spring")`, `toEntity(TeamRequest)`, and `toResponse(Team)`.
- Map request `name` and `description` only; leave generated `id` null and preserve the entity default `active = true`.
- Map entity `id`, `name`, `description`, and `active` to `TeamResponse`.
- Add a focused mapper test that runs without Spring or Oracle and follows RED-GREEN-REFACTOR.
- Align the approved Teams documentation files with the mapper contract.

### Out of Scope
- Changes to `Team`, `TeamRequest`, `TeamResponse`, build/configuration files, SQL, migrations, repositories, services, or controllers.
- Oracle-backed or Spring-context integration tests.
- Commits, pushes, pull requests, or unrelated module work.

## Capabilities

### New Capabilities
- `teams-object-mapping`: Compile-time conversion between Teams request/response DTOs and the `Team` entity, including preservation of entity defaults during request mapping.

### Modified Capabilities
None.

## Approach

Define one MapStruct interface in the Teams mapper package using the existing MapStruct 1.6.3 processor configuration. Verify generated behavior through direct mapper instantiation in a focused unit test, then synchronize only the approved Teams documentation.

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `src/main/java/pe/edu/upeu/saludablemente/teams/team/mapper/TeamMapper.java` | New | Spring-managed MapStruct contract. |
| `src/test/java/pe/edu/upeu/saludablemente/teams/team/mapper/TeamMapperTest.java` | New | Focused request/entity/response mapping tests. |
| `docs/README.md` and `docs/rebuild/{07-next-agent-handoff,08-teams-module-contract,09-teams-package-structure,10-teams-layered-module-blueprint,11-bomerp-class-by-class-reference}.md` | Modified | Mapper-layer documentation only. |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| Generated request mapping overwrites entity defaults. | Medium | Assert null `id` and true `active` in the focused test. |
| Documentation drifts beyond approved scope. | Low | Restrict edits and fresh-diff review to the listed files. |

## Rollback Plan

Remove the mapper, focused test, and mapper-specific documentation edits; no schema, configuration, or persisted-data rollback is required.

## Dependencies

- Existing MapStruct 1.6.3 API and annotation processor configuration.
- Existing public no-args construction and setters on `Team`.

## Success Criteria

- [ ] Focused mapper test passes without Spring or Oracle after RED-GREEN-REFACTOR.
- [ ] Full Maven suite and `git diff --check` pass.
- [ ] Fresh diff review confirms only approved paths changed and the next step is exclusively repository delivery.
