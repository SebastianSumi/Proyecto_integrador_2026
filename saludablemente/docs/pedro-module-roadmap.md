# Pedro Module Roadmap

This roadmap is the authoritative implementation view for Pedro's **Activities, Teams, and Goals** scope, including the accepted Activities-owned Enrollment correction. It reconciles the project brief with the shared logical schema while preserving the current Java and Oracle modular-monolith architecture.

## Status at a glance

| Module | Status | Current boundary |
|---|---|---|
| Teams | **Implemented / verified** | Core create/read/update, explicit active state, Oracle mapping, Spanish REST contract |
| Activities | **Implemented / verified** | Activity header, lifecycle, temporal validation, overlap checks and locking; concurrent integration proof pending |
| Enrollment | **Implemented / accepted correction** | Activities-local pre-event relationship; not defined by either PDF; stores current/latest membership state |
| Goals | **Core planned; automatic integration externally blocked** | Core can be designed around `id_persona` and indicator codes; provider validation and automatic observations require agreed contracts |

Status meanings:

- **Implemented:** source and Oracle provisioning exist.
- **Verified:** focused tests, full tests, or runtime evidence listed below passed for the implemented behavior.
- **Planned:** owned by Pedro but not implemented yet.
- **Accepted correction:** project-owned clarification that closes a source-document gap without claiming the PDFs define it.
- **Externally blocked:** implementation depends on a public contract from another module; Pedro must not replace that module or access its repository.

## Technical baseline

| Concern | Project decision |
|---|---|
| Runtime | Java 21, Spring Boot 4.0.7 |
| Build | Maven, executable JAR |
| Database | Oracle 23 with module-owned `SAL_*` schemas |
| Architecture | Spring Modulith modular monolith; entities and repositories remain private to their modules |
| HTTP contract | REST under `/api/v1`; public JSON and OpenAPI wording in Spanish, Spanish Activity/Enrollment type names; existing shared/Teams naming retained |
| Persistence | Input/output DTOs only; JPA entities are never serialized directly; transactions live at application-service boundaries |

The PDF schema is a shared logical model, not executable MySQL DDL. Oracle names and types may differ when the attribute meaning, identifier, nullability, and relationship remain traceable.

## Confirmed source and identity boundary

The brief assigns Activities, Teams, and Goals to Pedro. Activity is the header and Attendance is its explicit detail, owned by Francisco. Activity exists before attendance, which is recorded during the event or synchronized later; a header-detail relationship does not require creating both simultaneously.

The word **"inscritos" appears once**, in the Activities notification rule. Neither PDF defines an `Inscripcion` entity/table/module, flow, or owner. The project has accepted `Inscripcion` as an Activities-owned correction that provides the missing pre-event relationship. This decision is recorded without rewriting the PDFs; alternatives and tradeoffs remain documented in [External Module Recommendations](external-module-recommendations.md). Attendance cannot supply pre-event recipients.

Use `PERSONA.id_persona` for cross-module identity. Names are display data, not identifiers/FKs. DNI is absent from both PDFs and, per the user, the staff spreadsheet. Do not require or invent it. A later Personal-owned DNI business key would not replace the stable primary ID in foreign references.

Team, Activity, and Enrollment cores remain valid independently of DNI. Teammates follow the PDFs; consume agreed stable IDs/public contracts without inventing their entities, repositories, or physical tables.

## Traceability matrix

### Teams

| Brief requirement | Schema attribute/entity | Current implementation | Corrected inconsistency or decision | Remaining acceptance evidence |
|---|---|---|---|---|
| Create and edit Teams | `TEAM.id_team`, `nombre`, `descripcion` | **Implemented / verified:** `SAL_TEAMS.TEAMS`; `POST`, `PUT`, `GET` under `/api/v1/equipos` | Logical names map to `ID`, `NAME`, and `DESCRIPTION`; wider `VARCHAR2` limits are retained without changing semantics | End-to-end acceptance with production-like authorization when Auth exists |
| Inactivate Teams | `TEAM.activo` | **Implemented / verified:** explicit idempotent `PATCH /{id}/estado`; Java boolean maps to Oracle `NUMBER(1)` | Oracle check restricts values to `0/1`; no ambiguous toggle | Authorization evidence when roles are integrated |
| Prevent deletion with active people | `PERSONA.id_team`; no deletion coordination entity | **Externally blocked:** no hard-delete endpoint | Do not fake a zero count or access Personal's repository; Personal must expose active-person existence/count | Personal contract test plus rollback/conflict API test |
| List active-person count per Team | `TEAM 1:N PERSONA` | **Externally blocked** | Count belongs to Personal data authority and is composed through a public query | Query integration and list-response acceptance test |

### Activities

| Brief requirement | Schema attribute/entity | Current implementation | Corrected inconsistency or decision | Remaining acceptance evidence |
|---|---|---|---|---|
| Register activity details | `ACTIVIDAD`: ID, name, description, date, start/end time, place, state, creator user ID | **Implemented / verified:** Activity entity, DTOs, mapper, service, controller, repository, `SAL_ACTIVITIES` DDL | Public `fecha` and time fields remain separate; Oracle stores `START_AT`/`END_AT` timestamps safely | Auth creator validation after its public contract exists |
| End must be after start | Implied by `hora_inicio` / `hora_fin`; no schema check in PDF | **Implemented / verified:** service/entity rule and Oracle `CHECK (END_AT > START_AT)` | Added required invariant rather than relying on client validation | Already covered by focused service tests; retain Oracle integration evidence |
| Prevent same-place overlap | Activity date/time/place; no concurrency mechanism in PDF | **Implemented / verified:** normalized `PLACE_KEY`, place coordination row, pessimistic lock, and open-interval overlap query | Uses `newStart < existingEnd && newEnd > existingStart`; cancelled activities ignored; contiguous schedules allowed; an `exists` query alone was rejected as unsafe | Add a dedicated concurrent integration test when an Oracle test harness is standardized |
| Change lifecycle state | `estado`: Programada, En curso, Finalizada, Cancelada | **Implemented / verified:** controlled transitions and idempotent explicit state setter | Internal English enum; public Spanish values `PROGRAMADA`, `EN_CURSO`, `FINALIZADA`, `CANCELADA`; terminal states reject transitions | Authorization and notification-event acceptance after external contracts |
| Notify enrolled people on change | Brief requirement; no enrollment entity in PDF schema | **Partially implemented:** accepted Enrollment records define recipients; notification publication/delivery is not implemented | Attendance is not enrollment. Delivery must not run inside the Activity transaction | Durable event/outbox proof and Notification consumer contract |

### Enrollment: accepted correction, not a PDF entity

| Evidence category | Current position | Next decision or proof |
|---|---|---|
| Confirmed source | Only "inscritos" in the notification rule; no defined enrollment workflow or owner | Project decision accepts Enrollment inside Activities as the correction |
| Current implementation | `Inscripcion`, Oracle relationship, API, scheduled-only changes, unique Activity/Person pair, logical cancellation and reactivation | Keep the boundary inside Activities; Personal validation remains external |
| Accepted rules | Batches capped at 100; duplicate input and active duplicate rejected; same-row reactivation replaces latest timestamps, not full cycle history | Unit tests cover the rules; Oracle rollback/concurrency proof remains pending |
| Verification boundary | Unit/API checks exist; batch service uses a transaction | Real Oracle rollback/concurrency proof remains pending; do not claim full transactional proof |
| Explicit Activity detail | Attendance remains Francisco's detail, not Enrollment | Agree public Activity boundary and applicable Attendance transaction/late-sync behavior |

### Goals

| Brief requirement | Schema attribute/entity | Current implementation | Corrected inconsistency or decision | Remaining acceptance evidence |
|---|---|---|---|---|
| Create a Goal for a person and health indicator | `META`: ID, Person ID, type, description, target/current values, start/deadline, state | **Core design can proceed; implementation pending** | Design around `id_persona` and indicator codes; preserve every logical attribute in an Oracle-owned Goals schema; validate Person through Personal's public service | Personal contract, DTO/API tests, Oracle constraint evidence |
| Update current value from a related evaluation | `tipo_meta`, `valor_actual`; no stable integration contract | **Externally blocked** | Evaluation modules publish a normalized indicator observation; Goals must not read Nutrition or Physical Fitness repositories | Agreed indicator codes, units, source record identity, idempotent event test |
| Mark Goal complete automatically | `estado`: En curso, Cumplida, Vencida | **Planned / externally blocked** | Indicator definition must state comparison direction (`<=`, `>=`, or approved rule); free-text matching is not acceptable | Product-approved comparison catalog and domain tests for both directions/deadlines |

## Phased work units

| Order | Work unit | Boundary / entry condition | Completion evidence |
|---|---|---|---|
| 1 | Consolidate Activity and Team cores | Preserve valid existing behavior; no dependence on DNI or a new Enrollment decision | Focused validation and previously identified integration gaps addressed in an authorized implementation slice |
| 2 | Publish Activity changes for notifications | Enrollment is the accepted recipient source; Notification ownership remains external | Durable outbox/event evidence and an agreed consumer contract |
| 3 | Activity public boundary for Attendance | Agree minimum fields with Francisco; Activity exists before Attendance | Module-boundary and applicable transaction/late-sync acceptance tests |
| 4 | Team-Personal completion | Agreed count/existence contract using stable IDs | Accurate active-person counts and protected deletion acceptance |
| 5 | Goals core | Design around `id_persona`, indicator codes, targets and deadlines; coordinate provider validation before integration | Domain/API/schema evidence; no invented DNI dependency |
| 6 | Automatic integrations | Agreed indicator vocabulary/source events and notification recipient contract | Goal update/idempotency tests; durable notification publication and retry evidence |

Implementation status belongs here. External recommendations are proposals; the [next-iteration guide](next-iteration.md) owns local operational instructions. No implementation or SDD proposal is authorized by this documentation correction.

## Current verification evidence

Activity core evidence captured on 2026-09-06:

- Focused Maven tests: **9 passed**, 0 failures/errors.
- Full Maven suite: **26 passed**, 0 failures/errors; Spring Modulith verification passed.
- Oracle 23 runtime: Hibernate schema validation succeeded; health was `UP`; OpenAPI contained `/api/v1/actividades`; Activity listing returned HTTP `200`.
- Runtime behavior: a temporary Activity returned HTTP `201` with `PROGRAMADA`; an overlapping Activity returned HTTP `409`; temporary rows were removed and cleanup was verified.
- Known non-blocking warnings: Mockito dynamic-agent deprecation and an optional Oracle JDBC `oraclepki.jar` manifest reference.

Enrollment core and Spanish naming-refactor evidence captured on 2026-09-07:

- Focused Activity and Enrollment Maven tests: **20 passed**, 0 failures/errors.
- Full Maven suite: **37 passed**, 0 failures/errors/skipped; Spring Modulith verification passed.
- At the recorded verification, Oracle startup passed Hibernate schema validation, and OpenAPI exposed the Activity and Enrollment routes. A data-mutating Oracle rollback/concurrency scenario has not yet been executed.

Check runtime availability using the operational guide before a demo; a historical startup result is not current liveness or transactional integration proof.

## Explicit non-goals

- Do not implement Personal, Attendance, Notifications, Nutrition, Physical Fitness, or Auth inside Pedro's modules.
- Do not import or access another module's JPA entity or repository.
- Do not treat Attendance as the source of enrollment recipients.
- Do not add a physical cross-schema foreign key until the authoritative owner/table/column and `REFERENCES` grant are agreed.
- Do not implement hard deletion where historical Activity, Enrollment, Attendance, Team, or Goal data must remain auditable.
- Do not add `@EntityGraph`, eager relationships, AI recommendations, offline Attendance synchronization, or unrelated modules without evidence and explicit scope.

## External blockers

Proposed provider contracts, not authoritative teammate interfaces, are maintained in [External Module Recommendations](external-module-recommendations.md). In priority order:

1. Personal: active Person validation and active-person Team count/existence.
2. Auth: authoritative active creator ID and roles.
3. Attendance: Activity-history existence/count without repository crossing.
4. Evaluation sources: normalized indicator observations.
5. Notifications: durable consumption of Activity-change events.
6. DBA coordination: Oracle owners, grants, migration order, and physical FK targets.

## Next step

Treat Enrollment as part of the accepted Activity baseline, then harden the current cores and plan Goals around stable IDs. Do not implement external modules; coordinate their public contracts before integration.
