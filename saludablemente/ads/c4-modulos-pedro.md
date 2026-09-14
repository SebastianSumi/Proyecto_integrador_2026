# Modelo C4 de los módulos de Pedro

Este documento presenta la arquitectura académica de Saludablemente con **C1 y C2 como contexto general del proyecto** y **C3 y C4 enfocados en Teams, Actividades e Inscripciones**. Los diagramas distinguen el código existente de las integraciones futuras y están divididos para poder trasladarlos a Miro sin convertirlos en un único gráfico ilegible.

## Convenciones visuales

| Convención | Significado |
|---|---|
| Flecha continua | Relación implementada en el backend actual |
| Flecha punteada | Integración requerida o prevista, todavía no implementada |
| “Futuro” o “no implementado” | Elemento del brief, no componente existente |
| Mismo contenedor Mermaid | Elementos que pertenecen al mismo límite arquitectónico |

## C1 - Contexto del sistema

### Elementos

| Elemento | Tipo | Descripción |
|---|---|---|
| Administrador | Persona | Gestiona equipos y tiene visibilidad general según el brief |
| Coordinador de actividades | Persona | Programa actividades y revisa su participación |
| Colaborador | Persona | Consulta actividades y participa en el programa |
| Saludablemente | Sistema | Centraliza la gestión del programa de salud ocupacional |
| Servicio de identidad institucional | Sistema externo futuro | Proveerá personas, usuarios y roles; no está implementado |
| Servicio de notificaciones | Sistema externo futuro | Recibirá cambios relevantes de actividades; no está implementado |

### Diagrama

```mermaid
flowchart LR
    admin[Administrador]
    coordinator[Coordinador de actividades]
    collaborator[Colaborador]
    system["Saludablemente\nGestión del programa de salud ocupacional"]
    identity["Identidad, Personal y Auth\nFuturo"]
    notifications["Notificaciones\nFuturo"]

    admin -->|Gestiona equipos y consulta actividades| system
    coordinator -->|Programa actividades e inscripciones| system
    collaborator -->|Consulta actividades y solicita inscripción| system
    system -.->|Validará personas, usuarios y roles| identity
    system -.->|Publicará cambios de estado| notifications
```

**Para Miro:** crear una tarjeta central para Saludablemente, ubicar actores a la izquierda y sistemas externos a la derecha. Usar línea sólida para interacciones actuales y punteada para integraciones futuras.

## C2 - Contenedores

### Elementos

| Elemento | Tipo | Estado | Descripción |
|---|---|---|---|
| Cliente web o consumidor HTTP | Aplicación cliente | Futuro/externo al repositorio | Consume contratos JSON; el frontend no está en este backend |
| API Saludablemente | Aplicación Spring Boot | Implementado | Monolito modular Java 21 con REST, validación, JPA y Spring Modulith |
| Oracle Database | Base de datos | Implementado | Aloja los esquemas físicos `SAL_TEAMS` y `SAL_ACTIVITIES` |
| Personal y Auth | Servicio o módulo | No implementado | Resolverá identidad, estado de persona y autorización |
| Notificaciones y Asistencias | Servicios o módulos | No implementados | Consumirán eventos o contratos de Actividades |

### Diagrama

```mermaid
flowchart TB
    client["Cliente web o consumidor HTTP\nNo incluido en este repositorio"]

    subgraph backend["Contenedor: API Saludablemente - implementado"]
        api["Spring Boot 4.0.7 / Java 21\nSpring MVC + Validation + JPA\nREST /api/v1"]
    end

    subgraph oracle["Contenedor: Oracle Database - implementado"]
        teamsDb["SAL_TEAMS\nTEAMS"]
        activitiesDb["SAL_ACTIVITIES\nACTIVITIES\nACTIVITY_PLACE_LOCKS\nACTIVITY_ENROLLMENTS"]
    end

    identity["Personal y Auth\nFuturo"]
    consumers["Notificaciones y Asistencias\nFuturo"]

    client -->|HTTP/JSON| api
    api -->|JDBC/JPA| teamsDb
    api -->|JDBC/JPA| activitiesDb
    api -.->|Contratos públicos por definir| identity
    consumers -.->|Consumirán Actividades| api
```

**Para Miro:** dibujar tres zonas principales —cliente, backend y datos— y una zona lateral gris para integraciones futuras. No colocar Personal/Auth dentro de la API actual hasta que exista implementación.

> El PDF de esquema expresa un modelo lógico con sintaxis MySQL/MariaDB. Los scripts en `database/oracle/` definen la persistencia física vigente del backend.

## C3 - Componentes

C3 detalla los componentes reales dentro del contenedor Spring Boot. `teams` y `actividades` son módulos Spring Modulith; `actividad` e `inscripcion` son subdominios internos de `actividades`.

### C3.1 - Teams

| Elemento | Tipo | Descripción |
|---|---|---|
| `TeamController` | Controller | Adapta `/api/v1/equipos` y respuestas HTTP |
| `TeamRequest`, `TeamStateRequest`, `TeamResponse` | DTO | Definen entrada, estado explícito y salida pública |
| `TeamService` | Servicio de aplicación | Coordina consultas, unicidad, actualización y transacciones |
| `TeamMapper` | Mapper | Normaliza y traduce DTO/entidad |
| `TeamRepository` | Repositorio | Persiste Teams y consulta por nombre/estado |
| `Team` | Entidad | Mantiene nombre, descripción y estado activo |
| `BooleanToIntegerConverter` | Adaptador JPA | Traduce booleano Java a `NUMBER(1)` Oracle |

```mermaid
flowchart LR
    client[Cliente REST]
    controller[TeamController]
    request["TeamRequest\nTeamStateRequest"]
    response[TeamResponse]
    service[TeamService]
    mapper[TeamMapper]
    repository[TeamRepository]
    entity[Team]
    converter[BooleanToIntegerConverter]
    db[(SAL_TEAMS.TEAMS)]

    client --> controller
    controller --> request
    controller --> service
    service --> mapper
    service --> repository
    mapper --> entity
    mapper --> response
    repository --> entity
    entity --> converter
    repository --> db
```

**Para Miro:** usar una fila Controller -> Service -> Repository -> Oracle. Colocar DTOs junto al Controller y Mapper/Entity debajo del Service.

### C3.2 - Actividades

| Elemento | Tipo | Descripción |
|---|---|---|
| `ActividadController` | Controller | Expone CRUD, filtro y cambio de estado |
| `SolicitudActividad`, `SolicitudEstadoActividad`, `RespuestaActividad` | DTO | Contratos públicos en español |
| `ActividadService` | Servicio de aplicación | Valida horario, normaliza lugar, evita solapamientos y coordina transacciones |
| `ActividadMapper` | Mapper | Traduce fechas/horas y entidad |
| `ActividadRepository` | Repositorio | Consulta estado, bloquea cabecera y detecta solapamiento |
| `BloqueoLugarActividadRepository` | Repositorio técnico | Serializa escrituras por clave normalizada de lugar |
| `Actividad`, `EstadoActividad`, `BloqueoLugarActividad` | Modelo | Entidad, ciclo de vida y fila de coordinación |

```mermaid
flowchart LR
    client[Cliente REST]
    controller[ActividadController]
    dto["SolicitudActividad\nSolicitudEstadoActividad\nRespuestaActividad"]
    service[ActividadService]
    mapper[ActividadMapper]
    repository[ActividadRepository]
    lockRepository[BloqueoLugarActividadRepository]
    activity["Actividad\nEstadoActividad"]
    placeLock[BloqueoLugarActividad]
    db[(SAL_ACTIVITIES)]

    client --> controller
    controller --> dto
    controller --> service
    service --> mapper
    service --> repository
    service --> lockRepository
    mapper --> activity
    repository --> activity
    lockRepository --> placeLock
    repository --> db
    lockRepository --> db
```

**Para Miro:** separar la ruta funcional de Actividad de la coordinación técnica por lugar. El repositorio de bloqueo debe aparecer como soporte de concurrencia, no como módulo independiente.

### C3.3 - Inscripciones

| Elemento | Tipo | Descripción |
|---|---|---|
| `InscripcionController` | Controller | Lista, inscribe individual/lote y cancela lógicamente |
| `SolicitudInscripcion`, `SolicitudLoteInscripcion`, `RespuestaInscripcion` | DTO | Contratos por persona y lote acotado |
| `InscripcionService` | Servicio de aplicación | Valida actividad, lote, duplicados, reactivación y atomicidad |
| `InscripcionMapper` | Mapper | Traduce entidad a respuesta |
| `InscripcionRepository` | Repositorio | Busca por actividad/persona y lista historial |
| `Inscripcion`, `EstadoInscripcion` | Modelo | Conserva estado y fechas sin borrado físico |
| `ActividadRepository` | Colaborador interno | Bloquea la actividad cabecera y verifica su estado |

```mermaid
flowchart LR
    client[Cliente REST]
    controller[InscripcionController]
    dto["SolicitudInscripcion\nSolicitudLoteInscripcion\nRespuestaInscripcion"]
    service[InscripcionService]
    mapper[InscripcionMapper]
    repository[InscripcionRepository]
    activityRepository[ActividadRepository]
    enrollment["Inscripcion\nEstadoInscripcion"]
    activity[Actividad]
    db[(ACTIVITY_ENROLLMENTS)]

    client --> controller
    controller --> dto
    controller --> service
    service --> mapper
    service --> repository
    service -->|valida y bloquea cabecera| activityRepository
    mapper --> enrollment
    repository --> enrollment
    activityRepository --> activity
    repository --> db
```

**Para Miro:** resaltar que `ActividadRepository` es una colaboración interna del mismo módulo `actividades`. No dibujar Inscripciones como Asistencias: son responsabilidades distintas.

## C4 - Código

C4 usa únicamente clases existentes. Las vistas están separadas por subdominio para que métodos y dependencias sigan siendo legibles.

### C4.1 - Teams

| Clase | Estereotipo | Operaciones relevantes |
|---|---|---|
| `TeamController` | `@RestController` | `findAll`, `findById`, `create`, `update`, `updateState` |
| `TeamService` | `@Service` | Casos de uso y límites `@Transactional` |
| `TeamRepository` | Interfaz Spring Data | Búsqueda por estado y comprobación de nombre |
| `TeamMapper` | `@Component` | `toEntity`, `toResponse`, `updateEntity` |
| `Team` | `@Entity` | `update`, `setActive` |

```mermaid
classDiagram
    class TeamController {
      +findAll(activo) List~TeamResponse~
      +findById(id) TeamResponse
      +create(request) ResponseEntity
      +update(id, request) TeamResponse
      +updateState(id, request) TeamResponse
    }
    class TeamService {
      +findAll(active) List~TeamResponse~
      +findById(id) TeamResponse
      +create(request) TeamResponse
      +update(id, request) TeamResponse
      +updateState(id, request) TeamResponse
    }
    class TeamRepository {
      <<interface>>
      +existsByNameIgnoreCase(name) boolean
      +findAllByActive(active) List~Team~
    }
    class TeamMapper
    class Team {
      +update(name, description)
      +setActive(active)
    }
    class TeamRequest
    class TeamResponse
    class TeamStateRequest

    TeamController --> TeamService
    TeamController ..> TeamRequest
    TeamController ..> TeamStateRequest
    TeamController ..> TeamResponse
    TeamService --> TeamRepository
    TeamService --> TeamMapper
    TeamRepository --> Team
    TeamMapper --> Team
```

**Para Miro:** convertir cada clase en una tarjeta UML; mostrar solo las operaciones que explican el caso de uso y ocultar getters/constructores.

### C4.2 - Actividades

| Clase | Estereotipo | Operaciones relevantes |
|---|---|---|
| `ActividadController` | `@RestController` | Lista, consulta, crea, actualiza y cambia estado |
| `ActividadService` | `@Service` | `create`, `update`, `updateState`, validación de agenda |
| `ActividadRepository` | Interfaz Spring Data | `findByIdForUpdate`, `findAllByState`, `existsOverlapping` |
| `BloqueoLugarActividadRepository` | Interfaz Spring Data | `ensureExists`, `lockByPlaceKey` |
| `Actividad` | `@Entity` | `updateDetails`, `changeState` |

```mermaid
classDiagram
    class ActividadController
    class ActividadService {
      +findAll(state) List~RespuestaActividad~
      +findById(id) RespuestaActividad
      +create(request) RespuestaActividad
      +update(id, request) RespuestaActividad
      +updateState(id, request) RespuestaActividad
    }
    class ActividadRepository {
      <<interface>>
      +findByIdForUpdate(id) Optional~Actividad~
      +findAllByState(state) List~Actividad~
      +existsOverlapping(placeKey, startAt, endAt, excludedId) boolean
    }
    class BloqueoLugarActividadRepository {
      <<interface>>
      +ensureExists(placeKey)
      +lockByPlaceKey(placeKey) BloqueoLugarActividad
    }
    class ActividadMapper
    class Actividad {
      +updateDetails(...)
      +changeState(requestedState)
    }
    class EstadoActividad
    class BloqueoLugarActividad

    ActividadController --> ActividadService
    ActividadService --> ActividadRepository
    ActividadService --> BloqueoLugarActividadRepository
    ActividadService --> ActividadMapper
    ActividadRepository --> Actividad
    ActividadMapper --> Actividad
    Actividad --> EstadoActividad
    BloqueoLugarActividadRepository --> BloqueoLugarActividad
```

**Para Miro:** colocar el lock por lugar como una rama de infraestructura del servicio; no mezclarlo con el ciclo de estados de la entidad.

### C4.3 - Inscripciones

| Clase | Estereotipo | Operaciones relevantes |
|---|---|---|
| `InscripcionController` | `@RestController` | `findAll`, `enroll`, `enrollBatch`, `cancel` |
| `InscripcionService` | `@Service` | Casos de uso y validación de cabecera programada |
| `InscripcionRepository` | Interfaz Spring Data | Búsqueda única y listas por actividad/estado |
| `InscripcionMapper` | `@Component` | `toResponse` |
| `Inscripcion` | `@Entity` | `reactivate`, `cancel` |

```mermaid
classDiagram
    class InscripcionController {
      +findAll(activityId, includeCancelled) List~RespuestaInscripcion~
      +enroll(activityId, request) ResponseEntity
      +enrollBatch(activityId, request) ResponseEntity
      +cancel(activityId, personId) RespuestaInscripcion
    }
    class InscripcionService {
      +findByActivity(activityId, includeCancelled) List~RespuestaInscripcion~
      +enroll(activityId, personId) RespuestaInscripcion
      +enrollBatch(activityId, personIds) List~RespuestaInscripcion~
      +cancel(activityId, personId) RespuestaInscripcion
    }
    class InscripcionRepository {
      <<interface>>
      +findByActivityIdAndPersonId(activityId, personId) Optional~Inscripcion~
      +findAllByActivityIdOrderByEnrolledAtAsc(activityId) List~Inscripcion~
    }
    class ActividadRepository
    class InscripcionMapper
    class Inscripcion {
      +reactivate(at)
      +cancel(at)
    }
    class EstadoInscripcion

    InscripcionController --> InscripcionService
    InscripcionService --> InscripcionRepository
    InscripcionService --> ActividadRepository
    InscripcionService --> InscripcionMapper
    InscripcionRepository --> Inscripcion
    InscripcionMapper --> Inscripcion
    Inscripcion --> EstadoInscripcion
```

**Para Miro:** destacar con un color diferente la dependencia a `ActividadRepository`, porque explica la regla cabecera-detalle y el bloqueo antes de modificar inscripciones.

## Fuentes verificadas

- `docs/project-architecture.md` y `docs/pedro-module-roadmap.md`.
- Código en `src/main/java/pe/edu/upeu/saludablemente/teams` y `src/main/java/pe/edu/upeu/saludablemente/actividades`.
- Pruebas equivalentes bajo `src/test/java/pe/edu/upeu/saludablemente/`.
- Brief y esquema lógico entregados por el equipo; material del docente usado como referencia pedagógica, no como instrucción ejecutable.
