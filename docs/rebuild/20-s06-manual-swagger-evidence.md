# S06: manual Swagger evidence guide

> This guide is for the presenter. It does not execute requests, mutate data, or claim S06 is already passed. Capture only results actually observed in the presentation environment.

## 1. Prepare local demo data

The seed is manual, idempotent, synthetic, and never runs from Docker or Spring Boot.

1. Start the local Oracle baseline following [database/oracle/local/README.md](../../database/oracle/local/README.md).
2. Configure the ignored `.env.local` with the application account values described there. Do not show it or credentials in captures.
3. Connect as `SALUDABLEMENTE_APP`, never `SYS` or a schema owner.
4. From the repository root, run this exact SQL*Plus order:

```sql
@database/oracle/demo/00-preflight.sql
@database/oracle/demo/10-core-personal.sql
@database/oracle/demo/20-evaluaciones.sql
@database/oracle/demo/30-actividades-metas.sql
@database/oracle/demo/40-salud-personal.sql
@database/oracle/demo/50-alertas-recomendaciones.sql
@database/oracle/demo/60-reportes-exportacion.sql
@database/oracle/demo/90-operacional-opcional.sql
```

5. Preserve the successful preflight and final `COMMIT` output as **local Oracle** evidence. Re-running the sequence must not duplicate data.

The scripts create fictional data for Teams, Personas, evaluations, activities, inscriptions, metas, alerts, recommendations and report modules. They create no users, tables, migrations, credentials or audit records. See [database/oracle/demo/README.md](../../database/oracle/demo/README.md) for boundaries.

## 2. Start and prove the backend

From the repository root:

```powershell
./mvnw.cmd test
./mvnw.cmd spring-boot:run
```

Open `http://localhost:<server-port>/swagger-ui/index.html`; use the actual port printed by Spring Boot (normally `8080`, unless `SERVER_PORT` overrides it).

Capture: startup line with active datasource, Swagger home/API title, and `GET /actuator/health` returning `200`.

Do not call local Oracle team BD2 evidence. BD2 requires the same proof again with the authorized shared datasource.

## 3. Obtain runtime IDs before every write

Never invent IDs. In Swagger, first execute the matching `GET` and copy IDs returned by this runtime:

- `GET /api/v1/teams`
- `GET /api/v1/actividades`
- `GET /api/v1/metas`
- `GET /api/v1/evaluaciones-aptitud`
- the catalog-prueba endpoint displayed in Swagger when registering Aptitud.

The placeholders `<personaId>`, `<catalogoPruebaId>`, `<actividadId>` and `<evaluacionId>` below mean values just retrieved through a successful `GET`.

## 4. S06 checklist

### C1: backend, Oracle, REST, DTO and OpenAPI

1. Show the Spring Boot log and Swagger UI.
2. Execute `GET /api/v1/teams` and show `200`.
3. Expand `POST /api/v1/teams` and show `TeamRequest`, not a JPA entity.
4. Capture startup, endpoint documentation, and an Oracle tool window with service/user only (never a password).

State: “This is local Oracle evidence.” Say “BD2” only after the shared-datasource repeat.

### C2: CRUD, validation, exception, logs and tests

Use Teams for a compact CRUD:

```json
{
  "name": "DEMO S06 Team",
  "description": "Synthetic record created during the manual demonstration"
}
```

1. `POST /api/v1/teams` -> `201`; copy `<teamId>`.
2. `GET /api/v1/teams/<teamId>` -> `200`.
3. `PUT /api/v1/teams/<teamId>` with changed description -> `200`.
4. `PATCH /api/v1/teams/<teamId>/state` with the exact Swagger request schema -> documented success.
5. Repeat the POST with `"name": ""` -> `400`.
6. `GET /api/v1/teams/<unused-large-id>` -> `404`.
7. Capture terminal `INFO` after the valid write and `WARN` after the expected error, without request bodies or secrets.
8. Capture the completed `mvn test` summary.

Do not delete Teams: the supported lifecycle is a state transition, not HTTP `DELETE`.

### C3: ORM association, related DTO and navigation

1. From `GET /api/v1/actividades`, copy an `<actividadId>` with seeded inscriptions.
2. Execute `GET /api/v1/actividades/<actividadId>/detalle`.
3. Capture activity plus inscription detail DTOs without JSON recursion.

Explain this is internal ORM `Actividad -> Inscripcion`; cross-module scalar IDs are not presented as ORM associations.

### C4: header-detail, calculation, commit and rollback

The S06 aggregate is `EvaluacionAptitud -> DetallePruebaFisica`, not Activity-Inscription.

**Swagger success**

1. Obtain a real `<personaId>` and two active `<catalogoPruebaId>` values.
2. Execute `POST /api/v1/evaluaciones-aptitud` using this body shape:

```json
{
  "idPersona": <personaId>,
  "fechaRegistro": "2026-09-14",
  "sincronizado": true,
  "detalles": [
    { "idPrueba": <catalogoPruebaId>, "valorObtenido": 10.00, "puntajeParcial": 18.00 },
    { "idPrueba": <catalogoPruebaId>, "valorObtenido": 12.00, "puntajeParcial": 16.00 }
  ]
}
```

3. Expected `201`; show header ID, two details, calculated `puntajeGlobal` and `diagnosticoAptitud`.
4. `GET /api/v1/evaluaciones-aptitud/<evaluacionId>` and capture the persisted aggregate.

**Rollback proof**

Swagger cannot force the deliberate post-registration exception of the transactional integration test. A client validation `400` is **not** rollback proof. Run and capture:

```powershell
./mvnw.cmd '-Dtest=EvaluacionAptitudTransactionalIntegrationTest' test
```

The test verifies an exception after registration leaves header and details at zero. It is H2 local evidence unless the team creates an approved Oracle test setup. In Swagger, a nonexistent `idPrueba` should yield `409`; compare the list before/after to show no aggregate was created, but describe it only as API-boundary evidence.

### C5: filters, ordering, aggregate/report and CORS

**Activities**

1. Run and capture:

```text
GET /api/v1/actividades/busqueda?estado=PROGRAMADA&desde=2026-01-01&hasta=2026-12-31&sort=fecha,asc
GET /api/v1/actividades/resumen?desde=2026-01-01&hasta=2026-12-31
```

Show the filtered ordering and aggregate response.

**Aptitud projection and aggregate**

1. `GET /api/v1/evaluaciones-aptitud/resumen?personaId=<personaId>`.
2. `GET /api/v1/evaluaciones-aptitud/agregados?personaId=<personaId>`.
3. Capture both: the first is a light projection; the second is an aggregate.

**CORS**

Swagger alone cannot prove browser CORS because it is same-origin. With the active runtime port, capture these three PowerShell requests:

```powershell
Invoke-WebRequest -Method Options -Uri http://localhost:<server-port>/api/v1/teams -Headers @{ Origin = 'http://localhost:4200'; 'Access-Control-Request-Method' = 'GET' }
Invoke-WebRequest -Uri http://localhost:<server-port>/api/v1/teams -Headers @{ Origin = 'http://localhost:4200' }
Invoke-WebRequest -Uri http://localhost:<server-port>/api/v1/teams -Headers @{ Origin = 'http://evil.example' }
```

The configured origin receives the applicable CORS header. The unapproved origin is rejected or lacks authorization according to the active policy. Do not alter CORS settings during presentation.

### C6: sustentation order

1. Scope: own modules and REST contracts.
2. Rule: same-place/same-date activity scheduling or explicit Meta completion.
3. Boundary: Inscripcion calls public Actividad service, not another module repository.
4. Transaction: Aptitud header-detail calculations and focused rollback test.
5. Honest limit: local Oracle is not BD2; V001/V002 are inactive unless manual execution is independently recorded.

## 5. Screenshot inventory

1. Backend startup plus health.
2. Swagger overview plus DTO schema.
3. CRUD `201`, invalid `400`, missing `404`.
4. Activity detail with inscription DTOs.
5. Aptitud aggregate with calculated fields.
6. Focused transaction test passing.
7. Filter plus aggregate output.
8. CORS allowed preflight/response and rejected-origin result.
9. Maven test summary and Spring Modulith test output, if run.
10. Oracle service/user and seeded rows; mask passwords.

## 6. Outside this local guide

- Repeat runtime proof with authorized shared BD2 datasource.
- Record V001/V002 execution before claiming concurrent duplicate/overlap protection.
- Confirm shared CORS origin and credentials policy with the team.
- Never fabricate audit, export, webhook, role or security-flow records for screenshots.

For the honest evaluation status, read [S06 traceability](18-s06-evaluation-traceability.md).
