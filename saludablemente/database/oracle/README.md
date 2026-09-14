# Oracle layout

The repository has one executable local baseline and one controlled delta path:

- `local/` is the active Docker Oracle Free bootstrap. Its `init/` directory stays flat because the image executes entrypoint files from that mounted directory. `local/CANONICAL_JPA_SCHEMA_INVENTORY.md` is the source of truth for its current JPA-aligned DDL.
- `manual-migrations/` contains reviewed, manually applied Oracle deltas. They are never mounted into Docker initialization and require owner/DBA authorization.
- `legacy/` is historical evidence only. It is not an initialization source, Flyway location, Spring SQL-init location, or deployment artifact.

Do not add another executable DDL source without an explicit team decision. Update `local/` for a new disposable baseline and add a reviewed file under `manual-migrations/` for a controlled change to an existing database.
