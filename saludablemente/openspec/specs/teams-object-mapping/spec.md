# Teams Object Mapping Specification

## Purpose

Define the Teams mapper contract for converting team request data into new entities and existing entities into response data while preserving entity-owned defaults.

## Requirements

### Requirement: Map team requests to new entities

The mapper MUST create a `Team` entity from a `TeamRequest` using only the request's `name` and `description` values. The mapping MUST NOT assign an identifier, and MUST preserve the entity's default `active` value of `true`.

#### Scenario: Request fields are mapped and entity defaults are preserved

- GIVEN a `TeamRequest` with a name and description
- WHEN the request is mapped to a new `Team`
- THEN the entity has the same name and description
- AND its id is `null`
- AND its active value is `true`

#### Scenario: Null request description remains null

- GIVEN a `TeamRequest` with a name and a `null` description
- WHEN the request is mapped to a new `Team`
- THEN the entity has the same name and a `null` description
- AND its id is `null`
- AND its active value is `true`

### Requirement: Map team entities to responses

The mapper MUST create a `TeamResponse` containing the source entity's `id`, `name`, `description`, and `active` values without changing them.

#### Scenario: All entity fields are mapped to the response

- GIVEN a `Team` with an id, name, description, and active value
- WHEN the entity is mapped to a `TeamResponse`
- THEN the response contains the same id, name, description, and active value

#### Scenario: Null description is preserved in the response

- GIVEN a `Team` with an id, name, and `null` description
- WHEN the entity is mapped to a `TeamResponse`
- THEN the response contains the same id, name, and `null` description
- AND its active value matches the entity
