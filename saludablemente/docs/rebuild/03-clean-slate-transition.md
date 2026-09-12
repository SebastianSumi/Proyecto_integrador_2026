# Transición limpia: estado ejecutado y límites vigentes

**Estado: limpieza filesystem EJECUTADA y retenida por decisión explícita del usuario.** No se restaurará el respaldo automáticamente. Oracle permanece sin ejecución: no hubo provisioning, usuarios, schemas, tablas, grants, migraciones, DDL ni DML.

## Alcance exacto de la limpieza ejecutada

La evidencia Git confirma que se eliminaron del worktree únicamente los activos legacy aprobados:

- Producción Java de `src/main/java/pe/edu/upeu/saludablemente/`: módulos `actividades`, `teams`, `configuration`, `exception`, `filter` y las clases raíz legacy asociadas.
- Pruebas Java legacy bajo `src/test/java/pe/edu/upeu/saludablemente/`.
- Recursos legacy: `application-dev.yaml`, `application.yaml`, `logback-spring.xml` y `META-INF/spring.factories`.
- Docker Compose legacy: `database/docker/compose-dev.yml`.
- Scripts Oracle legacy `01` a `08` y `database/oracle/migrations/team_identity_05/`.

No se eliminaron `.env`, Git, wrapper Maven, `pom.xml`, documentación de reconstrucción, fuentes académicas ni respaldo. La limpieza no autoriza otras eliminaciones.

## Respaldo y recuperación

| Evidencia | Valor confirmado |
|---|---|
| Ubicación | `C:/Users/pfloa/Backups/saludablemente-controlled-rebuild-20260910-175559` |
| Contenido | 66 entradas de respaldo pre-limpieza. |
| Integridad | Validación SHA-256: **PASS**. |
| Uso | Solo recuperación manual ante decisión explícita del usuario. |

**Regla:** no restaurar ni copiar desde este respaldo por iniciativa del agente.

## Baseline Oracle aprobado, pero bloqueado para ejecución

| Decisión | Límite |
|---|---|
| `ORACLE_JDBC_URL` | Única fuente de host, puerto y servicio PDB. |
| `SYSTEM` | Solo provisioning administrativo; nunca runtime Spring. |
| `SALUDABLEMENTE_OWNER` | Owner inicial previsto de tablas, constraints e índices. |
| `SALUDABLEMENTE_APP` | Runtime de mínimo privilegio, sin DDL y con operaciones explícitamente otorgadas. |
| Credenciales | Solo `.env` local ignorado; nunca SQL, YAML, documentación, logs o pruebas. |

SQL*Plus/SQLcl solo podrá derivar su transporte desde `ORACLE_JDBC_URL` o una derivación local segura. La aprobación del diseño **no** autoriza conexión ni ejecución.

## Próximo límite de trabajo

La siguiente tarea es resolver con el usuario **solo** cuál será el primer módulo y su orden de construcción, usando `05-module-ownership-and-dependency-matrix.md`. No se inicia código, migración, provisioning ni Oracle hasta que esa decisión exista.

## Referencias

- `docs/rebuild/04-phase-1-inventory-and-oracle-decision.md`: evidencia de limpieza ejecutada y gate Oracle.
- `docs/rebuild/05-clean-baseline-and-first-module.md`: baseline limpio y decisión pendiente.
- `docs/rebuild/07-next-agent-handoff.md`: handoff definitivo.