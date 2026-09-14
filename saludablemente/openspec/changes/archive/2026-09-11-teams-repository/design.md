# Design: Add the Teams Repository

## Technical Approach

Add one Spring Data repository interface inside the Team-owned package. The interface extends `JpaRepository<Team, Long>` for the inherited CRUD contract and declares only the active-status derived query required by the delta specification. No service, controller, entity, configuration, test-infrastructure, or cross-module persistence changes are part of this design.

## Architecture Decisions

### Decision: Keep persistence ownership inside the Team module

| Option | Tradeoff | Decision |
|---|---|---|
| Team-owned repository package | Preserves module ownership and prevents repository coupling across modules | Selected |
| Reuse another module's repository | Avoids a new interface but violates the modular-monolith boundary | Rejected |

### Decision: Use Spring Data inheritance and one derived query

| Option | Tradeoff | Decision |
|---|---|---|
| Extend `JpaRepository<Team, Long>` and declare `findAllByActive` | Minimal code; Spring Data supplies CRUD and derives the query from the entity property | Selected |
| Redeclare CRUD or add custom/name-based methods | Expands the contract without a specified requirement | Rejected |
| Add `@EntityGraph`, eager fetching, or custom JPQL | Introduces unsupported fetch/query behavior | Rejected |

### Decision: Defer behavioral repository testing

| Option | Tradeoff | Decision |
|---|---|---|
| Add H2 and repository configuration | Enables behavioral query tests but requires forbidden POM/configuration changes | Rejected for this change |
| Add mock or reflection tests | Avoids database setup but does not prove Spring Data query behavior | Rejected |
| Compile and inspect the diff | Verifies the Java contract and scope, but not runtime query semantics | Selected for this change |

## Data Flow

```text
Future Team application service
        |
        v
TeamRepository.findAllByActive(active)
        |
        v
Spring Data query derivation -> Team.active
```

No controller or service is added now. Future consumers use the public Team-owned repository through the Teams module; no other module repository is accessed.

## File Changes

| File | Action | Description |
|---|---|---|
| `src/main/java/pe/edu/upeu/saludablemente/teams/team/repository/TeamRepository.java` | Create | Define the minimal Team persistence boundary. |

## Interfaces / Contracts

```java
package pe.edu.upeu.saludablemente.teams.team.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.saludablemente.teams.team.entity.Team;

public interface TeamRepository extends JpaRepository<Team, Long> {

    List<Team> findAllByActive(boolean active);
}
```

The interface declares no other method. `JpaRepository` remains the sole CRUD contract, and Spring Data derives the filter from the existing `Team.active` boolean property.

## Testing Strategy

| Layer | What to Validate | Approach |
|---|---|---|
| Compile | Imports, generic types, repository declaration, and derived-method signature compile | Run `mvn -DskipTests compile`. |
| Scope | Only the authorized repository interface is introduced during implementation | Run `git diff --check` and inspect the final diff. |
| Repository behavior | Filtering for both `active=true` and `active=false` | Deferred to a separately authorized test-infrastructure change. The current scope forbids the POM, test configuration, and H2 changes required for a real repository integration test. |

## Threat Matrix

N/A — no routing, shell, subprocess, VCS/PR automation, executable-file classification, or process-integration boundary is introduced.

## Migration / Rollout

No migration required. The change adds only a Java persistence interface and does not modify schema, stored data, or runtime configuration.

## Open Questions

None.
