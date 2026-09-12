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
| Exception | Handler transversal y excepción de recurso inexistente | Agregar una excepción de negocio solo cuando exista una regla equivalente real. |

## Límites explícitos

- `ResourceNotFoundException` y `GlobalExceptionHandler` son transversales y siguen el formato BOMERP de `timestamp`, `status`, `error` y `message`.
- No se copia `StockInsuficienteException`: pertenece al dominio de ventas. Un módulo nuevo define su excepción solo si su propia regla lo exige.
- No tocar Oracle, SQL, POM, Docker, configuración, ramas, commits ni PRs sin una autorización específica.
- Preservar siempre cambios preexistentes del workspace.

## Evidencia de Teams

La vertical incluye `entity`, DTO, mapper MapStruct, repository, service, controller y manejo de excepciones. La evidencia final registrada es `mvn test` 26/26 PASS y `git diff --check` PASS, con advertencias LF/CRLF preexistentes únicamente.

## Aplicación inicial: Actividades

Las capas `actividades/actividad/entity`, `dto` y `mapper` están completadas. El modelo lógico confirma nombre, fecha, hora de inicio, hora de fin, lugar, estado y creador; la entidad preserva estos conceptos con `LocalDate` y `LocalTime`, sin decidir DDL ni la representación física Oracle de `TIME`. `ActividadMapper` usa MapStruct, ignora `id` y `estado` al crear una entidad, y transforma la entidad completa a response. El siguiente paso, sujeto a revisión, es solo el repository de `Actividad`.
