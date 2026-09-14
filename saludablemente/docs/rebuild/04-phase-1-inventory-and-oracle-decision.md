# Fase 1: inventario cerrado y gate Oracle

**Estado confirmado:** la lista de limpieza filesystem fue ejecutada y el usuario decidió conservar el resultado. No se debe restaurar el backup. Oracle no se ha ejecutado y sigue bloqueado.

## Evidencia de limpieza ejecutada

| Área aprobada | Estado en el worktree |
|---|---|
| Producción Java legacy | Eliminada: módulos/classes legacy bajo `src/main/java/pe/edu/upeu/saludablemente/`. |
| Tests Java legacy | Eliminados bajo `src/test/java/pe/edu/upeu/saludablemente/`. |
| Recursos legacy | Eliminados: YAML de aplicación, logback y `META-INF/spring.factories`. |
| Docker Compose legacy | Eliminado: `database/docker/compose-dev.yml`. |
| Oracle legacy | Eliminados: scripts `01` a `08` y migración `team_identity_05/`. |

La limpieza es definitiva para esta reconstrucción salvo una instrucción explícita de restauración del usuario. No existe una lista de eliminación pendiente dentro de este alcance.

## Respaldo verificable

- Ruta: `C:/Users/pfloa/Backups/saludablemente-controlled-rebuild-20260910-175559`.
- Cobertura: 66 entradas pre-limpieza.
- Validación: SHA-256 **PASS**.
- Uso permitido: recuperación manual únicamente cuando el usuario la ordene expresamente.

No se leen secretos ni se restaura contenido automáticamente.

## Baseline Oracle aprobado

| Decisión | Estado / límite |
|---|---|
| `ORACLE_JDBC_URL` | Fuente canónica única de host, puerto y servicio PDB. |
| `SYSTEM` | Solo administración/provisioning; nunca Spring runtime. |
| `SALUDABLEMENTE_OWNER` | Owner inicial previsto de objetos de aplicación. |
| `SALUDABLEMENTE_APP` | Runtime separado, sin DDL y con privilegios explícitos mínimos. |
| Credenciales | Solo `.env` local ignorado. |

Variables requeridas por nombre: `ORACLE_JDBC_URL`, `ORACLE_ADMIN_USERNAME`, `ORACLE_ADMIN_PASSWORD`, `ORACLE_OWNER_USERNAME`, `ORACLE_OWNER_PASSWORD`, `ORACLE_APP_USERNAME`, `ORACLE_APP_PASSWORD`.

## Gate de Oracle: NO autorizado

No se ejecutó provisioning, conexión administrativa, creación de usuarios/schemas/tablas, grants, migraciones, DDL ni DML. El diseño aprobado no habilita ninguna de esas acciones. SQL*Plus/SQLcl deberá derivar el destino desde `ORACLE_JDBC_URL` cuando exista autorización independiente de ejecución.

## Decisión siguiente única

Resolver con el usuario el primer módulo y orden de construcción de la matriz. No comenzar código, migraciones ni Oracle antes de esa respuesta.

## Referencias

- `docs/rebuild/03-clean-slate-transition.md`: alcance y recuperación de limpieza.
- `docs/rebuild/05-module-ownership-and-dependency-matrix.md`: responsables, dependencias y fases propuestas.
- `docs/rebuild/07-next-agent-handoff.md`: procedimiento de continuación.