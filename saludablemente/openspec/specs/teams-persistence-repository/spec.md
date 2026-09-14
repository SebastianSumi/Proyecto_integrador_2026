# Teams Persistence Repository Specification

## Purpose

Define the minimal Teams-owned Spring Data repository boundary for CRUD access and active-status filtering, without expanding persistence behavior or test infrastructure.

## Requirements

### Requirement: Teams repository exposes the typed CRUD boundary

The Teams repository MUST define `TeamRepository` as extending `JpaRepository<Team, Long>`. The repository MUST rely on the inherited CRUD contract and MUST NOT redeclare inherited operations such as create, read, update, or delete methods.

#### Scenario: Repository type provides inherited CRUD

- GIVEN the Teams persistence repository is available
- WHEN consumers use its repository contract
- THEN `TeamRepository` is assignable to `JpaRepository<Team, Long>`
- AND inherited CRUD operations are available without redeclarations

#### Scenario: Inherited CRUD remains the only CRUD contract

- GIVEN the repository declaration is inspected
- WHEN its declared methods are reviewed
- THEN no custom method duplicates an inherited CRUD operation

### Requirement: Repository filters teams by active status

Beyond inherited operations, the repository MUST declare only the derived method `List<Team> findAllByActive(boolean active)`. The method MUST select Teams whose `active` value equals the supplied boolean.

#### Scenario: Filter active teams

- GIVEN Teams exist with both `active=true` and `active=false`
- WHEN `findAllByActive(true)` is called
- THEN the result contains only Teams with `active=true`

#### Scenario: Filter inactive teams

- GIVEN Teams exist with both `active=true` and `active=false`
- WHEN `findAllByActive(false)` is called
- THEN the result contains only Teams with `active=false`

### Requirement: Repository scope excludes unsupported behavior

The repository MUST NOT introduce or document uniqueness guarantees, custom deletion semantics, cross-module repository access, eager fetching, or `@EntityGraph` behavior. No additional derived or custom repository method is part of this change.

#### Scenario: Unsupported behavior is absent

- GIVEN the repository contract is reviewed
- WHEN methods and fetch annotations are inspected
- THEN only inherited CRUD and `findAllByActive(boolean)` are documented
- AND no uniqueness, deletion, cross-module, eager-fetch, or `@EntityGraph` contract is present

### Requirement: Behavioral repository testing is deferred

Behavioral repository integration testing MUST be deferred because adding an embedded database, configuration, or POM changes is outside this change's scope. Mock-based or reflection-based substitutes MUST NOT be added as a replacement.

#### Scenario: Verification remains infrastructure-neutral

- GIVEN the current project lacks authorized embedded repository test infrastructure
- WHEN this repository change is verified
- THEN no repository integration test or test-infrastructure change is introduced
- AND no mock or reflection substitute claims behavioral query coverage
