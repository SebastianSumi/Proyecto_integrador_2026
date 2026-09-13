# Vertical de referencia: Teams

**Decisión.** Teams es la vertical terminada que guía la construcción de los módulos posteriores. Sirve para repetir límites arquitectónicos y evidencia de calidad; no autoriza copiar campos, reglas, relaciones, endpoints ni excepciones de negocio.

## Ruta rápida para un módulo nuevo

1. Confirmar en el brief y el modelo lógico el agregado, sus campos, relaciones y reglas.
2. Implementar una sola capa y su prueba focalizada.
3. Aplicar RED -> GREEN -> REFACTOR proporcional.
4. Ejecutar la prueba focalizada, `mvn test` y `git diff --check` antes de cerrar la capa.
5. Actualizar solo la documentación afectada; recién entonces decidir la capa siguiente.

## Patrón que se reutiliza

| Área | Referencia Teams | Regla para otro módulo |
|---|---|---|
| Organización | `<modulo>/<agregado>/<capa>` | Cada módulo es dueño de sus clases y repository. |
| Entity | JPA + Lombok, sin `@Data` ni lógica de servicio | Mapear solo hechos confirmados; no materializar Oracle ni SQL. |
| DTO | Request y response separados | No serializar entidades ni mezclar persistencia con HTTP. |
| Mapper | MapStruct sin reglas ni accesos externos | Transformar datos; las decisiones quedan en servicio. |
| Repository | `JpaRepository` y consultas mínimas evidenciadas | No consultar repositories de otros módulos ni usar `@EntityGraph` sin prueba de N+1. |
| Service | Interfaz pública + implementación transaccional | Concentrar reglas, transacciones y colaboraciones mediante servicios públicos. |
| Controller | `/api/v1`, validación y delegación exclusiva | No incluir JPA, transacciones ni reglas de negocio. |
| Exception | Handler transversal, excepción de recurso inexistente y bases HTTP compartidas | La excepción de negocio permanece en su módulo y extiende una base compartida según su semántica HTTP. |

## Límites explícitos

- `ResourceNotFoundException`, `BusinessConflictException`, `BusinessValidationException` y `GlobalExceptionHandler` son transversales y siguen el formato BOMERP de `timestamp`, `status`, `error` y `message`. El handler solo conoce las bases compartidas, no excepciones de módulos.
- No se copia `StockInsuficienteException`: pertenece al dominio de ventas. Un módulo nuevo define su excepción solo si su propia regla lo exige.
- No tocar Oracle, SQL, POM, Docker, configuración, ramas, commits ni PRs sin una autorización específica.
- Preservar siempre cambios preexistentes del workspace.

## Evidencia de Teams

La vertical incluye `entity`, DTO, mapper MapStruct, repository, service, controller y manejo de excepciones. La evidencia final registrada es `mvn test` 26/26 PASS y `git diff --check` PASS, con advertencias LF/CRLF preexistentes únicamente.

## Aplicación: Actividades

Las verticales de `Actividad` e `Inscripcion` están completadas en Java: entity, DTO, mapper, repository, service, controller y manejo HTTP de errores. `ActividadServiceImpl` aplica intervalo válido y solapamiento al crear/actualizar; `InscripcionServiceImpl` verifica la existencia de la actividad mediante `ActividadService`, impide duplicados vigentes y cancela de forma idempotente. El modelo conserva `LocalDate` y `LocalTime` sin decidir DDL ni la representación física Oracle de `TIME`.

Las garantías ante carreras concurrentes están preparadas como migraciones Oracle manuales: `V001` para inscripción vigente y `V002` para agenda por lugar/fecha. Se activan únicamente tras ejecución controlada en Oracle; ver `15-activities-concurrency-oracle-design.md`.

## Próximo paso documentado: Metas

Metas depende de contratos públicos aún no definidos de Personal y Evaluación Nutricional. Antes de iniciar código, consultar [la ficha de requisitos y dependencias de Metas](17-metas-requirements-and-dependencies.md) y cerrar sus gates de integración.
