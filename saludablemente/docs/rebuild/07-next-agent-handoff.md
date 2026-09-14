# Handoff vigente: Teams cerrado; próximo módulo Actividades

La reconstrucción limpia está retenida y las capas `mapper/`, `repository/`, `service/`, `controller/` y `exception/` necesarias para Teams ya están implementadas. No se restaura legado ni se ejecuta Oracle. Teams queda como vertical de referencia; el siguiente trabajo autorizado empieza exclusivamente por la capa `entity` de Actividades y sus pruebas.

## Estado confirmado

| Área | Estado |
|---|---|
| Baseline limpio | Retenido; el backup verificado permanece solo para recuperación manual. |
| Oracle y secretos | Sin ejecución; no leer `.env` ni conectarse a Oracle. |
| `entity/` | Completada y verificada: JPA/Lombok, `TeamTest`. |
| `dto/` | Completada y verificada: `TeamRequest`, `TeamResponse`, `TeamDtoTest`. |
| Maven | `mvn test -Dtest=TeamControllerTest`: 7/7 PASS; `mvn test -Dtest=TeamServiceImplTest`: 8/8 PASS; `mvn test`: 26/26 PASS. |
| MapStruct | Alineación selectiva aplicada: API y processor 1.6.3; Lombok excluido del empaquetado Spring Boot. |
| `mapper/` | `TeamMapper` y su prueba directa con `Mappers.getMapper` están implementados y verificados. `TeamMapperTest` pasó 4/4, la suite Maven pasó 26/26 y `git diff --check` pasó; solo emitió advertencias LF/CRLF preexistentes. |
| `repository/` | `TeamRepository` hereda `JpaRepository<Team, Long>` y declara únicamente `findAllByActive(boolean active)`. La cobertura comportamental se difiere hasta una autorización independiente de infraestructura de pruebas. |
| `service/` | `TeamService` y `TeamServiceImpl` están implementados. La prueba focalizada cubre listar, filtrar por estado, obtener por id, no encontrado, crear, actualizar, cambiar estado y anotaciones transaccionales. |
| `controller/` | `TeamController` expone `/api/v1/teams`, delega exclusivamente en `TeamService`, valida `TeamRequest`, cubre 200/201/400/404 y cambio de estado por HTTP. |
| `exception/` | Patrón BOMERP implementado: `package-info.java` abre el módulo transversal, `GlobalExceptionHandler` devuelve `Map<String, Object>` con `timestamp/status/error/message`, y `ResourceNotFoundException` recibe mensaje directo. No se copia `StockInsuficienteException` porque pertenece a ventas. |

La alineación con BOMERP no copió su POM completo: Saludablemente conserva `spring-modulith.version=2.1.1`, `springdoc.version=3.1.0` y `ojdbc17`, y no incorpora Prometheus.

## Lectura obligatoria

1. `docs/README.md`
2. `docs/rebuild/08-teams-module-contract.md`
3. `docs/rebuild/09-teams-package-structure.md`
4. `docs/rebuild/10-teams-layered-module-blueprint.md`
5. `docs/rebuild/11-bomerp-class-by-class-reference.md`
6. `pom.xml`

## Estado del mapper, repository, service, controller y exception de Teams

`TeamMapper` usa `@Mapper(componentModel = "spring")` con los DTO y la entidad existentes. Convierte request a entidad y entidad a response; no valida reglas, no consulta repositories, no abre transacciones ni inicia Oracle. La prueba focalizada no requiere Spring ni Oracle.

`TeamRepository` mantiene el límite de persistencia de Teams: CRUD heredado y un único filtro derivado por `active`. No incorpora `@EntityGraph`, métodos CRUD redeclarados, acceso cross-module ni garantías de unicidad o borrado. No se agregan H2, configuración ni pruebas mock/reflection; la verificación comportamental del filtro queda pendiente de una tarea separada con infraestructura autorizada.

`TeamService` expone el contrato público de la capa: `findAll(Boolean active)`, `findById(Long id)`, `create(TeamRequest request)`, `update(Long id, TeamRequest request)` y `changeState(Long id, boolean active)`. `TeamServiceImpl` usa `@Service`, `@RequiredArgsConstructor`, `@Transactional(readOnly = true)`, `TeamRepository` y `TeamMapper`; las operaciones de escritura llevan `@Transactional` explícito. No accede a otros módulos ni repositories externos.

`TeamController` expone `GET /api/v1/teams`, `GET /api/v1/teams/{id}`, `POST /api/v1/teams`, `PUT /api/v1/teams/{id}` y `PATCH /api/v1/teams/{id}/state?active=...`. No accede a repositories, no abre transacciones y no contiene reglas de negocio. El mapeo 404 ya no vive en el controller: `TeamServiceImpl` lanza `ResourceNotFoundException` con mensaje directo y `GlobalExceptionHandler` traduce 404 de forma transversal. También centraliza 400 de validación con el formato BOMERP `timestamp/status/error/message`.

Antes de cualquier exploración estructural, inicializar y consultar CodeGraph. Recuperar el contexto relevante desde Engram antes de trabajar y guardar en Engram toda decisión, descubrimiento o corrección no trivial.

## Guardas operativas

- No restaurar el backup ni borrar más archivos.
- No modificar `.env`, YAML, SQL, Oracle, POM, DTO o entity durante esta tarea salvo una autorización explícita posterior.
- La capa transversal `exception/` quedó verificada tras RED → GREEN → REFACTOR: el RED falló por ausencia de `pe.edu.upeu.saludablemente.exception`, las pruebas focalizadas `TeamControllerTest` + `TeamServiceImplTest` pasaron 15/15, la suite Maven pasó 26/26 y `git diff --check` pasó; solo emitió advertencias LF/CRLF preexistentes.
- No crear OpenSpec ni ejecutar una fase SDD para trabajo directo. Si el usuario invoca SDD, primero cumplir `sdd-init` y usar el dispatcher nativo de SDD antes de enrutar cualquier fase.
- Teams queda cerrado como vertical HTTP mínima. Próxima decisión: pasar al siguiente módulo o agregar solo documentación/OpenAPI/CORS si se requiere para entrega. Las pruebas comportamentales del repository requieren una autorización separada para infraestructura aislada.
- Para módulos posteriores, consultar `docs/rebuild/12-reference-vertical-for-next-modules.md`: reutiliza la estructura y límites de Teams, no sus campos, reglas ni excepciones de negocio.
