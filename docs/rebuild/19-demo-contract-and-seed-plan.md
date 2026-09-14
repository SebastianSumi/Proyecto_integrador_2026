# Demo contract and synthetic SQL seed plan

## Approved decisions

| Concern | Decision | Evidence / owner boundary |
|---|---|---|
| Teams lifecycle | Logical lifecycle only: `PATCH /api/v1/teams/{id}/state?active=false`; no physical DELETE endpoint. | Teams owns the state transition and preserves references. |
| Activity creator | An Activity creator must be an existing active Persona. | Activities calls public `PersonaService`, never a Personal repository. |
| HTTP success contract | Verified resource-creation endpoints return `201 Created`; unknown target resources return `404`. | Keep operation endpoints such as offline synchronization at `200 OK`. |
| Local configuration | `application.yaml` optionally imports ignored `.env.local`; process environment variables retain higher precedence. | Credentials are never versioned. |
| Demo data | Use manual, idempotent SQL `INSERT` scripts with deterministic synthetic data. | Seed execution is explicit, separate from schema initialization and V001/V002. |

## Verified remediation status

- **W1 – Activity creator:** complete. Personal exposes `validateActivePersona(Long)`; Activities invokes it before agenda locking on create and update.
- **W2 – POST status:** complete for Noticias, Notificaciones and Asistencias. Their creation POST returns `201`; asynchronous/offline synchronization remains `200`.
- **W3 – filter binding:** no defect confirmed. Alertas GET filter has a no-args constructor and setters; Auditoría uses its builder DTO only as POST JSON.
- **W4 – missing resource mapping:** complete for Recomendaciones IA. Missing recommendation detail/confirmation now uses shared `ResourceNotFoundException` and returns `404`.
- **W5 – Team lifecycle:** confirmed logical state transition; no DELETE is required.

## Seed implementation status`r`n`r`nManual, idempotent Oracle seed scripts are prepared under `database/oracle/demo/`. They were statically reviewed but deliberately **not executed**; the next evidence is a manual run against the local Oracle baseline, followed by Swagger checks.`r`n`r`n## Remaining work before live seed verification

1. Update the module/API reference pages with the verified contracts above.
2. Inventory every entity and its insertion prerequisites in dependency order.
3. Design one or more manual SQL scripts under `database/oracle/demo/`, using `INSERT ... SELECT ... WHERE NOT EXISTS` (or equivalent Oracle-safe guards) and only stable synthetic keys.
4. Execute the seed manually against the local Oracle baseline, then verify list/get flows and the Aptitud Física header-detail scenario through Swagger.
5. Run Maven suite, local Oracle `ddl-auto: validate`, Oracle static verifier, and `git diff --check`; record only observed evidence.

## Seed constraints

- The seed must never run on application startup or from Docker initialization.
- It must not execute V001/V002, modify schema, create roles, or overwrite non-synthetic data.
- It must cover each integrated module only when its public contract and SQL prerequisites are known.
- Cross-module scalar IDs must use the seeded parent row; do not invent new foreign keys.
- Metas remain manually managed; the seed must not claim automatic achievement calculation.

## Acceptance checks

- POST creation is `201`; operation endpoints preserve their documented status.
- Missing resources use `404`, validation uses `400`, and business conflicts use `409` when applicable.
- A repeated seed run leaves synthetic record counts stable.
- Aptitud Física persists its header and details atomically; its existing rollback test stays green.
- Full Maven suite, local Oracle validation, static verifier, and `git diff --check` pass before demo claims.

