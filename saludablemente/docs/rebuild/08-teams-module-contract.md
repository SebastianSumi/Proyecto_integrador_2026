# Contrato del módulo Teams

## Alcance

Teams es un módulo no transaccional asignado a Pedro Loayza. Su agregado es `Team`; su estructura sigue `teams/team/<layer>` y la referencia de organización es BOMERP `ventas/venta`.

## Hechos del brief y schema

- Crear, editar e inactivar Teams.
- No eliminar un Team si existen personas activas asignadas.
- `TEAM` tiene nombre obligatorio, descripción opcional y estado activo.
- `PERSONA.id_team` es una relación opcional lógica.

## Contrato entre módulos

Teams no accede al repository de Personal. La futura regla de eliminación debe consultar un servicio público de Personal. Hasta entonces Teams no expone operación de borrado.

## Nombres aprobados

| Concepto | Nombre |
|---|---|
| Módulo | `Teams` |
| Agregado / entidad | `Team` |
| Paquete | `pe.edu.upeu.saludablemente.teams.team` |
| Tabla futura | `TEAMS` |
| Owner futuro | `SALUDABLEMENTE_OWNER` |

## Estado de capas implementadas

`entity/Team.java` está completada y verificada como entidad JPA/Lombok siguiendo el patrón estructural de `Venta`: constructor vacío público, `@Getter`, `@Setter`, `@NoArgsConstructor`, y sin factory ni lógica de validación. Mapea `SALUDABLEMENTE_OWNER.TEAMS`; contiene `id`, `name`, `description` y `active`, cuyo valor inicial es `true`. `TeamTest` cubre el valor inicial, los accesores y el mapeo JPA esencial sin conectarse a Oracle.

`dto/TeamRequest.java`, `dto/TeamResponse.java` y `TeamDtoTest` están completados y retenidos tras auditoría: los DTO separan entrada y salida HTTP, y sus pruebas verifican el contrato correspondiente. La evidencia de Maven aprobada para Teams registra suite completa: 26 pruebas, todas PASS.

## Capa mapper

El patrón de compilación de MapStruct 1.6.3 fue adoptado selectivamente desde BOMERP: API, annotation processor para compilación principal y exclusión de Lombok del empaquetado Spring Boot. `mapper/TeamMapper.java` usa `@Mapper(componentModel = "spring")`: `TeamRequest` mapea solo `name` y `description`, preservando `id = null` y `active = true`; `Team` mapea `id`, `name`, `description` y `active` a `TeamResponse`. `TeamMapperTest` instancia el mapper con `Mappers.getMapper` sin Spring ni Oracle. `TeamMapperTest` pasó 4/4, la suite Maven pasó 26/26 y `git diff --check` pasó; solo emitió advertencias LF/CRLF preexistentes.

No se copiaron decisiones ajenas a ese patrón: no se rebajaron `spring-modulith.version=2.1.1` ni `springdoc.version=3.1.0`, se retuvo `ojdbc17` y no se agregó Prometheus. Estas diferencias pertenecen al stack vigente de Saludablemente, no a la responsabilidad de MapStruct.

## Capas service y controller

`TeamService` y `TeamServiceImpl` están implementados como contrato público e implementación Spring transaccional. `TeamController` expone el contrato HTTP mínimo de Teams bajo `/api/v1/teams`: listar con filtro opcional `active`, obtener por id, crear, actualizar y cambiar estado. El controller delega exclusivamente en `TeamService`, valida `TeamRequest` y no accede a repositories. `ResourceNotFoundException`, `GlobalExceptionHandler` y `package-info.java` siguen el patrón BOMERP: módulo transversal abierto, constructor por mensaje directo y respuestas `Map<String, Object>` con `timestamp`, `status`, `error` y `message`. `StockInsuficienteException` no se copia porque pertenece al dominio ventas. `TeamServiceImplTest` pasó 8/8, `TeamControllerTest` pasó 7/7, la suite Maven pasó 26/26 y `git diff --check` pasó; solo emitió advertencias LF/CRLF preexistentes.
