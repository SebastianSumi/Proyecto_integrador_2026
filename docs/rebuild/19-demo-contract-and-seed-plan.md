# Demo contract and synthetic seed plan

## Approved decisions

| Concern | Decision | Evidence / owner boundary |
|---|---|---|
| Teams lifecycle | Logical lifecycle only: `PATCH /api/v1/teams/{id}/state?active=false`; no physical DELETE endpoint. | `TeamController` and `TeamService` already expose state transition only. Teams owns this contract. |
| Activity creator | An Activity creator must be an existing active Persona. | Activities may call only the public `PersonaService`, never a Personal repository. Personal owns the active-person rule. |
| HTTP success contract | Every current `POST` create endpoint returns `201 Created`; unknown resources return `404`. | Controllers carry `@ResponseStatus(CREATED)` and services use the shared not-found exception. Demo assertions must not expect `200` for creates. |
| Local configuration | Hybrid binding: `application.yaml` optionally imports ignored `.env.local`; process environment variables keep higher precedence. | No credentials are versioned. Compose and Spring share `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`. |
| Demo data | Use idempotent, synthetic data only. No roles, identities, clinical records, or rules not already represented by a module contract may be invented. | Seed work is a separate, manually invoked phase after service contracts close. |

## Current dependency inventory

| Work unit | Owner | Existing dependency | Required direction |
|---|---|---|---|
| W1 — contract evidence | Foundation / API owners | Shared `GlobalExceptionHandler`, controller status annotations | Document and test 201/404/400/409 exactly as advertised. |
| W2 — Team lifecycle | Teams | `TeamController`, `TeamService` | Keep create/list/get/update/state. Do not add DELETE or call it CRUD without qualifying its logical lifecycle. |
| W3 — validate Activity creator | Activities + Personal | `ActividadServiceImpl` currently has scalar `creadorId`; `PersonaService.obtener(Long)` is public | Add a narrow public Personal contract such as `validateActivePersona(Long)`, then inject that service into Activities. Never import `PersonaRepository` into Activities. |
| W4 — Activity and enrollment demo flow | Activities | Activity → Inscripción internal relation; Inscripción already validates Activity via public service | Seed Activity only after its active creator exists; enroll against persisted IDs. Preserve V001/V002 as manual Oracle follow-ups, not seed side effects. |
| W5 — real header-detail demo | Aptitud Física | `EvaluacionAptitudServiceImpl.registrarEvaluacion(...)` and `DetallePruebaFisica` | Seed catalog tests and a valid person first; prove success and rollback with one invalid detail. Aptitud owns aggregate calculations. |
| W6 — remaining module demo records | Each module owner | Their own public services/entities | Seed only required minimal happy-path records and prerequisites; scalar cross-module IDs remain logical unless a public service contract exists. |

**W3 status (2026-09-14):** Personal exposes `validateActivePersona(Long)` and covers active, missing, and inactive outcomes. Activities consumption remains W4 and must be implemented without importing a Personal repository.

## Ordered implementation plan

1. **Contract tests first.** Add/adjust focused MockMvc/service tests that assert 201 for POST and 404 for missing IDs. Correct test expectations, not controllers, where current documented behavior is valid.
2. **Personal public validation.** Personal adds the smallest named public method that confirms existence and active state. Test existing, missing, and inactive Personas.
3. **Activity creator enforcement.** Activities calls that public method inside its create/update transaction boundary before persisting. Test success, missing creator, and inactive creator; ensure no Personal repository dependency appears.
4. **Demo seed design review.** Confirm every module's minimal public creation contract and prerequisites. Decide an explicit seed invocation mechanism and transaction boundaries; do not run at application startup.
5. **Synthetic seed implementation.** Add only idempotent records identifiable by stable synthetic keys. Re-running must reuse/skip matching records and must never overwrite non-synthetic user data.
6. **Oracle demo verification.** With a fresh local volume, apply the baseline, boot with `ddl-auto: validate`, invoke the seed manually, then capture Swagger evidence for CRUD, queries, CORS, logs, 201/404, and the Aptitud rollback.

## Acceptance checks per work unit

- W1: focal HTTP tests pass; response assertions distinguish 201, 204, 400, 404, and 409.
- W2: Team docs and Swagger describe state transition, not DELETE.
- W3/W4: Architecture test or focused unit test proves Activities depends on `PersonaService`, not Personal persistence.
- W5: exactly one aggregate header and its details persist on success; an invalid detail leaves neither persisted.
- W6: a second seed invocation leaves record counts and synthetic keys stable; no non-synthetic rows are changed.
- W7: Maven suite, local Oracle `ddl-auto: validate`, static Oracle verifier, and `git diff --check` pass before demo evidence is claimed.

## Explicit deferrals

- No role/authorization data is seeded because the current module contracts do not define it.
- No V001/V002 execution is hidden inside seeds.
- No cross-module foreign key is introduced for scalar IDs.
- No automatic achievement calculation is added to Metas.
