# Pedro Operations Guide Specification

## Purpose
Document only commands and behavior verified for Pedro-owned modules.

## Requirements

### Requirement: Truthful quick path
The guide MUST document Java 21, Spring Boot 4.0.7, Maven/JAR execution, Oracle prerequisites, external configuration, and verified provisioning/test commands.

#### Scenario: New operator setup
- GIVEN an operator follows the quick path
- WHEN prerequisites and commands are run
- THEN commands match repository behavior and do not assume a repaired Maven Wrapper

### Requirement: Explicit boundaries and evidence
The guide MUST state that Inscripcion is owned by Actividades with current/latest membership semantics, Asistencia remains Francisco-owned, and cross-module identity uses `id_persona` only.

#### Scenario: Review against implementation
- GIVEN a reviewer compares the guide with APIs and schemas
- WHEN ownership and identity claims are checked
- THEN no unrelated module, full enrollment history, or unsupported guarantee is asserted
