# Teams: estructura de paquetes
```text
pe.edu.upeu.saludablemente
├── exception/       # implementada: package-info + GlobalExceptionHandler + ResourceNotFoundException
└── teams
    └── team
        ├── controller/   # implementada: TeamController.java + TeamControllerTest.java
        ├── dto/          # completada: TeamRequest, TeamResponse y TeamDtoTest
        ├── entity/       # completada: Team.java + TeamTest.java
        ├── mapper/       # implementada: TeamMapper.java + TeamMapperTest.java (MapStruct)
        ├── repository/   # implementada: TeamRepository.java (JpaRepository + filtro active)
        └── service/      # implementada: TeamService.java + TeamServiceImpl.java
```

## Regla de construcción

La estructura es obligatoria; las capas se implementan y prueban una por una. No se crean stubs ni carpetas vacías solo para mostrar el árbol.

## Estado verificado

`Team.java` está implementada con JPA y Lombok conforme a `Venta`: constructor vacío público, tabla futura `SALUDABLEMENTE_OWNER.TEAMS`, campos persistentes `id`, `name`, `description` y `active = true`. `TeamTest` verifica la construcción pública, el estado inicial, los accesores y el mapeo esencial sin ejecutar Oracle.

`TeamRequest`, `TeamResponse` y `TeamDtoTest` están auditados como conformes y permanecen como la capa DTO completada. La evidencia Maven vigente registra 26 pruebas PASS en la suite completa; no se requirió Oracle.

`mapper/TeamMapper.java` está implementada con MapStruct y `TeamMapperTest` cubre el mapeo directo sin Spring ni Oracle. `TeamMapperTest` pasó 4/4, la suite Maven pasó 26/26 y `git diff --check` pasó; solo emitió advertencias LF/CRLF preexistentes. El `repository/TeamRepository.java` está implementado como límite mínimo de Spring Data: hereda `JpaRepository<Team, Long>` y declara solo `findAllByActive(boolean active)`. No reclama cobertura de consulta en tiempo de ejecución; esa prueba queda diferida hasta una autorización separada de infraestructura aislada. El DDL y la migración permanecen fuera de alcance. `controller/TeamController.java` está implementado como contrato HTTP mínimo bajo `/api/v1/teams`, delega solo en `TeamService`, valida `TeamRequest` y cubre 200/201/400/404 mediante `TeamControllerTest`. `exception/package-info.java`, `exception/GlobalExceptionHandler.java` y `exception/ResourceNotFoundException.java` reemplazan el handler local del controller y centralizan 400/404 con el formato BOMERP. `StockInsuficienteException` no se copia porque pertenece al dominio de ventas. `service/TeamService.java` y `service/TeamServiceImpl.java` están implementados con contrato público, orquestación repository/mapper y transacciones de escritura explícitas; `TeamServiceImplTest` pasó 8/8 y cubre el contrato sin Spring ni Oracle; la suite Maven completa pasó 26/26. Para responsabilidades por clase, consultar `10-teams-layered-module-blueprint.md` y `11-bomerp-class-by-class-reference.md`.

