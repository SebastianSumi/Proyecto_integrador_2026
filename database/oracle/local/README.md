# Oracle local reproducible environment

This folder provisions a disposable **local** Oracle Free database from the current JPA inventory. It is an integration harness, not BD2 and not a production deployment.

## Quick path

1. Run the interactive helper from the repository root: `./scripts/setup-local-oracle.ps1`. It prompts without echoing passwords, validates identifiers/ports, and writes the Git-ignored `.env.local`. Keep passwords simple (letters, digits, underscore) because initialization passes them to SQL*Plus.
2. Start a fresh database: `docker compose --env-file .env.local -f compose-dev.yml up -d oracle`.
3. Wait until `docker compose --env-file .env.local -f compose-dev.yml ps` reports `healthy`.
4. Load only the runtime datasource values into the current PowerShell session; Spring Boot does not read Docker Compose's `.env.local` automatically:

   ```powershell
   Get-Content .env.local | Where-Object { $_ -match '^(DB_URL|DB_USERNAME|DB_PASSWORD)=' } | ForEach-Object {
     $key, $value = $_ -split '=', 2
     Set-Item -Path "Env:$key" -Value $value
   }
   $env:SPRING_PROFILES_ACTIVE = 'local-oracle'
   mvn spring-boot:run
   ```

   Hibernate uses `ddl-auto: validate`; it never creates tables.
5. Stop without deleting data: `docker compose --env-file .env.local -f compose-dev.yml down`.

## Reset

A reset destroys only the local named volume and all local test data:

```powershell
docker compose --env-file .env.local -f compose-dev.yml down -v
```

Run it only when a fresh schema is intended. The init scripts run only for a new volume and preserve a nonempty volume. Changing `ORACLE_PASSWORD`, `DB_USERNAME`, `DB_PASSWORD`, or `LOCAL_ORACLE_OWNER_PASSWORD` after first initialization requires this reset before `up`; changing only `ORACLE_PORT` does not.

## What the harness creates

- Ten owner schemas from `CANONICAL_JPA_SCHEMA_INVENTORY.md`.
- The least-privilege runtime user supplied through `DB_USERNAME`/`DB_PASSWORD`.
- `SALUDABLEMENTE_APP.EVENT_PUBLICATION`, owned by the runtime account for Spring Modulith's current JPA publication mode.
- All currently mapped tables, identity keys, `RAW(16)` UUID keys, composite keys, same-schema FKs and `SEQ_ACCION_CORRECTIVA`.
- No FK for scalar cross-module IDs.

The runtime account receives only `CREATE SESSION`, DML on mapped tables and `SELECT` on the mapped sequence. It never receives `DBA`.

## Deliberately manual follow-up

`../manual-migrations/V001__enrollment_active_uniqueness.sql` then `V002__activity_schedule_coordination.sql` remain manual. Do not mount or execute them automatically: they require data/precondition review and are the Oracle-specific protection for concurrent enrollment and schedule creation.

After V002 succeeds, run `../manual-migrations/V002__activity_schedule_runtime_grant.sql` manually as the owner/DBA. It grants the separate runtime account permission to call `LOCK_AGENDA_ACTIVIDAD`; replace its default grantee if `DB_USERNAME` differs from `SALUDABLEMENTE_APP`.

## Deferred runtime validation

- `spring-modulith-starter-jpa` requires `EVENT_PUBLICATION` in the runtime schema and the harness provisions it. If the team switches Modulith completion mode to archive, add and validate `EVENT_PUBLICATION_ARCHIVE` before enabling that mode.
- `NoticiaEntity.contenido` and `NotificacionEntity.mensaje` map with `@Lob`, while Oracle local DDL uses `CLOB`. The mapping is aligned and was validated by a successful `ddl-auto: validate` Oracle local boot on 2026-09-14.

## Boundaries

- Do not commit `.env.local`, passwords, container dumps or Oracle data.
- This proves a local Oracle runtime only after the container and `ddl-auto: validate` boot are executed. It does not prove BD2 access.
- The real S06 header-detail aggregate is `EvaluacionAptitud -> DetallePruebaFisica`; no artificial composite operation is added here.
- The mapped `Map<String,Object>` / `CLOB` recommendation detail was included in the successful Hibernate runtime validation on 2026-09-14.

## Verified local runtime evidence (2026-09-14)

Oracle Free 23.5 started healthy at the local mapped port. The backend started with profile local-oracle and ddl-auto: validate, initialized the JPA EntityManagerFactory, and returned 200 from /actuator/health, /v3/api-docs, and /api/v1/teams. This is local Oracle evidence only; it does not prove BD2, manual V001/V002, or a concurrent Oracle workload.
