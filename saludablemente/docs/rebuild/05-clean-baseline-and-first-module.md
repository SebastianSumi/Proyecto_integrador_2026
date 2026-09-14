# Baseline limpio y módulo activo

**Estado vigente.** La limpieza legacy fue ejecutada y retenida. Oracle no se ejecutó. El equipo eligió el orden **Teams -> Actividades -> Metas**; Teams es el único módulo activo.

## Baseline protegido

| Área | Estado |
|---|---|
| Build | `pom.xml`, Maven wrapper y `.mvn/` permanecen. |
| Secretos | `.env` local ignorado; no leído ni expuesto. |
| Legado | Producción Java, pruebas, recursos, Compose y scripts Oracle aprobados fueron eliminados. |
| Respaldo | Recuperación manual únicamente mediante orden explícita del usuario. |
| Oracle | Sin provisioning, DDL, DML, grants ni conexión administrativa. |

## Teams: estado por capas

- Módulo: `teams/team`.
- Referencia estructural: `bomerp/ventas/venta`.
- Capa actual: `entity/Team`.
- Corrección pendiente: convertir la clase actual en entidad JPA con Lombok conforme a `10` y `11`.
- Próxima capa posterior a entity aprobada: `dto`.

## Límites

No restaurar legado, no borrar más archivos y no avanzar a Actividades/Metas. No ejecutar Oracle hasta aprobar migración, tipos físicos, constraints e índices.

## Referencias

- `03-clean-slate-transition.md`
- `04-phase-1-inventory-and-oracle-decision.md`
- `10-teams-layered-module-blueprint.md`
- `11-bomerp-class-by-class-reference.md`
