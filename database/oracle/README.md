# Oracle database assets

This directory is the single entry point for Oracle assets used by Saludablemente. It separates reproducible local bootstrap from controlled, manual database changes.

## Current authoritative paths

| Path | Purpose | Execution mode |
|---|---|---|
| `local/` | Reproducible Oracle Free environment derived from the current JPA mappings. | Docker local only. |
| `local/init/` | Initialization assets mounted by `compose-dev.yml`. | Runs only on a fresh local volume. |
| `manual-migrations/` | Oracle-specific changes that need data/precondition review. | Manual, authorized execution only. |

`local/CANONICAL_JPA_SCHEMA_INVENTORY.md` records the JPA-derived local baseline. `local/README.md` is the operational runbook.

## Why bootstrap uses shell and SQL files

The bootstrap has two responsibilities:

- Shell scripts (`.sh`) orchestrate the process: create users, select the target schema, execute assets in a deterministic order and fail fast when Oracle returns an error.
- SQL files define database objects: tables, constraints, sequences and grants.

The shell layer never contains the schema definition itself. The SQL layer never contains passwords. Passwords are supplied at runtime from ignored `.env.local` values.

## Target organization

The current `local/init/` layout remains active until it is migrated and revalidated. The intended final layout is:

```text
database/oracle/
├── bootstrap/
│   ├── scripts/       # orchestration only (.sh)
│   └── sql/           # baseline schema definition only (.sql)
├── migrations/        # reviewed, manual deltas (V###__description.sql)
└── README.md
```

A `.sql.template` suffix is temporary compatibility naming. It may be renamed to `.sql` only after confirming that its content does not require variable substitution; execution order must remain unchanged.

## Legacy and duplicate assets

The repository also contains historical SQL under root `oracle/` and `src/main/resources/db/`. They are not mounted by Compose, not loaded by Spring Boot and are not the local Oracle source of truth. They contain older mappings and must not be executed alongside `local/init/`.

They will be archived only after references are updated and a fresh local Oracle validation succeeds. This preserves academic history while preventing two conflicting schema definitions.

## Safety rules

- Do not commit `.env.local`, passwords, dumps or local database volumes.
- Do not run `manual-migrations/` automatically.
- Recreate the local Docker volume after changing bootstrap users, credentials or baseline SQL.
- Run the backend with `ddl-auto: validate`; application startup must not create production-like schema objects.