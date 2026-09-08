# External Module Recommendations for Pedro's Modules

This document contains non-authoritative collaboration proposals for **Activities, Teams, and Goals**. Teammates are expected to follow the brief and shared schema; proposed contracts below require coordination, not unilateral adoption. The [Pedro Module Roadmap](pedro-module-roadmap.md) owns implementation status and sequencing.

## Scope and non-goals

The brief assigns **Activities, Teams, and Goals** to Pedro and **Attendance** to Francisco. Pedro has accepted `Inscripcion` as an Activities-owned correction for the brief's otherwise undefined pre-event recipients. This is a project design decision, not a claim that either PDF defines the entity.

Pedro must not implement the modules described below, map their entities inside his modules, or access their repositories directly. Integration must use stable identifiers and explicitly exposed public services or events.

This document does not prescribe internal packages, controllers, workflows, or storage beyond the data required at module boundaries. It also does not assign implementation work to a teammate beyond ownership already established by the brief.

## Source requirement and accepted correction

- The Activities notification rule uses **"inscritos" once**. Neither PDF defines an `Inscripcion` entity, table, module, flow, or owner.
- The explicit Activity detail is Attendance, owned by Francisco. Activity exists first; attendance is recorded during the event or synchronized later. Attendance cannot identify pre-event notification recipients.
- The accepted correction adds `Inscripcion` inside Activities. It uses `PERSONA.id_persona` as the stable external reference and does not import Personal persistence.

The project selected the first resolution:

| Resolution path | Benefit | Tradeoff / agreement needed |
|---|---|---|
| **Selected:** retain an Activities-owned Enrollment relationship and flow | Explicit pre-event recipients; existing code is reusable | Adds a concept beyond the PDFs; the accepted model stores one current/latest row per Activity/Person, not a complete episode history |
| Replace "inscritos" with an explicitly defined recipient source | May avoid a new relationship and workflow | Clarifies or changes the notification requirement; agree exactly who is notified, who owns the source, and when recipients are resolved |

The unselected path remains documented only as context. Attendance still cannot be used to infer pre-event recipients or justify inventing a teammate's storage model.

## Personal

| Aspect | Recommendation |
|---|---|
| Brief requirement | Persona is the central entity. A Team cannot be deleted while active people are assigned, Team listings include the active-person count, and Activities, Goals, and Attendance identify people. |
| Schema gap | The schema defines `PERSONA.id_team`, but it does not define a module-level query contract or deletion/count coordination. |
| Impact | Pedro cannot implement truthful Team counts or safe deletion, validate Activity enrollment, or create Goals for a valid active person without crossing the module boundary. |
| Proposed solution | Personal owns Person data and the Team association and exposes the minimum read-only contract below. |
| Tradeoff | A public service preserves module ownership but introduces a runtime dependency. A physical foreign key adds database integrity but does not replace business validation. |
| Owner | Personal module owner. |

Proposed public contract for Pedro's modules:

```java
public interface PersonDirectory {
    PersonReference requireActive(Long personId);
    long countActiveByTeamId(Long teamId);
    boolean hasAnyActiveByTeamId(Long teamId);
}

public record PersonReference(Long id, boolean active) {}
```

The implementation remains inside Personal; consumers must not query its repositories. Proposed Java names above are examples, not claims that those types exist.

**Identity contract:** cross-module references use the stable `PERSONA.id_persona` primary ID (`personId` in the example). A person's name is display data, not an identity or foreign key. DNI is absent from the brief and schema and, according to the user, from the current staff spreadsheet; it must not be invented or required. If Personal later adds DNI, it is a coordinated business key owned by that module; foreign references still use `id_persona`. Goals core can be designed around this ID and indicator codes without waiting for a DNI field. Active-person validation is a proposed business rule to confirm where applicable, not an added identity requirement.

## Attendance

| Aspect | Recommendation |
|---|---|
| Brief requirement | Activity is the transaction header owned by Pedro; Attendance is the detail owned by Francisco. Each attendance records person, exact marking time, registration method, and offline synchronization state. |
| Schema gap | The schema has the required foreign IDs and uniqueness rule, but it does not define the application contract between the modules or the policy for deleting an Activity with attendance history. |
| Impact | Activities needs to protect history without reading Attendance tables. Attendance needs to validate Activity state without owning or mutating Activity. |
| Proposed solution | Francisco's Attendance module exposes only the history query Activities needs. Attendance consumes Pedro's public Activity query to validate the referenced header. |
| Tradeoff | Bidirectional module dependencies must be avoided. Use a narrow query from Activities to Attendance only for lifecycle protection; operational attendance creation calls Activities, not the reverse. If Modulith detects a cycle, move the orchestration to a dedicated application-level use case while both domain modules keep their own repositories. |
| Owner | Francisco, as assigned by the brief. |

Proposed public contract for Activities:

```java
public interface AttendanceHistoryQuery {
    boolean existsByActivityId(Long activityId);
    long countByActivityId(Long activityId);
}
```

Attendance requires a Pedro-owned Activity contract containing only `id`, `state`, `startAt`, and `endAt`. It must not access `ActividadRepository`.

## Notifications

| Aspect | Recommendation |
|---|---|
| Brief requirement | Activity state changes must notify enrolled people. The Notification model identifies a recipient, channel, origin module, origin reference, message, read state, and send time. |
| Schema gap | Neither PDF defines Enrollment or its owner/flow. The project has corrected the recipient-source gap with Activities-owned Enrollment, while reliable delivery remains undefined. |
| Impact | Notifications cannot infer recipients from Attendance because attendance occurs after or during the event. A direct email/push call inside the Activity transaction would couple persistence to an external side effect. |
| Proposed solution | Activities resolves enrolled person IDs through its accepted relationship and publishes a durable `ActivityChanged` event after committing the business change. Notifications consumes the event and owns channel delivery, retries, and read state. |
| Tradeoff | Asynchronous delivery is eventually consistent, but it prevents a provider outage from rolling back a valid Activity update. A durable outbox is preferable to an in-memory event when delivery guarantees matter. |
| Owner | Notifications module owner. |

Proposed event for Activities:

```java
public record ActivityChanged(
        Long activityId,
        String previousState,
        String currentState,
        List<Long> recipientPersonIds,
        Instant occurredAt
) {}
```

The event is a boundary DTO, not a Notification entity. Notifications decides channels and message rendering.

## Nutrition and Physical Fitness evaluation sources

| Aspect | Recommendation |
|---|---|
| Brief requirement | Goal current values update when a related evaluation is recorded, and a Goal becomes complete when the current value reaches its target. |
| Schema gap | `META.tipo_meta` is free text. The schema does not define a stable indicator vocabulary, comparison direction, unit, or an evaluation-to-Goal event contract. |
| Impact | Goals cannot reliably decide whether an evaluation applies or whether lower or higher values satisfy the target. Directly reading evaluation repositories would break module ownership. |
| Proposed solution | Evaluation modules publish a normalized indicator observation. The team must agree indicator codes, units, and comparison direction before automatic completion is enabled. |
| Tradeoff | A shared vocabulary requires coordination, but prevents string matching, unit confusion, and duplicated clinical interpretation in Goals. Evaluation modules remain the authority for measured values; Goals remains the authority for progress state. |
| Owner | Nutrition and Physical Fitness module owners for source observations; Pedro for Goal evaluation against the agreed indicator contract. |

Proposed event for Goals:

```java
public record HealthIndicatorObserved(
        Long personId,
        String indicatorCode,
        BigDecimal value,
        String unit,
        Instant observedAt,
        String sourceModule,
        Long sourceRecordId
) {}
```

The shared indicator catalog must additionally define whether success means `value <= target`, `value >= target`, or another explicitly approved rule.

## Auth / Users

| Aspect | Recommendation |
|---|---|
| Brief requirement | `ACTIVIDAD.id_usuario_creador` references the user who created the activity. Roles include Administrator and Activity Coordinator responsibilities. |
| Schema gap | The logical schema provides the foreign ID but does not define the module service used to validate an active creator or expose authorization claims. |
| Impact | Activities cannot safely accept an arbitrary creator ID or import Auth persistence types. |
| Proposed solution | Auth owns identity, active state, and roles. Activities receives the authenticated user ID from the security context and validates it through the minimum public contract if the security principal does not already carry authoritative status. |
| Tradeoff | Reading the creator ID from the authenticated principal is safer than trusting request JSON. A public lookup may still be required for background operations or explicit referential validation. |
| Owner | Shared Auth / Users module owners. |

Proposed public contract for Activities:

```java
public interface UserDirectory {
    UserReference requireActive(Long userId);
}

public record UserReference(Long id, Set<String> roles, boolean active) {}
```

`creatorUserId` must not be accepted as an unverified client-controlled identity when an authenticated principal is available.

## Cross-schema DBA coordination

| Aspect | Recommendation |
|---|---|
| Brief requirement | The logical model defines references from Persona to Team, Attendance to Activity and Persona, Goal to Persona, and Activity to User. |
| Schema gap | The PDF uses a single MySQL-style schema and does not specify Oracle owners, `REFERENCES` grants, DDL order, or application-user privileges. |
| Impact | Independently created Oracle schemas can compile in isolation but fail when foreign keys, grants, or application startup validation are applied. |
| Proposed solution | Agree canonical owner, table, column, and numeric ID types before creating dependent DDL. Provision referenced schemas first, grant `REFERENCES` to dependent owners, then create foreign keys. Grant the application user only the DML each module needs. |
| Tradeoff | Cross-schema foreign keys provide strong integrity but couple migration order and local setup. If an owner/table name is not finalized, defer only that FK to a coordinated migration; keep application validation and never invent a physical target. |
| Owner | Team-level database coordination, with each module owner responsible for their schema scripts. |

Minimum coordination record for every cross-schema reference:

```text
logical field -> owner.table(column) -> Oracle type -> REFERENCES grant recipient
```

Required references for Pedro's modules are:

- `PERSONA.id_team -> SAL_TEAMS.TEAMS(ID)`; exact Personal owner/table name still requires confirmation.
- `ASISTENCIA.id_actividad -> SAL_ACTIVITIES.ACTIVITIES(ID)`; exact Attendance owner/table name still requires confirmation.
- `META.id_persona -> Personal PERSONA identifier`; physical target requires confirmation.
- `ACTIVIDAD.id_usuario_creador -> Auth USUARIO identifier`; physical target requires confirmation.

## Boundary acceptance checklist

- [ ] Every module keeps its entities and repositories private.
- [ ] Pedro consumes only team-agreed public contracts; examples here are not binding interfaces.
- [ ] IDs, nullability, state meanings, and error behavior are documented by each provider.
- [ ] Personal supports active-person Team counts before Team deletion/count is implemented.
- [ ] Attendance supplies Activity history without owning Activity.
- [ ] Evaluation sources publish normalized indicators before automatic Goal progress is enabled.
- [ ] The team/teacher has resolved the recipient source before Notification integration; Attendance is not a pre-event recipient list.
- [ ] Oracle owners, grants, migration order, and physical FK targets are agreed before integration DDL.

## Next step

Use the [roadmap](pedro-module-roadmap.md) for sequencing. Coordinate unresolved recipient semantics and the necessary public contracts without inventing external entities, repositories, physical table names, or DNI requirements.
