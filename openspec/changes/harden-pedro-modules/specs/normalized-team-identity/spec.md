# Normalized Team Identity Specification

## Purpose
Make Team names deterministic and unique across fresh installs, upgrades, and concurrent requests.

## Requirements

### Requirement: Canonical Team names
Team names MUST be trimmed and compared case-insensitively; persisted and returned names MUST use the canonical trimmed value.

#### Scenario: Equivalent names
- GIVEN an existing Team named `Care`
- WHEN a request uses surrounding spaces or different casing
- THEN it is treated as the same name and rejected as a conflict

#### Scenario: Distinct names
- GIVEN no Team with the canonical name exists
- WHEN a valid create or update is submitted
- THEN exactly one Team with that canonical name is stored

### Requirement: Safe uniqueness migration
Schema upgrades MUST enforce canonical uniqueness and MUST abort with actionable collision diagnostics when pre-existing rows collide; migration MUST NOT rename, delete, or merge rows.

#### Scenario: Collisions during upgrade
- GIVEN legacy rows collide after normalization
- WHEN the upgrade runs
- THEN it fails before destructive changes and identifies every collision

#### Scenario: Concurrent creates
- GIVEN two transactions submit equivalent names concurrently
- WHEN both commit
- THEN at most one succeeds and the other receives a conflict response
