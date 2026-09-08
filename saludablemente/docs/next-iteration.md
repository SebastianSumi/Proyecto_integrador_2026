# Saludablemente backend: next iteration guide

The local backend is ready to run against Oracle and exposes **Equipos** and the Activity header core. This operational guide gives the next developer a short, repeatable path without creating data unintentionally.

For requirement traceability, implementation status, evidence, and phased work units across Pedro's scope, see the [Pedro Module Roadmap](pedro-module-roadmap.md). External module boundaries remain in [External Module Recommendations](external-module-recommendations.md).

## Shared logical-model alignment

`saludablemente_schema.pdf` is the team's logical model, not executable Oracle DDL. Each module owns its Oracle schema and may use implementation-oriented English names as long as the attribute meaning and identifiers remain stable across module boundaries.

| Shared logical attribute | Oracle physical attribute | Java / REST contract | Decision |
|---|---|---|---|
| `TEAM.id_team` | `SAL_TEAMS.TEAMS.ID NUMBER` identity | `Team.id` / `id` | Canonical Team identifier. Personal must reference this value as `equipoId`. |
| `TEAM.nombre VARCHAR(60)` | `NAME VARCHAR2(100 CHAR)` | `name` / `nombre` | Wider Oracle limit retained; semantics are unchanged. |
| `TEAM.descripcion VARCHAR(200)` | `DESCRIPTION VARCHAR2(500 CHAR)` | `description` / `descripcion` | Wider Oracle limit retained; semantics are unchanged. |
| `TEAM.activo BOOLEAN` | `ACTIVE NUMBER(1)` plus `CHECK (ACTIVE IN (0, 1))` | `active` / `activo` | Oracle-safe boolean mapping through `BooleanToIntegerConverter`. |

The physical reference target for the future Personal module is `SAL_TEAMS.TEAMS(ID)`. Personal owns `PERSONA.id_team` (exposed publicly as `equipoId`) and must not place Person entities or repositories inside Teams. Cross-module application access must go through an explicitly exposed public service when Personal is implemented; direct repository access is forbidden.

## Quick path

1. Start the local Oracle container: `docker compose -f database/docker/compose-dev.yml up -d`.
2. Confirm it is healthy: `docker compose -f database/docker/compose-dev.yml ps`.
3. Start the backend with the `dev` profile: `./mvnw.cmd spring-boot:run`.
4. Open Swagger at `http://localhost:8080/swagger-ui/index.html` and perform the read-only checks below.

The local database listener is `localhost:1522`; the dedicated PDB is `SALUDPDB`. This is separate from the instructor's BomERP instance.

## Local Oracle setup

The repository contains seven versioned Oracle scripts. The `SAL_ACTIVITIES` owner must be provisioned separately by the local DBA before running its DDL; schema credentials are not stored in Git.

| Order | Script | Responsibility |
|---|---|---|
| 1 | `01_provision_users.sql` | Creates schema owner `SAL_TEAMS` and DML user `SALUDABLEMENTE_APP`. |
| 2 | `02_create_teams.sql` | Creates `SAL_TEAMS.TEAMS`, including named constraints and the `ACTIVE` `0/1` check. |
| 3 | `03_grant_teams_access.sql` | Grants only Team-table DML to the application user. |
| 5 | `05_create_activities.sql` | Creates Activities and the place-lock table used for concurrency-safe scheduling. |
| 6 | `06_grant_activities_access.sql` | Grants only the required Activity-table DML to the application user. |
| 7 | `07_create_activity_enrollments.sql` | Adds the accepted Activities-local enrollment relationship, lifecycle checks, indexes, and Activity foreign key. |
| 8 | `08_grant_activity_enrollment_access.sql` | Grants enrollment read/write access without physical-delete permission. |

Run script 1 as a PDB SYSDBA, scripts 2 and 3 as `SAL_TEAMS`, and scripts 5 through 8 as `SAL_ACTIVITIES`. Existing installations that already completed scripts 1 through 6 apply only scripts 7 and 8; no reset is required. Provision local credentials outside version control and never reuse them outside local development.

## Activities REST contract

Base path: `/api/v1/actividades`. Public date and time fields remain separate while Oracle stores safe `START_AT` and `END_AT` timestamps.

| Method | Endpoint | Purpose |
|---|---|---|
| `GET` | `/api/v1/actividades` | List all activities. |
| `GET` | `/api/v1/actividades?estado=PROGRAMADA` | Filter by public Spanish state. |
| `GET` | `/api/v1/actividades/{id}` | Get one activity. |
| `POST` | `/api/v1/actividades` | Create a scheduled activity. |
| `PUT` | `/api/v1/actividades/{id}` | Edit details while the activity is scheduled. |
| `PATCH` | `/api/v1/actividades/{id}/estado` | Set the desired lifecycle state explicitly. |
| `GET` | `/api/v1/actividades/{id}/inscripciones` | List active enrollments; add `?incluirCanceladas=true` for history. |
| `POST` | `/api/v1/actividades/{id}/inscripciones` | Enroll one Person ID while the Activity is scheduled. |
| `POST` | `/api/v1/actividades/{id}/inscripciones/lote` | Enroll an atomic batch of at most 100 Person IDs. |
| `DELETE` | `/api/v1/actividades/{id}/inscripciones/{personaId}` | Cancel logically and idempotently; no history row is deleted. |

States are `PROGRAMADA`, `EN_CURSO`, `FINALIZADA`, and `CANCELADA`. Valid progress is Programada -> En curso -> Finalizada; Programada or En curso may become Cancelada. Repeating the current state is idempotent.

The scheduling transaction normalizes the place, locks its coordination row, and rejects overlap when `newStart < existingEnd` and `newEnd > existingStart`. Cancelled activities are ignored and contiguous schedules are allowed. `usuarioCreadorId` remains a stable external ID until Auth exposes its public validation contract.

Enrollment is an accepted Activities-owned correction for the brief's undefined "inscritos" recipient source; neither PDF defines its entity or flow. It accepts positive `personaId` values and requires the Activity to remain `PROGRAMADA`. A duplicate active enrollment returns a conflict. Re-enrolling a cancelled person reactivates the same unique Activity/Person row and replaces its latest enrollment/cancellation timestamps; it does not preserve every enrollment episode. Person existence/active status is intentionally deferred to Personal's future public directory contract; Activities never accesses Personal persistence.

## Equipos REST contract

Base path: `/api/v1/equipos`. The public contract is Spanish; Java package and type names remain English internally.

| Method | Endpoint | Purpose |
|---|---|---|
| `GET` | `/api/v1/equipos` | List all teams. |
| `GET` | `/api/v1/equipos?activo=true` | List active teams; use `false` for inactive teams. |
| `GET` | `/api/v1/equipos/{id}` | Get one team by its own identifier. |
| `POST` | `/api/v1/equipos` | Create a team. |
| `PUT` | `/api/v1/equipos/{id}` | Replace its name and description. |
| `PATCH` | `/api/v1/equipos/{id}/estado` | Set the requested active state explicitly. |

There is intentionally no `?id` filter: the canonical single-resource route is `/api/v1/equipos/{id}`. A foreign-key filter belongs to a dependent module, for example future `GET /api/v1/personas?equipoId=...`.

### Request and response examples

```json
{
  "nombre": "Nutrición",
  "descripcion": "Equipo de apoyo nutricional"
}
```

```json
{
  "id": 1,
  "nombre": "Nutrición",
  "descripcion": "Equipo de apoyo nutricional",
  "activo": true
}
```

To change state, send an explicit desired state rather than a toggle:

```json
{ "activo": false }
```

Sending the same request again leaves the entity in the same state, which makes the operation idempotent and safe for a frontend button whose label changes from “Desactivar” to “Activar”.

## Read-only verification

| Check | URL | Expected result |
|---|---|---|
| Liveness/readiness | `http://localhost:8080/actuator/health` | JSON with `status: "UP"`. |
| OpenAPI | `http://localhost:8080/v3/api-docs` | HTTP `200` and the `Equipos` tag. |
| Swagger | `http://localhost:8080/swagger-ui/index.html` | Interactive Spanish contract. |
| Teams | `http://localhost:8080/api/v1/equipos` | HTTP `200`, possibly an empty array. |
| Active filter | `http://localhost:8080/api/v1/equipos?activo=true` | HTTP `200`, only active records. |

## Responsibilities and deferred work

| Area | Current responsibility | Deferred because it belongs to another module/iteration |
|---|---|---|
| Teams | Team CRUD, state management, Team Oracle schema. | Person-Team navigation. |
| Personal | Owns the future `Persona -> Team` foreign key and controlled `equipoId` filter. | Must expose an active-person count/query contract so Teams can enforce deletion and return counts without accessing Personal's repository. |
| Activities | Owns the Activity header, lifecycle, overlap rule, Oracle schema, and accepted Enrollment correction. | Notification delivery still requires an external module contract. |
| Goals | Owns `META`; references Persona by identifier. | Must consume public Personal and evaluation contracts instead of their repositories. |

### Observed gaps that are intentionally deferred

- **Team deletion and active-person count:** the brief requires blocking deletion when active people are assigned and listing that count. Implementing either now would require inventing Personal data or returning a false zero. Add both only after Personal exposes an active-person query. Until then, Teams supports inactivation but no `DELETE` endpoint.
- **Activity notifications:** "inscritos" is not a defined entity or workflow in either PDF. Activities-owned Enrollment now provides the accepted pre-event recipient source. Notification delivery remains external, and Attendance cannot define pre-event recipients.
- **Attendance ownership:** Asistencias belongs to Francisco and references Pedro's Activity ID. It must not map or mutate Activity through an Asistencia repository; orchestration uses the Activity module's public service.
- **Goal progress:** `META.tipo_meta` identifies the health indicator only as free text. Automatic progress updates require a stable indicator vocabulary and a public evaluation event/query contract. Goals must not read Nutrition or Physical Fitness repositories directly.
- **Goal completion direction:** “reaching the target” is ambiguous for indicators where lower is better versus higher is better. Define comparison direction per indicator before automatic completion is implemented.
- **Cross-schema foreign keys:** physical Oracle FKs and grants require coordination between schema owners. Application-level public services remain necessary for business rules even when a database FK exists.

`@EntityGraph` is deliberately deferred until a DTO actually traverses `Persona -> Team` and query evidence shows an N+1 fetch. Do not make relationships eager by default.

## Verified baseline

- Historical test counts and scope are recorded in the roadmap; run `./mvnw.cmd test` when fresh verification is needed.
- Spring Modulith verification remains enabled.
- Hibernate validates the Oracle schema at startup.
- `BooleanToIntegerConverter` maps Java `boolean` to Oracle `NUMBER(1)` values `0/1` while the REST contract remains `activo: true|false`.

## Next step

Follow the [Pedro Module Roadmap](pedro-module-roadmap.md) for current priorities and unresolved integrations. This guide describes operating the accepted baseline; new behavior still requires its own authorized work unit.
