# Synthetic demo SQL seed

This directory contains **manual** Oracle SQL seed scripts for deterministic, synthetic demonstration data. It is not mounted by Docker and is never executed automatically by Spring Boot.

## Execution contract

1. Start the local Oracle baseline from `../local/` and run the backend with `ddl-auto: validate`.
2. Connect as the application schema user documented in the local setup guide.
3. Execute seed scripts explicitly in their documented order.
4. Re-run safely: every script must preserve existing synthetic rows and never overwrite non-synthetic data.

## Boundaries

- Scripts insert data only; they do not create schemas, tables, users, roles, grants, or migrations.
- V001/V002 remain manual concurrency migrations under `../manual-migrations/` and are not seed side effects.
- Use fictitious, stable values only. Do not commit credentials or personal data.
- The first implementation follows the dependency inventory in [`docs/rebuild/19-demo-contract-and-seed-plan.md`](../../../docs/rebuild/19-demo-contract-and-seed-plan.md).
