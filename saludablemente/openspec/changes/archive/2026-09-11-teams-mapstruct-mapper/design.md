# Design: Add the Teams MapStruct Mapper

## Technical Approach

Add a MapStruct interface in the Teams mapper package and a focused JUnit 5 test. The mapper is generated at compile time and registered as a Spring component, while the test obtains the generated implementation directly through MapStruct so it needs neither a Spring context nor Oracle. Request mapping explicitly ignores `id` and `active`; this lets `new Team()` retain its `null` identifier and `active = true` initializer.

## Architecture Decisions

### Mapper Definition

| Option | Tradeoff | Decision |
|---|---|---|
| `@Mapper(componentModel = "spring")` interface | Compile-time implementation and dependency-injection support without handwritten conversion code | Use this option |
| Handwritten Spring component | More implementation code and possible contract drift | Reject |
| Static utility mapper | Simple invocation but not Spring-managed | Reject |

### Preserve Entity-Owned Defaults

| Option | Tradeoff | Decision |
|---|---|---|
| Explicitly ignore `id` and `active` in `toEntity` | Documents intent and prevents generated setters from overwriting defaults | Use this option |
| Map every target property implicitly | Could assign primitive `false` to `active` | Reject |
| Restore defaults after mapping | Couples mapping code to entity initialization rules | Reject |

### Test the Generated Mapper Directly

| Option | Tradeoff | Decision |
|---|---|---|
| `Mappers.getMapper(TeamMapper.class)` in JUnit 5 | Exercises generated code with no application context or database | Use this option |
| `@SpringBootTest` | Adds unnecessary startup and infrastructure coupling | Reject |
| Mock the mapper | Does not verify generated mapping behavior | Reject |

## Data Flow

```text
TeamRequest ──toEntity──> generated TeamMapperImpl ──> new Team
    name, description                              id = null
                                                   active = true

Team ──toResponse──> generated TeamMapperImpl ──> TeamResponse
                 id, name, description, active
```

## File Changes

| File | Action | Description |
|---|---|---|
| `src/main/java/pe/edu/upeu/saludablemente/teams/team/mapper/TeamMapper.java` | Create | Declare the Spring component-model mapper and both conversion methods. |
| `src/test/java/pe/edu/upeu/saludablemente/teams/team/mapper/TeamMapperTest.java` | Create | Verify request mapping, preserved defaults, response mapping, and nullable descriptions without Spring. |
| `docs/README.md` | Modify | Reflect completion of the Teams mapper contract only. |
| `docs/rebuild/07-next-agent-handoff.md` | Modify | Update the mapper-related handoff status only. |
| `docs/rebuild/08-teams-module-contract.md` | Modify | Document the implemented mapper boundary only. |
| `docs/rebuild/09-teams-package-structure.md` | Modify | Add the mapper package/file to the documented structure only. |
| `docs/rebuild/10-teams-layered-module-blueprint.md` | Modify | Reflect the mapper layer implementation only. |
| `docs/rebuild/11-bomerp-class-by-class-reference.md` | Modify | Add the `TeamMapper` class contract only. |

The implementation scope remains exclusively `TeamMapper` and `TeamMapperTest`. Documentation changes are limited to the six approved files above and only to content necessary to reflect mapper completion. No other source, test, DTO/entity, repository/service/controller, POM, configuration, documentation, or database file changes are permitted.

## Interfaces / Contracts

```java
@Mapper(componentModel = "spring")
public interface TeamMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    Team toEntity(TeamRequest request);

    TeamResponse toResponse(Team team);
}
```

`toEntity` maps only `name` and `description`. `toResponse` maps `id`, `name`, `description`, and `active` by matching property names.

## Testing Strategy

| Layer | What to Test | Approach |
|---|---|---|
| Unit | Request fields and entity defaults | Instantiate the generated mapper directly; assert name, description, null id, and true active. |
| Unit | Null request description | Assert null is preserved while id and active defaults remain intact. |
| Unit | Entity-to-response fields | Populate a `Team`; assert all four response properties match. |
| Unit | Null entity description | Assert null description and active state are preserved. |
| Integration/E2E | Not applicable | No runtime integration or API behavior changes are in scope. |

## Threat Matrix

N/A — no routing, shell, subprocess, VCS/PR automation, executable-file classification, or process-integration boundary.

## Migration / Rollout

No migration required. MapStruct generates the implementation during the existing Maven compilation process.

## Open Questions

None.
