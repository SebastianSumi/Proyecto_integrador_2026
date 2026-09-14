# Synthetic demo seed area

This directory is reserved for the future, manually invoked, idempotent synthetic demo seed. It is intentionally empty in planning phase.

The seed must follow [`docs/rebuild/19-demo-contract-and-seed-plan.md`](../../../docs/rebuild/19-demo-contract-and-seed-plan.md): it must not run during application startup, execute V001/V002, create roles, or alter non-synthetic data. The active schema baseline remains `../local/`; this directory is not mounted by Docker initialization.
