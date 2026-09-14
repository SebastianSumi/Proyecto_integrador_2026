# Teams: blueprint de capas basado en Ventas/Venta

**Referencia verificada.** La referencia estructural es `pe.edu.upeu.bomerp.ventas.venta`, no `catalogo.producto`. Se adopta su organización por capas, sus responsabilidades Spring Boot/JPA/Lombok y su separación `Service`/`ServiceImpl`; no se copian su dominio de ventas, sus schemas, sus entidades de detalle, sus consultas ni sus decisiones de negocio.

## Estructura de referencia y adaptación

BOMERP `ventas/venta` contiene `controller`, `dto`, `entity`, `mapper`, `repository`, `service/VentaService` y `service/VentaServiceImpl`. Teams conserva exactamente esa forma:

```text
pe.edu.upeu.saludablemente.teams
└── team
    ├── controller
    │   └── TeamController.java
    ├── dto
    │   ├── TeamRequest.java
    │   └── TeamResponse.java
    ├── entity
    │   └── Team.java
    ├── mapper
    │   └── TeamMapper.java
    ├── repository
    │   └── TeamRepository.java
    └── service
        ├── TeamService.java
        └── TeamServiceImpl.java
```

Teams no es cabecera-detalle. Por eso no tendrá un equivalente artificial de `DetalleVenta`, `VentaReporte`, `VentaResumen` o `VentaAgregado`.

## Convenciones obligatorias por capa

### `entity/Team.java`

Entidad JPA, como `Venta`:

```java
@Entity
@Table(name = "TEAMS", schema = "SALUDABLEMENTE_OWNER")
@Getter
@Setter
@NoArgsConstructor
public class Team { }
```

Debe incluir `id`, `name`, `description` y `active`, con `@Id`, `@GeneratedValue` y `@Column`. No usar `@Data`, setters fuera de la entidad, schema BOMERP, ni SQL/Oracle ejecutable. El mapeo JPA se prueba sin conectarse a Oracle; la migración se decide después.

### `dto/`

- `TeamRequest`: entrada para alta/edición; Bean Validation sobre `name` y `description`.
- `TeamResponse`: salida con `id`, `name`, `description`, `active`.
- No se incluye el conteo de personas activas hasta que Personal publique su contrato.

### `mapper/TeamMapper.java`

Equivalente funcional de `VentaMapper`: transforma `TeamRequest` a `Team` y `Team` a `TeamResponse`; jamás abre transacciones, consulta repositories ni ejecuta reglas de negocio.

BOMERP usa MapStruct para `VentaMapper`. La referencia `pom.xml` declara `org.mapstruct:mapstruct:1.6.3` y configura `org.mapstruct:mapstruct-processor:1.6.3` dentro de `maven-compiler-plugin`.

Saludablemente adoptó selectivamente ese patrón en `pom.xml`: `mapstruct:1.6.3`, `mapstruct-processor:1.6.3` para compilación principal y exclusión de Lombok del empaquetado Spring Boot. `TeamMapper` usa `@Mapper(componentModel = "spring")`; al crear una entidad ignora explícitamente `id` y `active`, y al responder mapea los cuatro campos de `Team`. `TeamMapperTest` lo instancia con `Mappers.getMapper` sin Spring ni Oracle.

La alineación no copia el POM completo de BOMERP: se conservan `spring-modulith.version=2.1.1`, `springdoc.version=3.1.0` y `ojdbc17`; tampoco se agrega Prometheus. `TeamMapperTest` pasó 4/4; tras el cierre de la vertical Teams, la suite Maven pasó 26/26 y `git diff --check` pasó; solo emitió advertencias LF/CRLF preexistentes.

### `repository/TeamRepository.java`

Extiende `JpaRepository<Team, Long>`. Solo define consultas necesarias y probadas, por ejemplo `findAllByActive(boolean active)`. `@EntityGraph` se agrega únicamente tras demostrar una necesidad real de fetch/N+1.

### `service/TeamService.java` y `TeamServiceImpl.java`

Como `VentaService` y `VentaServiceImpl`, la interfaz expresa el contrato y la implementación concentra orquestación:

```java
public interface TeamService {
    List<TeamResponse> findAll(Boolean active);
    TeamResponse findById(Long id);
    TeamResponse create(TeamRequest request);
    TeamResponse update(Long id, TeamRequest request);
    TeamResponse changeState(Long id, boolean active);
}
```

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamServiceImpl implements TeamService { }
```

Las escrituras llevan `@Transactional`. La regla de eliminación con personas activas queda para una operación posterior que consuma un servicio público de Personal, nunca `PersonaRepository`.

### `exception/` transversal

`package-info.java` declara `exception` como módulo transversal abierto, siguiendo BOMERP. `ResourceNotFoundException` recibe un mensaje directo. `GlobalExceptionHandler` convierte validación fallida a 400 y recurso inexistente a 404 con cuerpo JSON BOMERP (`timestamp`, `status`, `error`, `message`). No se copia `StockInsuficienteException` porque su regla pertenece a ventas.

### `controller/TeamController.java`

Como `VentaController`: `@RestController`, `@RequestMapping("/api/v1/teams")`, `@RequiredArgsConstructor`, `@Valid` y delegación exclusiva al servicio. No contiene lógica de negocio ni accede al repository. La implementación actual cubre listar, obtener, crear, actualizar y cambiar estado; el 404 lo resuelve `GlobalExceptionHandler`, no el controller.

## Orden de construcción

1. Refactorizar y verificar la entidad JPA `Team` con Lombok.
2. DTOs y pruebas de validación.
3. Mapper y pruebas de conversión: implementación verificada.
4. Repository: implementación verificada; prueba de persistencia real queda diferida hasta estrategia Oracle/infraestructura aprobada.
5. Interfaz/implementación de servicio y pruebas transaccionales: implementación verificada.
6. Controller y pruebas HTTP: implementación verificada.
7. Exception handler transversal mínimo: implementación verificada.

Tras cada capa: RED -> GREEN -> REFACTOR, prueba focalizada, suite completa, `git diff --check` y revisión fresca. No avanzar a Actividades o Metas hasta terminar Teams.
