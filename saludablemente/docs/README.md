# Documentación de Saludablemente

## Estado actual

Saludablemente está en reconstrucción controlada. La limpieza legacy fue ejecutada y retenida; Oracle no se ejecutó. El primer módulo aprobado es **Teams**: sus capas `entity/` y `dto/` están completadas y verificadas. El patrón de compilación MapStruct quedó adoptado selectivamente y la capa `mapper/` ya está implementada y verificada. La capa `repository/` también está implementada con el contrato mínimo de Spring Data; su prueba comportamental queda diferida hasta que se autorice infraestructura aislada de pruebas. La capa `service/` está implementada con contrato e implementación Spring transaccional. La capa `controller/` está implementada como contrato HTTP mínimo bajo `/api/v1/teams`, y la capa transversal `exception/` centraliza 400/404.

## Ruta de lectura

1. [Fundación](rebuild/00-project-foundation.md)
2. [Oracle y seguridad](rebuild/01-oracle-data-and-security.md)
3. [Playbook de entrega](rebuild/02-module-delivery-playbook.md)
4. [Limpieza y respaldo](rebuild/03-clean-slate-transition.md)
5. [Inventario y gate Oracle](rebuild/04-phase-1-inventory-and-oracle-decision.md)
6. [Propiedad y dependencias](rebuild/05-module-ownership-and-dependency-matrix.md)
7. [Modelo lógico y traducción Oracle](rebuild/06-logical-data-model-and-oracle-translation.md)
8. [Handoff vigente](rebuild/07-next-agent-handoff.md)
9. [Contrato Teams](rebuild/08-teams-module-contract.md)
10. [Paquetes Teams](rebuild/09-teams-package-structure.md)
11. [Blueprint Teams basado en Ventas](rebuild/10-teams-layered-module-blueprint.md)
12. [Referencia BOMERP clase por clase](rebuild/11-bomerp-class-by-class-reference.md)
13. [Vertical de referencia para módulos posteriores](rebuild/12-reference-vertical-for-next-modules.md)
14. [Contrato inicial de Actividades](rebuild/13-activities-module-contract.md)
15. [Preparación de integración S06](rebuild/14-s06-integration-readiness.md)
16. [Diseño de concurrencia Oracle para Actividades](rebuild/15-activities-concurrency-oracle-design.md)
17. [Scripts Oracle manuales](../database/oracle/manual-migrations/README.md)
18. [Requisitos y fundamento de Actividades](rebuild/16-activities-module-requirements-and-rationale.md)

## Estado de Teams

| Capa | Estado |
|---|---|
| `entity` | Completada y verificada: JPA + Lombok. |
| `dto` | Completada y verificada: contratos de entrada/salida y pruebas. |
| `mapper` | Completada y verificada: `TeamMapper` usa MapStruct. `TeamMapperTest` pasó 4/4, la suite Maven pasó 26/26 y `git diff --check` pasó; solo emitió advertencias LF/CRLF preexistentes. |
| `repository` | Implementada: `TeamRepository` hereda `JpaRepository<Team, Long>` y declara solo `findAllByActive(boolean active)`. La prueba comportamental queda diferida hasta contar con infraestructura aislada autorizada. |
| `service` | Completada y verificada: `TeamService` + `TeamServiceImpl`, CRUD lógico y cambio de estado con pruebas unitarias. |
| `controller` | Completada y verificada: `TeamController` delega en `TeamService`, valida `TeamRequest` y expone el contrato HTTP mínimo. |
| `exception` | Completada y verificada: `package-info.java` + `GlobalExceptionHandler` + `ResourceNotFoundException` centralizan 400/404. |

## Inicio de Actividades

Las capas `entity`, `dto`, `mapper`, `repository`, `service` y `controller` de `Actividad` están completadas. `ActividadServiceImpl` lista, obtiene, crea y actualiza dentro de fronteras transaccionales, y rechaza solapamientos de horario. `ActividadController` expone esas operaciones bajo `/api/v1/actividades`, valida los DTO de entrada y el manejo global centraliza 400, 404 y 409.

Para `Inscripcion`, las capas `entity`, `dto`, `mapper`, `repository`, `service` y `controller` están completadas. El registro verifica la actividad mediante el contrato público de Actividades e impide una segunda inscripción vigente con 409 en el flujo normal; la cancelación se publica como transición idempotente bajo `/api/v1/inscripciones/{id}/cancelacion` y conserva historial. `database/oracle/manual-migrations/V001__enrollment_active_uniqueness.sql` está listo para activarlo contra Oracle de manera manual y controlada: hasta que el equipo lo ejecute, la unicidad concurrente no está garantizada. El solapamiento concurrente sigue pendiente en `rebuild/15-activities-concurrency-oracle-design.md`.

## Documentos históricos

`external-module-recommendations.md`, `next-iteration.md`, `pedro-module-roadmap.md` y `project-architecture.md` describen el legado eliminado. Se conservan como evidencia; no son instrucciones de implementación para la reconstrucción.
