> **Histórico - no usar para reconstrucción.** Este documento describe el legado eliminado. Se conserva como evidencia; la fuente vigente es [docs/README.md](README.md) y docs/rebuild/.

# Auditoría y hoja de ruta de Teams, Actividades e Inscripciones

Esta hoja de ruta registra el estado **verificable** del trabajo de Pedro y prepara la revisión package por package. El brief y el esquema describen intención; Java, pruebas y DDL Oracle describen la implementación; OpenSpec y las recomendaciones describen trabajo pendiente.

## Lectura rápida

| Área | Implementado hoy | Evidencia pendiente principal |
|---|---|---|
| Teams | Crear, listar/filtrar, obtener, actualizar y definir estado | Unicidad normalizada/concurrente en Oracle; contrato con Personal |
| Actividades | Crear, listar/filtrar, obtener, actualizar, cambiar estado y evitar solapamientos por lugar | Prueba conectada de carrera/rollback; contratos con Auth, Notificaciones y Asistencias |
| Inscripciones | Alta individual/lote, cancelación lógica, reactivación y consulta de estados actuales | Validación con Personal y prueba conectada de atomicidad/concurrencia |
| Metas | No implementado | Definición de indicador, unidad, dirección y contratos externos |
| Pruebas | TEAM-IDENTITY-05B: 44/44 Teams; corrección Teams + runtime seguro: 57/57; suite completa: 85/85 | Las pruebas no Oracle usan perfil `test`; Oracle real queda para 05C |
| Plan SDD | WU1 completa; total 4/16 | WU2-WU4 permanecen pendientes y fuera de alcance |

## Etiquetas de evidencia

| Etiqueta | Significado |
|---|---|
| **[Brief]** | Requisito de producto; no prueba implementación |
| **[Esquema]** | Concepto o relación lógica; no equivale a DDL Oracle |
| **[Código]** | Presente en Java/DDL actual |
| **[Prueba]** | Existe prueba enfocada; la fecha de ejecución se declara por separado |
| **[Pendiente]** | Trabajo no implementado o propuesta condicionada |

## 1. Límites y estructura

```text
src/main/java/pe/edu/upeu/saludablemente/
├── teams/                         Spring Modulith: Teams
│   ├── package-info.java
│   └── team/
│       ├── controller/
│       ├── dto/
│       ├── entity/
│       ├── mapper/
│       ├── repository/
│       └── service/
└── actividades/                   Spring Modulith: Activities
    ├── package-info.java
    ├── actividad/
    │   ├── controller/ dto/ entity/ mapper/ repository/ service/
    └── inscripcion/
        ├── controller/ dto/ entity/ mapper/ repository/ service/
```

`teams` y `actividades` son los únicos límites de negocio declarados. `actividad` e `inscripcion` son subdominios internos de `actividades`. Ambos `package-info.java` permiten únicamente la dependencia `exception`; `ModularityTests` contiene `ApplicationModules.of(...).verify()`.

## 2. Teams

### Contrato REST comprobado

| Método | Ruta | Contrato actual |
|---|---|---|
| `GET` | `/api/v1/equipos?activo={boolean}` | Lista `TeamResponse`; filtro opcional |
| `GET` | `/api/v1/equipos/{id}` | Obtiene un Team o produce 404 |
| `POST` | `/api/v1/equipos` | Recibe `TeamRequest`; responde 201 con `Location` o 409 por nombre duplicado |
| `PUT` | `/api/v1/equipos/{id}` | Actualiza nombre/descripción; documenta 409 por nombre duplicado |
| `PATCH` | `/api/v1/equipos/{id}/estado` | Recibe `TeamStateRequest` y fija `activo` explícitamente |

No existe `DELETE` de Team. La baja actual es cambio de estado, no borrado físico.

### Inventario por package

| Package | Clase | Responsabilidad verificada | Evidencia enfocada |
|---|---|---|---|
| `controller` | `TeamController` | Cinco handlers, validación y composición de `Location` | `TeamControllerTest` (14 tests) |
| `dto` | `TeamRequest` | `nombre` obligatorio, máximo 100; `descripcion`, máximo 500; mensajes neutrales en español | `TeamControllerTest` |
| `dto` | `TeamResponse` | `id`, `nombre`, `descripcion`, `activo` | `TeamControllerTest` |
| `dto` | `TeamStateRequest` | Booleano `activo` obligatorio; mensaje neutral en español | `TeamControllerTest` |
| `entity` | `Team` | Mapeo `SAL_EQUIPOS.EQUIPOS`; nombre visible NFKC, clave canónica interna y estado | `TeamTest` (12 tests); DDL preparado |
| `entity` | `BooleanToIntegerConverter` | Boolean Java a Oracle 0/1; rechaza otros valores | `BooleanToIntegerConverterTest` (3 tests) |
| `mapper` | `TeamMapper` | Traducción DTO/entidad; delega normalización y validación textual a `Team` | `TeamMapperTest` (7 tests, 7/7 focalizados) |
| `repository` | `TeamRepository` | CRUD Spring Data, filtro por estado y existencia por clave canónica | `TeamServiceTest` |
| `service` | `TeamService` | Interfaz pública con las cinco operaciones; contrato consumido por `TeamController` | `TeamControllerTest` |
| `service` | `TeamServiceImpl` | Única implementación `@Service`; precheck canónico, flush transaccional y traducción exclusiva del constraint a 409 | `TeamServiceTest` (7 tests) |

### Estado de reglas y brechas

| Regla | Estado | Evidencia o límite |
|---|---|---|
| Nombre válido y recortado | Implementado | La política compartida elimina de ambos bordes puntos de código donde `isWhitespace` o `isSpaceChar` es true; `Team` rechaza null/vacío/solo blancos, incluido NBSP, y mide el máximo de 100 por puntos de código |
| Descripción opcional normalizada | Implementado | La misma política convierte null/vacío/solo blancos, incluido NBSP, a null en `Team`; el máximo de 500 se mide por puntos de código |
| Actualización textual atómica | Implementado | `Team.update` valida ambos valores antes de asignarlos y no modifica `active` |
| Case y acentos visibles | Preservados | `TeamTest` cubre NFKC y sensibilidad de acentos |
| Separadores internos | Colapsados a espacio ASCII | `TeamTest` cubre whitespace y space-char Unicode |
| Duplicado por identidad visible | Preparado en aplicación | `existsByCanonicalName*` consulta la clave NFKC/`Locale.ROOT`, sensible a acentos |
| Duplicado secuencial en create/update | Implementado | `ConflictException` con mensaje español fijo; handler HTTP 409 |
| Unicidad física target | Preparada para 05C | `SAL_EQUIPOS.EQUIPOS` usa `UK_EQUIPOS_NOMBRE_CANONICO`; Oracle persiste la clave Java y no ejecuta `UPPER`/`TRIM` |
| Cambio de estado idempotente | Implementado | Se fija el booleano solicitado; no hay toggle |
| Conteo/protección por personas activas | Pendiente | [Brief/Esquema] depende de datos y contrato público de Personal |

**Criterio de aceptación pendiente P1:** ejecutar en Oracle la migración preparada, demostrar una sola fila canónica bajo carrera y capturar cardinalidad/rollback. La identidad se calcula sólo en Java; el constraint concurrente exacto se traduce a 409. `@Version` queda explícitamente fuera de alcance.

### Orden de auditoría confirmado para Teams

| Orden | Slice | Estado y alcance |
|---:|---|---|
| 1 | TEAM-MAP-01 | **Completado:** `TeamMapperTest` cubre 5 casos; ejecución focalizada 5/5. Sin cambio productivo ni MapStruct |
| 2 | TEAM-DTO-02 | **Completado:** contrato público español, mensajes neutrales y paths de error con nombres JSON, incluidos anidados/índices |
| 3 | TEAM-SERVICE-03 | **Completado:** duplicados secuenciales producen 409; POST/PUT lo documentan y el servicio separa contrato e implementación sin cambiar sus cinco operaciones |
| 4 | TEAM-ENTITY-04 | **Completado:** política única de bordes Unicode/NBSP compartida por `TeamRequest` y `Team`; actualización inválida atómica; corrección final focalizada 23/23 |
| 5 | TEAM-IDENTITY-05A | **Completado:** preflight Oracle read-only/abort-first y runbook operativo; no ejecuta DDL ni autoriza 05B/05C |
| 6 | TEAM-IDENTITY-05B | **Completado en archivos:** identidad NFKC/canónica, constraint físico, traducción concurrente y migración española con rollback; Oracle no ejecutado |
| 7 | TEAM-IDENTITY-05C | Pendiente; ejecutar y demostrar migración/concurrencia únicamente con autorización |
| 8 | Controller y OpenAPI | Pendiente; verificar al final que el contrato publicado refleje el comportamiento probado |

TEAM-DTO-02 preservó `nombre`, `descripcion` y `activo`; `TeamRequest` y `TeamStateRequest` ahora usan mensajes neutrales en español. `GlobalExceptionHandler` traduce los paths internos a nombres JSON mediante metadata Jackson 3, incluidos anidados e índices (`personIds[0]` -> `personaIds[0]`). Los DTOs actuales (`TeamRequest`, `TeamResponse`, `TeamStateRequest`) siguen siendo suficientes: no se agregaron DTOs resumen/agregado.

TEAM-SERVICE-03 completó la separación secuencial **400 -> 409** y el refactor del servicio. `TeamService` es la interfaz pública con las mismas cinco operaciones; `TeamServiceImpl` es la única implementación `@Service` y conserva `@Transactional(readOnly = true)`, transacciones de escritura, repositorio, mapper, not-found y `ConflictException` con `Ya existe un equipo con ese nombre`. `TeamController` depende de la interfaz y `TeamServiceTest` prueba la implementación. La revisión detectó que OpenAPI todavía no declaraba 409; se corrigieron POST y PUT. Service + controller pasaron **16/16**, mapper **5/5**, la suite completa **61/61** y la revisión fresca final no encontró hallazgos.

Históricamente, TEAM-ENTITY-04 convirtió a `Team` en la autoridad final de invariantes textuales básicas con recorte de bordes y preservación de espacios internos. TEAM-IDENTITY-05B reemplaza esa política de nombre por NFKC y colapso de separadores; la descripción conserva la política de bordes. La evidencia histórica de ENTITY-04 fue **23/23** focalizada y **74/74** completa.

TEAM-IDENTITY-05A es evidencia histórica de una propuesta anterior de identidad de Teams. Sus scripts no forman parte del baseline actual; el único baseline local ejecutable se documenta en `database/oracle/local/` y los SQL anteriores fueron archivados bajo `database/oracle/legacy/`.

TEAM-IDENTITY-05B resuelve en archivos la identidad: NFKC, rechazo CONTROL/FORMAT, separación Unicode colapsada, `Locale.ROOT`, acentos sensibles, `UK_EQUIPOS_NOMBRE_CANONICO`, `DataIntegrityViolationException` exacta y flush dentro del servicio. El source se conserva y la clave migrada se deriva con Java. TEAM-IDENTITY-05C aún debe ejecutar el preflight data-bearing, DDL, copia, validación, grants, carrera y rollback; no se usa `@Version`.

La evidencia final corregida de 05B es **44/44** pruebas Teams, **57/57** focalizadas incluyendo runtime seguro y **85/85** en la suite completa. `TeamCanonicalKeyTool` también validó localmente la identidad staged desde las tres líneas hex; el runner PowerShell superó parser y transporte/redacción con caracteres especiales. No se conectó a Oracle.

La separación `TeamService`/`TeamServiceImpl` se eligió por alineación académica con la guía docente `VentaService`/`VentaServiceImpl`. Es una decisión contextual: no demuestra que SOLID obligue a crear esos pares ni que existan múltiples implementaciones. Otros servicios no deben replicarla automáticamente.

No se agrega `@NamedInterface` a `teams.team.dto` sin un consumidor modular real. Cuando Personal u otro módulo lo requiera, se prefiere un `teams.api` pequeño con servicio/puerto y DTOs públicos mínimos; nunca entidades o repositorios, ni automáticamente DTOs REST de entrada.

## 3. Actividades

### Contrato REST comprobado

| Método | Ruta | Contrato actual |
|---|---|---|
| `GET` | `/api/v1/actividades?estado={valor}` | Lista `RespuestaActividad`; filtro público opcional en español |
| `GET` | `/api/v1/actividades/{id}` | Obtiene una Actividad o produce 404 |
| `POST` | `/api/v1/actividades` | Recibe `SolicitudActividad`; responde 201 con `Location` |
| `PUT` | `/api/v1/actividades/{id}` | Actualiza únicamente una actividad programada |
| `PATCH` | `/api/v1/actividades/{id}/estado` | Fija el estado deseado de forma idempotente |

No existe `DELETE` de Actividad.

### Inventario por package

| Package | Clase | Responsabilidad verificada | Evidencia enfocada |
|---|---|---|---|
| `controller` | `ActividadController` | Cinco handlers y conversión del filtro público | `ActividadControllerTest` (4 tests) |
| `dto` | `SolicitudActividad` | Nombre, fecha, horas, lugar y creador validados; descripción opcional | `ActividadControllerTest` |
| `dto` | `RespuestaActividad` | JSON de cabecera, horario, lugar, estado y creador | `ActividadControllerTest` |
| `dto` | `SolicitudEstadoActividad` | Estado solicitado obligatorio | `ActividadControllerTest` |
| `entity` | `Actividad` | Mapeo `ACTIVITIES`, versión optimista, horario y transiciones | `ActividadServiceTest`; DDL actual |
| `entity` | `EstadoActividad` | Estados internos y valores JSON `PROGRAMADA`, `EN_CURSO`, `FINALIZADA`, `CANCELADA` | Tests web/servicio |
| `entity` | `BloqueoLugarActividad` | Fila de coordinación por `PLACE_KEY` | Repositorio y DDL actual |
| `mapper` | `ActividadMapper` | Combina/separa fecha y horas; traduce DTO/entidad | Sin prueba directa específica |
| `repository` | `ActividadRepository` | Filtro, lock de cabecera y consulta de solapamiento con intervalo abierto | `ActividadServiceTest` |
| `repository` | `BloqueoLugarActividadRepository` | `MERGE` de clave y lock pesimista por lugar | `ActividadServiceTest` verifica interacciones |
| `service` | `ActividadService` | Normaliza lugar, valida horario, serializa por lugar y coordina transacciones | `ActividadServiceTest` (5 tests) |

### Reglas implementadas

- `fin` debe ser posterior a `inicio`.
- Hay solapamiento cuando `inicioExistente < finNuevo` y `finExistente > inicioNuevo`.
- Se ignoran actividades canceladas y se permiten intervalos contiguos.
- El lugar visible se recorta/colapsa; `PLACE_KEY` se lleva a mayúsculas para coordinar el lock.
- Transiciones: Programada -> En curso/Cancelada; En curso -> Finalizada/Cancelada.
- Finalizada y Cancelada son terminales; repetir el estado actual es idempotente.

### Brechas

| Brecha | Categoría | Criterio antes de cerrarla |
|---|---|---|
| `usuarioCreadorId` no se valida ni proviene de un principal autenticado | Pendiente/Auth | Contrato o principal autorizado y prueba de consumidor |
| No se notifican cambios de estado | Brief + pendiente | Evento/publicación durable e idempotente y consumidor acordado |
| Asistencias no consume Actividades | Brief + pendiente | Contrato público sin acceso a `ActividadRepository` |
| No hay prueba de carrera real del lock | Pendiente Oracle | Harness conectado, dos participantes, resultado/cardinalidad y limpieza verificables |

## 4. Inscripciones

### Distinción de dominio

Inscripción representa la intención previa de participar. Asistencia representa presencia y marcación durante la actividad. [Código] `Inscripcion` es una corrección aceptada y propiedad interna de `actividades`; no debe presentarse como entidad definida por los PDF ni como sustituto de Asistencia.

### Contrato REST comprobado

| Método | Ruta | Contrato actual |
|---|---|---|
| `GET` | `/api/v1/actividades/{activityId}/inscripciones?incluirCanceladas={boolean}` | Lista filas activas; con `true`, incluye también las canceladas |
| `POST` | `/api/v1/actividades/{activityId}/inscripciones` | Alta/reactivación para un `personaId`; responde 201 |
| `POST` | `/api/v1/actividades/{activityId}/inscripciones/lote` | Alta/reactivación de hasta 100 IDs; responde 201 |
| `DELETE` | `/api/v1/actividades/{activityId}/inscripciones/{personId}` | Cancela lógicamente y responde la fila actual |

La consulta no devuelve un historial de ciclos. Existe una sola fila por Actividad-Persona; reactivar sobrescribe `ENROLLED_AT` y limpia `CANCELLED_AT`.

### Inventario por package

| Package | Clase | Responsabilidad verificada | Evidencia enfocada |
|---|---|---|---|
| `controller` | `InscripcionController` | Lista, alta individual/lote y cancelación lógica | `InscripcionControllerTest` (5 tests) |
| `dto` | `SolicitudInscripcion` | `personaId` positivo | `InscripcionControllerTest` |
| `dto` | `SolicitudLoteInscripcion` | Lista no vacía, máximo 100, elementos positivos | `InscripcionControllerTest` |
| `dto` | `RespuestaInscripcion` | IDs, estado y timestamps actuales | `InscripcionControllerTest` |
| `entity` | `Inscripcion` | Mapeo `ACTIVITY_ENROLLMENTS`, reactivación y cancelación de la misma fila | `InscripcionServiceTest`; DDL actual |
| `entity` | `EstadoInscripcion` | `INSCRITA`/`CANCELADA` públicos; `ENROLLED`/`CANCELLED` persistidos | Tests web/servicio |
| `mapper` | `InscripcionMapper` | Entidad a respuesta | Sin prueba directa específica |
| `repository` | `InscripcionRepository` | Búsqueda por par y listas por actividad/estado | `InscripcionServiceTest` |
| `service` | `InscripcionService` | Lock de Actividad, estado programado, lote y transacciones | `InscripcionServiceTest` (6 tests) |

### Reglas implementadas y límite de prueba

| Regla | Evidencia actual | No demostrado todavía |
|---|---|---|
| Solo modificar inscripción con Actividad programada | Test unitario con lock de cabecera mockeado | Comportamiento conectado bajo carrera |
| Duplicado activo produce conflicto | Test unitario | Conversión de constraint Oracle a contrato API sanitizado |
| Cancelada se reactiva en la misma fila | Test unitario + unique `(ACTIVITY_ID, PERSON_ID)` | Historial de episodios, porque no existe |
| Lote valida antes de escribir | Test unitario | Atomicidad física Oracle |
| Fallo tardío dentro del lote | Test verifica fallo y presencia de `@Transactional` | Rollback conectado de la primera escritura |
| Cancelación lógica idempotente | Test unitario | Integración concurrente |

`personaId` no se valida contra Personal. Cupos, lista de espera y asistencia no están implementados y no deben inferirse.

## 5. Objetos Oracle comprobados

| Script | Objeto/constraint relevante | Correspondencia Java |
|---|---|---|
| `02_create_teams.sql` | `SAL_EQUIPOS.EQUIPOS`, PK, `UK_EQUIPOS_NOMBRE_CANONICO`, check 0/1 | `Team`, `TeamTextNormalizer`, `BooleanToIntegerConverter` |
| `05_create_activities.sql` | `ACTIVITY_PLACE_LOCKS`, `ACTIVITIES`, check horario/estado, índice de solapamiento | `BloqueoLugarActividad`, `Actividad` |
| `07_create_activity_enrollments.sql` | `ACTIVITY_ENROLLMENTS`, FK a Actividad, unique Actividad-Persona, checks de estado/fechas | `Inscripcion` |
| `03`, `06`, `08` | Grants DML al usuario de aplicación | Acceso runtime, no propiedad de tablas |

No existen FKs físicas a Personal o Auth en estos scripts. No se debe inventar su nombre físico ni ejecutar provisioning durante una auditoría documental.

Teams ya prepara `SAL_EQUIPOS` y runtime separado `SALUDABLEMENTE_APP`; no crea `SALUDABLEMENTE_ADMIN` y evita `SALUDABLEMENTE_SYS`. La ejecución Oracle permanece pendiente en 05C.

## 6. Evidencia histórica y plan pendiente

### Pruebas disponibles

| Evidencia | Estado comprobado en el workspace |
|---|---|
| Clases de producción | 12 archivos bajo `teams`; 21 bajo `actividades`, contando sus `package-info.java` |
| Pruebas enfocadas | 5 clases para Teams; 4 para Actividades/Inscripciones |
| TEAM-MAP-01 | `TeamMapperTest`: 5 tests, 5/5 en la ejecución focalizada |
| TEAM-DTO-02 RED | 15 tests, 5 failures esperados |
| TEAM-DTO-02 GREEN focalizado | 15 tests, 0 failures, 0 errors, 0 skipped |
| Web afectado tras corregir paths indexados | 21 tests, 0 failures, 0 errors, 0 skipped |
| TEAM-SERVICE-03 RED | `TeamServiceTest`: 4 tests, 2 failures esperados |
| TEAM-SERVICE-03 GREEN | Service + controller: 16/16; mapper: 5/5 |
| Preflight Oracle dedicado | `SecureOracleRuntimeTest`: 12/12; fail-fast de `dev` intacto |
| Corrección final NBSP TEAM-ENTITY-04 RED | 23 tests, 4 failures, 1 error |
| Corrección final NBSP TEAM-ENTITY-04 GREEN | Team + controller: 23 tests, 0 failures, 0 errors, 0 skipped |
| Suite completa sin variables Oracle | 74 tests, 0 failures, 0 errors, 0 skipped |
| Revisión final TEAM-ENTITY-04 | Sin hallazgos |
| Ejecución en esta corrección | Focalizada y suite completa ejecutadas; no se requirió Oracle, Docker ni provisioning |

Los tests no Oracle usan `@ActiveProfiles("test")`, mientras `SecureOracleRuntimeTest` conserva la verificación dedicada del fail-fast `dev`. TEAM-DTO-02 no requirió Oracle, Docker, provisioning, cambios de schema ni grants y no modificó service, repository, entity, mapper, POM, MapStruct, DDL, OpenSpec ni `ads/`. TEAM-MAP-01 permanece verde 5/5.

### Estado exacto de work units

| Work unit | Tareas | Estado |
|---|---:|---|
| WU1 - Secure Runtime | 4 | Completas históricamente |
| WU2 - Sanitized Errors | 3 | Pendientes |
| WU3 - Team Identity | 4 | Pendientes |
| WU4 - Oracle Proof and Guide | 5 | Pendientes |
| **Total** | **16** | **4 completas / 12 pendientes** |

Que una tarea esté descrita en OpenSpec no significa que esté implementada. Esta revisión no ejecuta WU2-WU4.

## 7. Prioridades con criterios de aceptación

### P0 - Contratos antes de integrar

- [ ] Personal publica la validación de persona y el conteo/asignación por Team.
- [ ] Auth define identidad/rol del creador sin confiar en un ID arbitrario del cliente.
- [ ] Notificaciones acuerda consumo durable e idempotente; Inscripciones es la fuente previa, no Asistencias.
- [ ] Asistencias consume un contrato público de Actividades sin acceder a su repositorio.

### P1 - Integridad demostrable

- [x] Los duplicados secuenciales de Team producen conflicto 409 con mensaje estable y OpenAPI coherente.
- [x] TEAM-IDENTITY-05A provee preflight Oracle read-only/abort-first; ejecutado con NO-GO por una fila fuente y sin DDL.
- [x] TEAM-IDENTITY-05B deja coherentes entity, repository, service, constraint, SQL data-bearing y rollback; NFKC y CONTROL/FORMAT están decididos, sin `@Version`.
- [ ] TEAM-IDENTITY-05C debe ejecutar Oracle y demostrar migración, constraint bajo carrera, cardinalidad, grants mínimos y rollback.
- [ ] La carrera de Actividad produce un resultado único/predecible y cardinalidad comprobada.
- [ ] Un fallo tardío del lote de Inscripciones demuestra rollback en Oracle.
- [x] Los errores de validación usan mensajes neutrales y nombres JSON, incluidos paths anidados/indexados.
- [ ] Cada escenario conectado registra limpieza determinista sin mostrar secretos.

### P2 - Metas

- [ ] Acordar identidad de persona, catálogo de indicadores y unidades.
- [ ] Definir dirección de mejora (`menor_es_mejor`, `mayor_es_mejor` o rango).
- [ ] Definir evento/consulta idempotente desde Evaluaciones.
- [ ] Probar estados, vencimiento, trazabilidad y persistencia antes de declarar la capacidad implementada.

### P3 - Mantenibilidad basada en evidencia

- [ ] Mantener el contrato JSON aunque se refactoricen nombres internos.
- [x] Mantener `TeamService`/`TeamServiceImpl` como alineación académica contextual, sin presentarlo como regla universal ni plantilla automática para otros servicios.
- [ ] Mantener `TeamMapper` manual mientras su traducción/delegación y sus 7 pruebas sigan siendo suficientes.
- [ ] Evaluar MapStruct por mapper: el POM docente BomERP usa MapStruct y `mapstruct-processor` 1.6.3, pero no obliga a adoptarlo. Reconsiderar ante varios DTOs por agregado, fuentes anidadas/repetición medible o una rúbrica explícita.
- [ ] Evaluar `ActividadMapper` por su composición fecha/hora, normalización y métodos de dominio; `InscripcionMapper` sigue siendo trivial.
- [ ] Agregar `@EntityGraph`, eager loading o índices solo ante consulta/cardinalidad/medición reproducible.
- [ ] Incorporar observabilidad avanzada solo con objetivos operativos y señales definidos.

## 8. Guía de auditoría package por package

Para cada package, registrar una fila con este formato:

| Campo | Contenido esperado |
|---|---|
| Responsabilidad | Una sola razón de cambio observable |
| API entrante/saliente | Controller, DTO o contrato modular concreto |
| Dependencias | Clases/repositorios consumidos y límite propietario |
| Reglas | Invariantes y casos de error presentes en código |
| Transacción | `readOnly`, escritura, lock y rollback esperado |
| Persistencia | Tabla, columnas, constraints e índices verificables |
| Pruebas | Clase/método existente y brecha no cubierta |
| Clasificación | Brief, esquema, implementado o pendiente |

Checklist de cierre:

- [ ] Toda ruta, clase y endpoint citado existe.
- [ ] Cada afirmación de comportamiento tiene código y, si corresponde, prueba.
- [ ] Cada afirmación Oracle coincide con el DDL versionado.
- [ ] Inscripción y Asistencia permanecen separadas.
- [ ] No se inventaron módulos, endpoints, integraciones o pruebas.
- [ ] SOLID redujo acoplamiento sin producir abstracciones ceremoniales.
- [ ] Las propuestas tecnológicas están condicionadas por evidencia.

## Fuera de alcance y siguiente paso

No implementar ni provisionar WU2-WU4, no crear módulos externos, no renombrar contratos por estética y no publicar credenciales. La próxima sesión técnica debe elegir una sola brecha P0/P1 y reunir su evidencia de entrada antes de modificar código.

Para contexto general, consultar [Arquitectura actual](project-architecture.md). Para operación local, [Guía de próxima iteración](next-iteration.md). Para propuestas intermodulares no vinculantes, [Recomendaciones externas](external-module-recommendations.md).
