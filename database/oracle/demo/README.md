# Synthetic demo SQL seed

These scripts insert deterministic, fictional data for local Swagger demonstrations. They are **manual**, idempotent, and never run from Docker or Spring Boot.

## Prerequisites

- The local Oracle baseline in `../local/` is running and the backend starts with `ddl-auto: validate`.
- Connect as `SALUDABLEMENTE_APP`. The account receives the required runtime grants from the local baseline.
- Do not use a schema-owner or `SYS` account for the seed.
- No password belongs in these files. Use the local environment configuration already documented for the project.

## Execution order

Run each file manually, in this exact order:

1. `00-preflight.sql` (read-only prerequisite check)
2. `10-core-personal.sql`
3. `20-evaluaciones.sql`
4. `30-actividades-metas.sql`
5. `40-salud-personal.sql`
6. `50-alertas-recomendaciones.sql`
7. `60-reportes-exportacion.sql`
8. `90-operacional-opcional.sql` (read-only boundary statement; no fixture is inserted)

### SQL*Plus

Connect using the project-local connection string and the `SALUDABLEMENTE_APP` account, then run:

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

### IntelliJ Database tool window

Create a data source with the same host, port, service and `SALUDABLEMENTE_APP` account used by the backend. Open one script at a time and use **Run**. Keep the listed order and verify the `COMMIT` completes before continuing.

## Safety boundaries

- Every insert is guarded with `NOT EXISTS`; a second run preserves the same synthetic data.
- Scripts contain no DDL, grants, users, roles, migrations, credentials, or V001/V002 execution.
- Values are invented and prefixed or described as `DEMO`/`synthetic` where practical.
- Metas are fixtures only; they do not claim automatic goal completion.
- Operational audit, security, webhook, download and interoperability records are deliberately not fabricated. Their owners must create them through a real, explicitly approved flow.

## Demo coverage

The seed provides a Team, two Personas, preferences and credential; Aptitud Física and Nutricional header-detail data; an Activity with an Inscripción and attendance; one Meta; a News item and Notification; an Alert with detail; one Recommendation with detail; a personal report, report queue item and export task.