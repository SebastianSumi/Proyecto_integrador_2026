# Saludablemente backend: next iteration guide

The local backend is ready to run against Oracle and exposes the first completed module: **Equipos**. This guide gives the next developer a short, repeatable path without creating data unintentionally.

## Quick path

1. Start the local Oracle container: `docker compose -f database/docker/compose-dev.yml up -d`.
2. Confirm it is healthy: `docker compose -f database/docker/compose-dev.yml ps`.
3. Start the backend with the `dev` profile: `./mvnw.cmd spring-boot:run`.
4. Open Swagger at `http://localhost:8080/swagger-ui/index.html` and perform the read-only checks below.

The local database listener is `localhost:1522`; the dedicated PDB is `SALUDPDB`. This is separate from the instructor's BomERP instance.

## Local Oracle setup

The first provision uses three ordered scripts under `database/oracle/`:

| Order | Script | Responsibility |
|---|---|---|
| 1 | `01_provision_users.sql` | Creates schema owner `SAL_TEAMS` and DML user `SALUDABLEMENTE_APP`. |
| 2 | `02_create_teams.sql` | Creates `SAL_TEAMS.TEAMS`, including named constraints and the `ACTIVE` `0/1` check. |
| 3 | `03_grant_teams_access.sql` | Grants only Team-table DML to the application user. |

Run script 1 as a PDB SYSDBA, script 2 as `SAL_TEAMS`, and script 3 as `SAL_TEAMS`. Use the local passwords configured for this course environment; never reuse them outside local development.

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
| Personal | Owns the future `Persona -> Team` foreign key and controlled `equipoId` filter. | Must be implemented by the Personal owner. |
| Activities | Must become Pedro's transactional header operation. | Requires an agreed integration contract with Asistencias for detail records and rollback tests. |

`@EntityGraph` is deliberately deferred until a DTO actually traverses `Persona -> Team` and query evidence shows an N+1 fetch. Do not make relationships eager by default.

## Verified baseline

- `./mvnw.cmd test`: all current tests pass.
- Spring Modulith verification remains enabled.
- Hibernate validates the Oracle schema at startup.
- `BooleanToIntegerConverter` maps Java `boolean` to Oracle `NUMBER(1)` values `0/1` while the REST contract remains `activo: true|false`.

## Next step

Before adding another module, agree its ownership and REST/DB boundary. The next meaningful business slice is the coordinated **Activities–Asistencias** transactional flow, not duplicate Team features.
