# Oracle local reproducible environment

This folder provisions a disposable **local** Oracle Free database from the current JPA inventory. It is an integration harness, not BD2 and not a production deployment.

## Quick path

1. Copy `.env.example` to `.env.local` and replace every password locally. Keep simple local passwords without quotes or shell-special characters because the initialization shell passes them to SQL*Plus.
2. Start a fresh database: `docker compose --env-file .env.local -f compose-dev.yml up -d oracle`.
3. Wait until `docker compose --env-file .env.local -f compose-dev.yml ps` reports `healthy`.
4. Start the backend with `SPRING_PROFILES_ACTIVE=local-oracle` and the same `DB_URL`, `DB_USERNAME` and `DB_PASSWORD` values. Hibernate uses `ddl-auto: validate`; it never creates tables.
5. Stop without deleting data: `docker compose --env-file .env.local -f compose-dev.yml down`.

## Reset

A reset destroys only the local named volume and all local test data:

```powershell
docker compose --env-file .env.local -f compose-dev.yml down -v
```

Run it only when a fresh schema is intended. The init scripts run only for a new volume and preserve a nonempty volume.

## What the harness creates

- Ten owner schemas from `CANONICAL_JPA_SCHEMA_INVENTORY.md`.
- The least-privilege runtime user supplied through `DB_USERNAME`/`DB_PASSWORD`.
- All currently mapped tables, identity keys, `RAW(16)` UUID keys, composite keys, same-schema FKs and `SEQ_ACCION_CORRECTIVA`.
- No FK for scalar cross-module IDs.

The runtime account receives only `CREATE SESSION`, DML on mapped tables and `SELECT` on the mapped sequence. It never receives `DBA`.

## Deliberately manual follow-up

`../manual-migrations/V001__enrollment_active_uniqueness.sql` then `V002__activity_schedule_coordination.sql` remain manual. Do not mount or execute them automatically: they require data/precondition review and are the Oracle-specific protection for concurrent enrollment and schedule creation.

After V002 succeeds, run `../manual-migrations/V002__activity_schedule_runtime_grant.sql` manually as the owner/DBA. It grants the separate runtime account permission to call `LOCK_AGENDA_ACTIVIDAD`; replace its default grantee if `DB_USERNAME` differs from `SALUDABLEMENTE_APP`.

## Deferred runtime validation

- `spring-modulith-starter-jpa` is present. Before claiming a fully validated Oracle runtime, decide whether its persistent publication registry is enabled; create and grant its exact table only if the final configuration requires it.
- `NoticiaEntity.contenido` and `NotificacionEntity.mensaje` currently declare `TEXT` while Oracle local DDL uses `CLOB`. Do not change the DDL to `TEXT`; prove `ddl-auto: validate` and align the mappings to `@Lob`/`CLOB` if Hibernate validates the literal definition.

## Boundaries

- Do not commit `.env.local`, passwords, container dumps or Oracle data.
- This proves a local Oracle runtime only after the container and `ddl-auto: validate` boot are executed. It does not prove BD2 access.
- The real S06 header-detail aggregate is `EvaluacionAptitud -> DetallePruebaFisica`; no artificial composite operation is added here.
- The mapped `Map<String,Object>` / `CLOB` recommendation detail still requires Hibernate runtime proof before it can be claimed as validated.
