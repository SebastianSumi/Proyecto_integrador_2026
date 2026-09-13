# Requisitos y fundamento del módulo Actividades

**Estado.** Actividades está implementado en Java como dos submódulos: `actividad` e `inscripcion`. Este documento reúne los requisitos confirmados, las decisiones aplicadas y los pendientes deliberados para que el equipo pueda revisar e integrar el módulo sin deducir comportamientos no implementados.

> **Lectura rápida:** el módulo programa actividades y registra inscripciones previas. Rechaza horarios que se solapan en el mismo lugar y fecha, evita una segunda inscripción vigente en el flujo normal y conserva las cancelaciones como historial. Las garantías concurrentes de inscripción y horarios solo entran en vigor después de ejecutar manualmente `V001` y `V002` en Oracle.

## 1. Propósito y límites

| Tema | Requisito o decisión | Motivo |
|---|---|---|
| Propósito | Programar actividades y gestionar su inscripción previa. | Corresponde al alcance funcional aprobado del módulo. |
| Agregado de programación | `Actividad` contiene nombre, fecha, horario, lugar, estado y `creadorId`. | Representa los hechos necesarios para programar. |
| Inscripción | `Inscripcion` representa la participación previa de una persona en una actividad. | Permite registrar y cancelar sin perder el historial. |
| Asistencia | No es hija JPA de Actividad ni Inscripción. | Pertenece a otro módulo; cualquier colaboración futura será mediante servicio público. |
| Referencias externas | `creadorId`, `actividadId` y `personaId` son IDs escalares. | Evita relaciones JPA y acceso directo a repositorios de otros módulos. |

## 2. Organización y contratos HTTP

Cada submódulo es dueño de sus capas `entity`, `dto`, `mapper`, `repository`, `service`, `controller` y excepciones de negocio. Los controllers usan DTOs, no entidades JPA; los services contienen reglas y fronteras transaccionales; los repositories solo encapsulan persistencia propia.

| Recurso | Operaciones implementadas | Resultado esperado |
|---|---|---|
| `/api/v1/actividades` | `GET`, `GET /{id}`, `POST`, `PUT /{id}` | Lista, consulta, crea y actualiza actividades. |
| `/api/v1/inscripciones` | `GET`, `GET /{id}`, `POST`, `PATCH /{id}/cancelacion` | Lista, consulta, registra y cancela inscripciones. |
| Entrada | `ActividadRequest` e `InscripcionRequest` con `@Valid`. | Rechazo 400 para datos obligatorios o formatos inválidos antes del service. |
| Salida | `ActividadResponse` e `InscripcionResponse`. | El estado y las marcas de ciclo de Inscripción quedan controlados por backend. |

## 3. Reglas funcionales confirmadas

### Actividades

1. El intervalo debe cumplir `horaInicio < horaFin`; si no, la operación es inválida.
2. No se permite crear ni actualizar una actividad si existe otra con el mismo `lugar`, misma `fecha` y un intervalo que se cruce.
3. Los intervalos contiguos no se consideran solapados: una actividad que termina a las 11:00 puede preceder a otra que comienza a las 11:00.
4. Actividades con lugar o fecha distintos pueden coexistir aunque sus horarios coincidan.
5. En una actualización, la actividad actual queda excluida de la consulta de solapamiento; no se rechaza a sí misma.

La consulta de cruce aplica `inicioExistente < finSolicitado` y `finExistente > inicioSolicitado`. Esta definición captura cruces reales sin bloquear los límites contiguos.

### Inscripciones

1. Antes de registrar, debe existir la actividad indicada; de lo contrario se devuelve 404.
2. Una persona solo puede tener una inscripción con estado `INSCRITA` para la misma actividad.
3. Cancelar cambia `INSCRITA` a `CANCELADA` y registra `canceladaEn`.
4. Cancelar por segunda vez es idempotente: devuelve la inscripción cancelada sin cambiar nuevamente `canceladaEn` ni producir efectos adicionales.
5. Una inscripción cancelada no bloquea un registro posterior de la misma persona en la misma actividad.

`InscripcionService` usa `ActividadService.validateExists(...)`, no el repository de Actividad. Así expresa que solo necesita validar existencia y conserva el límite modular público.

## 4. Respuestas de error y responsabilidades

| Situación | Excepción de dominio o transversal | HTTP | Qué recibe la web |
|---|---|---:|---|
| Recurso inexistente | `ResourceNotFoundException` | 404 | Recurso solicitado no encontrado. |
| DTO inválido o intervalo inválido | `MethodArgumentNotValidException` o `BusinessValidationException` | 400 | Solicitud inválida. |
| Horario que se solapa | `ActividadSolapadaException` | 409 | Conflicto con la agenda existente. |
| Inscripción vigente duplicada | `InscripcionVigenteException` | 409 | Conflicto con una inscripción vigente. |

Las excepciones de negocio permanecen en `actividad` o `inscripcion`, porque cada módulo es dueño de su regla. `BusinessValidationException`, `BusinessConflictException`, `ResourceNotFoundException` y `GlobalExceptionHandler` son transversales: el handler traduce las excepciones a una respuesta uniforme con `timestamp`, `status`, `error` y `message`. No conoce ni depende de clases internas de Actividades.

## 5. Transacciones e idempotencia

- Las escrituras de crear, actualizar, registrar y cancelar se ejecutan dentro de transacciones de service.
- La transacción no reemplaza las reglas: el service valida primero y luego persiste.
- La cancelación es una **transición de estado**, no un `DELETE`, porque se necesita conservar el historial y permitir una nueva inscripción posterior.
- El registro conserva la consulta previa de duplicado para una respuesta clara en el caso normal. Esa consulta por sí sola no garantiza seguridad frente a dos requests simultáneos; la base de datos completa esa garantía cuando `V001` está aplicada.

## 6. Datos y concurrencia

### Hecho implementado: unicidad de inscripción activa

El script manual `database/oracle/manual-migrations/V001__enrollment_active_uniqueness.sql` define el índice Oracle basado en función `UK_INSCRIPCION_VIGENTE`. Indexa `actividad_id` y `persona_id` únicamente cuando `estado = 'INSCRITA'`.

**Después de que un responsable autorizado lo ejecute correctamente en Oracle:** la base rechaza una segunda inscripción activa para la misma persona y actividad, incluso si dos requests pasan la validación previa al mismo tiempo. El service usa `saveAndFlush()` para observar la violación dentro de su transacción y solo transforma la violación identificada de ese índice en 409. Otros errores de integridad no se disfrazan de duplicado.

**Antes de la ejecución manual:** el script no tiene efecto y la unicidad concurrente no está garantizada. La carpeta no usa Flyway ni el backend ejecuta DDL automáticamente. El equipo debe confirmar los nombres físicos, resolver duplicados existentes y registrar la ejecución según `database/oracle/manual-migrations/README.md`.

### Hecho implementado: serialización de agenda

`V002__activity_schedule_coordination.sql` crea una fila durable por el `lugar` exacto y fecha, y `LOCK_AGENDA_ACTIVIDAD` la obtiene con `SELECT ... FOR UPDATE`. El service llama ese procedimiento antes de reutilizar la consulta de solapamiento. Al actualizar, bloquea agenda previa y nueva en orden determinista para evitar deadlocks.

Esto no normaliza ni cambia mayúsculas/minúsculas de `lugar`: conserva la igualdad exacta ya definida. La garantía requiere la ejecución manual de `V002` contra el Oracle usado por la aplicación; la evidencia concurrente real debe ejecutarse allí. Ver [Diseño de concurrencia Oracle](15-activities-concurrency-oracle-design.md).

## 7. Fuera de alcance actual

| Pendiente | Razón para no implementarlo todavía |
|---|---|
| Prueba de concurrencia real con agenda Oracle | `V002` debe ejecutarse y verificarse contra Oracle autorizado. |
| Prueba de integración real con Oracle | No existe infraestructura aislada autorizada para repositories ni migraciones. |
| CORS | Es configuración transversal por propiedades de entorno, no responsabilidad de controller, DTO ni `package-info`. |
| Logs transversales | Requiere acuerdo de integración del backend. |
| Reportes o consultas combinadas | No hay requisito funcional confirmado para proyectarlos aún. |
| Operación cabecera-detalle | Actividad–Inscripción solo se construirá si se acuerda una operación atómica real; no se copiará el patrón de Venta por similitud. |
| Exposición Spring Modulith mediante `package-info.java` | Se añadirá cuando exista un contrato externo concreto que deba publicarse; no configura CORS ni HTTP. |

## 8. Evidencia de verificación

La evidencia registrada al cierre de la última mejora es:

- pruebas focalizadas de agenda: **14/14**;
- suite Maven completa: **83/83**;
- `git diff --check`: sin errores de espacios.

Las pruebas cubren entidades, DTOs, mappers, services y controllers de Actividad e Inscripción, incluidos 400, 404, 409, intervalo inválido, solapamiento, actividad inexistente, duplicado vigente y cancelación idempotente. Las pruebas comportamentales de repository y la concurrencia real de Oracle siguen pendientes de infraestructura autorizada; por tanto, esta evidencia no afirma haber ejecutado Oracle.

## 9. Checklist de integración

- [x] Capas Java de Actividad e Inscripción completadas.
- [x] DTOs de entrada validados y entidades no expuestas por HTTP.
- [x] Reglas de intervalo, solapamiento secuencial, existencia y ciclo de inscripción implementadas.
- [x] Script Oracle manual de unicidad activa preparado.
- [ ] `V001` ejecutado y registrado en Oracle por el equipo.
- [x] Garantía concurrente de agenda implementada como V002 + bloqueo de service.
- [ ] `V002` ejecutado y probado contra Oracle por el equipo.
- [ ] CORS, logs, reportes y operación cabecera-detalle acordados e integrados.
