# Referencia BOMERP aplicada a Saludablemente

**Propósito.** Esta guía traduce la referencia real `bomerp/ventas/venta` y sus transversales hacia Saludablemente. Es una guía de responsabilidades por clase: no autoriza copiar credenciales, schemas `BOM_*`, privilegios, endpoints de prueba, ni lógica de ventas.

## Referencia auditada

Se revisaron `BomerpBackendApplication`, `CorsConfig`, `HelloController`, `OpenApiConfig`, excepciones, `CorrelationIdFilter`, recursos de configuración y el módulo completo `ventas/venta`. La configuración de desarrollo de BOMERP contiene credenciales de ejemplo: no se replican, no se documentan aquí y no se leen desde Saludablemente.

## Estructura global futura

```text
pe.edu.upeu.saludablemente
├── SaludablementeApplication.java
├── CorsConfig.java
├── OpenApiConfig.java
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   └── <BusinessRuleException>.java
└── filter/
    └── CorrelationIdFilter.java
```

| Clase BOMERP | Responsabilidad observada | Regla para Saludablemente |
|---|---|---|
| `BomerpBackendApplication` | Arranque `@SpringBootApplication`. | Crear `SaludablementeApplication` solo al habilitar la primera capa Spring/HTTP; no antes. |
| `CorsConfig` | Lee orígenes desde propiedad y configura `/api/**`. | Usar propiedad no secreta por entorno; nunca orígenes hardcodeados en código. |
| `HelloController` | Endpoint de smoke test. | No copiar salvo que exista un requisito explícito de health/smoke HTTP. |
| `OpenApiConfig` | Bean `OpenAPI` y metadatos API. | Crear cuando exista el primer controller. |
| `GlobalExceptionHandler` | Convierte excepciones y validación a respuestas HTTP con `Map<String,Object>`. | Implementado con el formato BOMERP: `timestamp`, `status`, `error`, `message`. |
| `ResourceNotFoundException` | Señala recurso inexistente con mensaje directo. | Implementado y usado por `TeamServiceImpl` para `Team` inexistente. |
| `StockInsuficienteException` | Regla específica de ventas. | No copiar en Teams; solo documentar como ejemplo de excepción de negocio si aparece una regla propia. |
| `CorrelationIdFilter` | Propaga/genera ID de trazabilidad por request. | Crear cuando haya cadena HTTP y logging configurados; no pertenece a la entity. |

## Configuración Spring Boot

| Archivo futuro | Contenido permitido |
|---|---|
| `application.yml` | nombre de aplicación, perfil activo y propiedades no secretas. |
| `application-dev.yml` | referencias `${ORACLE_JDBC_URL}`, `${ORACLE_APP_USERNAME}`, `${ORACLE_APP_PASSWORD}`; `ddl-auto: validate`; CORS por propiedad. |

Nunca se copia una contraseña, URL con password, usuario BOMERP, schema BOMERP ni un origin local de la referencia. `SALUDABLEMENTE_APP` es la única cuenta Spring runtime.

## Patrón completo: `ventas/venta` -> `teams/team`

```text
ventas/venta                          teams/team
├── controller/VentaController         ├── controller/TeamController
├── dto/VentaRequest                   ├── dto/TeamRequest
├── dto/VentaResponse                  ├── dto/TeamResponse
├── entity/Venta                       ├── entity/Team
├── mapper/VentaMapper                 ├── mapper/TeamMapper
├── repository/VentaRepository         ├── repository/TeamRepository
└── service/VentaService               └── service/TeamService
    service/VentaServiceImpl               service/TeamServiceImpl
```

`Venta` es cabecera-detalle y trabaja con productos. `Team` es una entidad no transaccional simple. Por ello Teams no tendrá `DetalleTeam`, reportes, totales, stock ni llamadas a servicios inexistentes.

## Lógica exacta por clase de Teams

### `entity/Team.java`

- JPA: `@Entity`, `@Table(name = "TEAMS", schema = "SALUDABLEMENTE_OWNER")`, `@Id`, `@GeneratedValue`, `@Column`.
- Lombok: `@Getter`, `@Setter`, `@NoArgsConstructor`; nunca `@Data`.
- Estado: `id`, `name`, `description`, `active`.
- Restricciones del modelo lógico: nombre obligatorio hasta 60; descripción opcional hasta 200; activo por defecto.
- Entidad mínima de persistencia: no prescribe métodos de intención, factories ni lógica de negocio. Las reglas de actualización, cambio de estado o eliminación se diseñarán explícitamente en la futura capa de servicio.

### `dto/TeamRequest.java`

- Entrada HTTP para crear/editar.
- `@NotBlank @Size(max = 60)` en nombre; `@Size(max = 200)` en descripción.
- No posee `id`, estado de persistencia, repositorios ni anotaciones JPA.

### `dto/TeamResponse.java`

- Salida HTTP: `id`, `name`, `description`, `active`.
- Lombok `@Getter`, `@Setter`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`, como la respuesta de Venta.
- El conteo de personas activas se agrega solo mediante contrato público futuro de Personal.

### `mapper/TeamMapper.java`

- Convierte `TeamRequest -> Team` y `Team -> TeamResponse`; cualquier actualización de una entidad existente debe respetar el contrato que se defina para servicio.
- No valida reglas, no busca entidades, no abre transacciones y no consulta repositories.
- BOMERP usa MapStruct y declara `mapstruct:1.6.3` junto con su processor en Maven. Saludablemente adoptó selectivamente ese patrón: API 1.6.3, processor 1.6.3 para compilación principal y exclusión de Lombok del empaquetado Spring Boot. `TeamMapper` usa `@Mapper(componentModel = "spring")`; al crear una entidad ignora `id` y `active` para preservar los valores de la entidad, y al responder copia `id`, `name`, `description` y `active`. `TeamMapperTest` usa `Mappers.getMapper` sin Spring ni Oracle.
- No se copió el POM completo de BOMERP: Saludablemente conserva `spring-modulith.version=2.1.1`, `springdoc.version=3.1.0` y `ojdbc17`, y no incorpora Prometheus. Estas elecciones no pertenecen al patrón de mapeo.

### `repository/TeamRepository.java`

- `extends JpaRepository<Team, Long>`.
- Consulta mínima prevista: `findAllByActive(boolean active)`.
- No usa `@EntityGraph` sin evidencia de N+1 y no accede a Personal.

### `service/TeamService.java`

Interfaz pública del módulo: listar, obtener por id, crear, actualizar y cambiar estado. Es el contrato que usa controller, no `TeamServiceImpl` directamente.

### `service/TeamServiceImpl.java`

- `@Service`, `@RequiredArgsConstructor`, `@Transactional(readOnly = true)` a nivel clase.
- Escrituras con `@Transactional` explícito.
- Orquesta `TeamRepository` + `TeamMapper`.
- Si en el futuro se requiere eliminar, consulta un servicio público de Personal; nunca su repository.

### `exception/package-info.java`

- Declara `pe.edu.upeu.saludablemente.exception` como módulo transversal abierto con `@ApplicationModule(type = OPEN)`.
- Es patrón estructural copiado de BOMERP; no contiene lógica de negocio.

### `controller/TeamController.java`

- `@RestController`, `@RequestMapping("/api/v1/teams")`, `@RequiredArgsConstructor`.
- Recibe `@Valid TeamRequest`, llama a `TeamService` y devuelve `TeamResponse` con HTTP coherente.
- Sin reglas de negocio, sin JPA y sin transacciones. La implementación actual cubre listar, obtener, crear, actualizar y cambiar estado; el 404 se maneja mediante `GlobalExceptionHandler`.

## Pruebas por capa

| Capa | Pruebas mínimas |
|---|---|
| Entity | creación, límites, actualización y estado. |
| DTO | validación Bean Validation. |
| Mapper | cada campo entrada/salida y actualización. |
| Repository | consulta `active` y constraint real cuando Oracle esté autorizado. |
| Service | éxito, no encontrado, regla de estado y rollback si aplica. |
| Controller | 200/201, 400 de request inválido, 404 y contrato JSON. |

## Secuencia obligatoria

1. `Team` y sus pruebas: completados y verificados.
2. DTOs y sus pruebas: completados y verificados.
3. Mapper MapStruct implementado y verificado. `TeamMapperTest` pasó 4/4, la suite Maven pasó 26/26 y `git diff --check` pasó; solo emitió advertencias LF/CRLF preexistentes.
4. Repository implementado; prueba de persistencia real queda diferida hasta infraestructura aprobada.
5. Interfaz + implementación de servicio implementadas y verificadas.
6. Controller implementado y verificado.
7. Manejo de errores transversal mínimo implementado y verificado.
8. OpenAPI, CORS, filtro de correlación y configuración por propiedades quedan como decisiones de plataforma posteriores.

Cada paso exige RED -> GREEN -> REFACTOR, prueba focalizada, suite completa y `git diff --check`. No se avanza a Actividades ni Metas hasta terminar Teams.
