# Producto LP2 Unidad I: Saludablemente

> **Estado de evaluación:** esta guía registra evidencia real del backend de Saludablemente y separa lo verificable de lo pendiente. No reemplaza la demostración en vivo exigida por S06.

## Lectura rápida

- **Implementado en Java:** verticales Teams, Actividades, Inscripciones y Metas, con DTOs, mappers, servicios transaccionales, controllers y pruebas focalizadas.
- **Pendiente para afirmar cumplimiento integral de S06:** Oracle vivo y repetición de las evidencias locales contra Oracle autorizado. El agregado real `EvaluacionAptitud -> DetallePruebaFisica` ya tiene prueba H2 de éxito y rollback; CORS, Spring Modulith y Swagger tienen evidencia local. El ambiente compartido debe configurar sus valores CORS y las consultas/reportes de Actividades requieren demostración con datos reales.
- **Ruta de evidencia:** usar la [matriz S06](rebuild/18-s06-evaluation-traceability.md) durante la demo; no presentar como terminada una fila pendiente.

## 1. Alcance arquitectónico del corte

```text
saludablemente/                         # un único proyecto Maven / Spring Boot
└── src/main/java/pe/edu/upeu/saludablemente/
    ├── SaludablementeApplication.java   # bootstrap único Spring Boot
    ├── teams/team/                     # vertical CRUD de referencia
    ├── actividades/actividad/          # programación de actividades
    ├── actividades/inscripcion/        # registro y cancelación previa
    ├── metas/meta/                     # objetivos de salud supervisados
    └── exception/                      # traducción HTTP transversal
```

El backend es un monolito modular: cada módulo conserva entity, DTO, mapper, repository, service y controller. La comunicación entre módulos se realiza mediante servicios públicos; un módulo no usa directamente el repository de otro. Las entidades JPA no se exponen por HTTP.

| Aspecto | Evidencia actual | Límite honesto |
|---|---|---|
| Proyecto único | Un único `pom.xml` Maven, contratos REST, `SaludablementeApplication` como bootstrap único y metadatos OpenAPI para Swagger. El arranque local H2 verificó health, OpenAPI y Swagger UI con `200`. | Falta arrancar contra Oracle/BD2; la evidencia H2 no demuestra conexión BD2. |
| Modularidad | Paquetes de negocio separados y `ModularityTests` con `ApplicationModules.of(...).verify()` verde (1/1, 2026-09-13). | La prueba refleja la topología actual; el equipo debe acordar límites explícitos antes de ampliar módulos. |
| Persistencia | Entities y `JpaRepository` implementados. | No declarar persistencia Oracle validada hasta la demo conectada. |
| Errores | Handler global y excepciones de dominio con 400/404/409. | No sustituye logs de trazabilidad. |

## 2. Demo ejecutable

La demo S06 debe ejecutarse con el backend conectado al Oracle real del equipo. El orden sugerido y los requisitos de evidencia en vivo están en la [matriz S06](rebuild/18-s06-evaluation-traceability.md#demo-individual-de-pedro).

## 3. Contrato REST actual

| Método | Endpoint | Propósito | Estado |
|---|---|---|---|
| `GET` | `/api/v1/teams` | Listar equipos. | Implementado en Java. |
| `GET` | `/api/v1/teams/{id}` | Consultar un equipo. | Implementado en Java. |
| `POST` | `/api/v1/teams` | Crear un equipo. | Implementado en Java. |
| `PUT` | `/api/v1/teams/{id}` | Actualizar un equipo. | Implementado en Java. |
| `PATCH` | `/api/v1/teams/{id}/state` | Cambiar su estado. | Implementado en Java. |
| `GET` | `/api/v1/actividades` y `/{id}` | Listar y consultar actividades. | Implementado en Java. |
| `POST`, `PUT` | `/api/v1/actividades`, `/{id}` | Crear/actualizar programación; rechaza intervalos inválidos y solapamiento secuencial. | Implementado en Java; garantía concurrente requiere V002 ejecutada en Oracle. |
| `GET` | `/api/v1/actividades/busqueda` | Buscar por estado y rango de fechas, con ordenamiento permitido; por defecto ordena por fecha. | Implementado en Java; demostrar con datos reales. |
| `GET` | `/api/v1/actividades/resumen` | Contar actividades por estado dentro de un rango opcional. | Implementado en Java; demostrar con datos reales. |
| `GET` | `/api/v1/inscripciones` y `/{id}` | Listar y consultar inscripciones. | Implementado en Java. |
| `POST` | `/api/v1/inscripciones` | Registrar inscripción previa. | Implementado en Java; garantía concurrente requiere V001 ejecutada en Oracle. |
| `PATCH` | `/api/v1/inscripciones/{id}/cancelacion` | Cancelar de forma idempotente. | Implementado en Java. |
| `GET` | `/api/v1/metas`, `/persona/{personaId}`, `/{id}` | Consultar metas generales, por padre lógico e individual. | Implementado en Java. |
| `POST`, `PUT` | `/api/v1/metas`, `/{id}` | Crear y actualizar una meta `EN_CURSO`. | Implementado en Java. |
| `PATCH` | `/api/v1/metas/{id}/cumplimiento` | Confirmar cumplimiento supervisado. | Implementado en Java. |
| `DELETE` | `/api/v1/metas/{id}` | Eliminar solo una meta `EN_CURSO`. | Implementado en Java. |

**Cabecera–detalle S06:** `EvaluacionAptitud -> DetallePruebaFisica` registra una solicitud compuesta de forma transaccional, calcula puntaje/diagnóstico y cuenta con una prueba H2 de commit y rollback. Actividad–Inscripción sigue sin presentarse como cabecera–detalle. CORS usa una única configuración transversal para `/api/**`, con `app.cors.*` y sobrescritura por `CORS_*`; no se usa `@CrossOrigin` por controller. El valor local autorizado permite Angular en `http://localhost:4200` sin credenciales; confirmar y configurar el origen del ambiente de demo antes de presentarlo. Las consultas y el resumen existen en Java, pero su evidencia con datos reales sigue pendiente para S06.

## 4. DTO principales y límites de datos

| Módulo | DTOs/decisión principal | Regla demostrable |
|---|---|---|
| Teams | Request/response separados y mapper MapStruct. | HTTP no serializa la entity. |
| Actividades | `ActividadRequest` / `ActividadResponse`. | `horaInicio < horaFin`; no solapamiento secuencial de mismo lugar y fecha. |
| Inscripciones | `InscripcionRequest` / `InscripcionResponse`. | La actividad debe existir; una cancelación conserva historial. |
| Metas | Request/response y `personaId` escalar obligatorio. | Meta pertenece lógicamente a una persona; cumplimiento es explícito, no calculado. |

Los cuerpos de entrada usan `@Valid`. El service es la frontera transaccional y el handler global traduce validaciones y excepciones de recurso o negocio. Metas no incorpora `@ManyToOne` hacia Personal: evita acoplamiento hasta que exista contrato público. Por ello **no satisface aún** el criterio S3 de asociación ORM + DTO relacionado.

## 5. Arquitectura backend U1

```text
Controller (REST + @Valid)
        ↓ DTO
Service (reglas + transacción + contratos públicos)
        ↓ entity / mapper
Repository (persistencia propia)
        ↓
Oracle BD2 (evidencia pendiente en vivo)
```

- `exception/` es transversal y convierte errores a una respuesta uniforme.
- Actividad e Inscripción son submódulos separados: Inscripción consulta `ActividadService`, no `ActividadRepository`.
- `personaId`, `actividadId` y `creadorId` son referencias escalares cuando el propietario está fuera del módulo.
- V001/V002 son scripts manuales preparados para Oracle; no se ejecutan automáticamente y no se consideran activos sin registro/evidencia del equipo.

## 6. Casos de prueba y demo

| Caso | Evidencia disponible | Resultado o límite |
|---|---|---|
| Validaciones HTTP | Pruebas de controller y handler para 400/404/409. | Cubierto en Java; repetir en vivo con requests reales. |
| Horario inválido/solapado | Pruebas de service de Actividad. | Rechazo secuencial; concurrencia real requiere Oracle + V002. |
| Inscripción duplicada/cancelación | Pruebas de service de Inscripción. | Flujo normal e idempotencia cubiertos; carrera real requiere Oracle + V001. |
| Meta | Pruebas entity/DTO/mapper/service/controller. | Crear, fechas, estado inicial, actualización, cumplimiento y borrado condicionado. |
| Suite registrada | La suite local incluye `EvaluacionAptitudTransactionalIntegrationTest`: 1 cabecera y 2 detalles con cálculo, más rollback completo ante falla posterior dentro de la misma transacción H2. | La evidencia H2 valida la atomicidad local; no sustituye Oracle. |
| Runtime H2 local | Perfil `test` en puerto 8087: health 200, OpenAPI 200, Swagger redirect/UI 200, Teams 200, CORS permitido/rechazado/preflight y POST Team 201 con `INFO` after-commit. El proceso se detuvo al finalizar. | Prueba local de arranque y contratos HTTP; no usa Oracle ni persiste fuera de H2 en memoria. |
| Integración Oracle | Sin evidencia de ejecución viva contra BD2 registrada aquí. | Pendiente: arrancar y demostrar conexión contra BD2. |

Existe evidencia local H2 de rollback del agregado `EvaluacionAptitud -> DetallePruebaFisica`; falta repetirla contra Oracle autorizado. `SaludablementeApplication` permite arrancar Spring Boot y `ModularityTests` verifica localmente la topología con `ApplicationModules.of(...).verify()` (1/1 PASS, 2026-09-13). El runtime H2 local comprobó Swagger, CORS y un log `INFO` posterior a commit; aún falta repetir estas evidencias contra Oracle/BD2 autorizado. La búsqueda combinada y el resumen agregado de Actividades existen localmente; falta repetirlos con datos reales.

## 7. Trazabilidad con ADS y BD2

| Elemento LP2 | ADS | BD2 | Evidencia/pendiente de Saludablemente |
|---|---|---|---|
| Monolito modular | Límites y arquitectura del proyecto integrador. | Un datasource y propiedad de tablas/esquemas. | Bootstrap único y prueba Modulith local disponibles; integración/equipo debe acordar límites explícitos antes de ampliar módulos. |
| ORM y CRUD | Diseño de recursos y responsabilidades. | Oracle real, tablas y restricciones. | CRUD Java implementado; falta conexión Oracle en vivo. |
| Reglas de inscripción/agenda | Reglas de negocio de Actividades. | V001/V002 deben ser aplicadas manualmente y registradas. | Scripts preparados, no ejecutados según esta documentación. |
| Asociación ORM/DTO relacionado | Modelo de relaciones aprobado. | FK/objetos relacionados del esquema real. | Pendiente: el diseño actual usa IDs escalares entre módulos. |
| Cabecera–detalle y rollback | `EvaluacionAptitud -> DetallePruebaFisica` es el caso compuesto existente. | Transacción/rollback verificable. | Prueba H2 cubre commit y rollback; repetir contra Oracle autorizado. Actividad–Inscripción no se presenta como cabecera–detalle. |
| Consultas y CORS | Requisitos de consulta y cliente web. | Datos reales/índices según BD2. | Búsqueda y resumen de Actividades implementados localmente; CORS transversal para `/api/**` permite temporalmente `http://localhost:4200` sin credenciales. Falta backend iniciado y valores del ambiente compartido. |

## 8. Rúbrica de evaluación

La calificación se asigna durante la revisión con evidencia en vivo. Esta tabla reproduce los criterios, pesos y niveles de la plantilla LP2; **no autoasigna calificaciones** a Saludablemente.

| Criterio | Peso | CE / Nivel | A (20 pts) | B (15 pts) | C (10 pts) | D (5 pts) | Calificación obtenida |
|---|---:|---|---|---|---|---|---|
| 1. Crea y configura el proyecto backend con ORM, conexión a la base de datos, recurso REST inicial, DTO y documentación de API | 16% | CE023-N2 (parcial) | Proyecto ejecutable, conectado a Oracle, con contrato y versionado de API documentados y verificables en vivo. | Proyecto ejecutable y conectado, con documentación parcial. | Proyecto ejecutable con conexión o documentación incompleta. | No presenta un proyecto backend ejecutable. | Pendiente de evaluación. |
| 2. Implementa un CRUD REST completo, con validaciones, excepciones, logs y pruebas transversales | 16% | CE023-N2 (parcial) | CRUD completo con validación, manejo de errores y trazabilidad probados con casos reales. | CRUD completo con validación parcial o trazabilidad incompleta. | CRUD incompleto o sin manejo de errores. | No presenta CRUD funcional. | Pendiente de evaluación. |
| 3. Gestiona objetos relacionados mediante ORM, DTO y reglas de asociación | 16% | CE023-N2 (parcial) | Asociación entre entidades con DTO relacionado y navegación controlada, verificada en vivo. | Asociación funcional, con detalles menores en la navegación o el DTO. | Asociación incompleta o sin control de referencias. | No implementa objetos relacionados. | Pendiente de evaluación. |
| 4. Implementa una operación cabecera–detalle con registro atómico, cálculos, estados, consistencia, commit y rollback | 16% | CE023-N2 (parcial) | Operación completa, con caso de éxito y caso de rollback probados y explicados. | Operación completa, con un caso probado. | Operación presente, sin evidencia clara de atomicidad. | No implementa la operación cabecera–detalle. | Pendiente de evaluación. |
| 5. Implementa consultas, filtros, ordenamiento, agregaciones, reportes y configuración CORS | 16% | CE023-N2 (parcial) | Filtros combinados, reporte agregado y CORS configurado por propiedad, probados en vivo. | La mayoría funciona, con detalles menores. | Consultas o CORS incompletos. | No implementa consultas ni CORS. | Pendiente de evaluación. |
| 6. Sustentación | 20% | CG | Sustenta con claridad y profesionalismo su aporte individual, respondiendo con precisión las preguntas del jurado. | Sustenta con solvencia, con detalles menores en claridad, orden o precisión. | Sustenta con dificultad; claridad, orden o precisión insuficientes. | No sustenta adecuadamente ni demuestra su aporte individual. | Pendiente de evaluación. |

**Nota final = suma de (Peso × Puntos de la calificación obtenida) / 100 × 20.**

`CE023-N2 (parcial)` corresponde a la porción de backend REST del Nivel 2 de CE023; frontend SPA, JWT e integración full-stack se completan en Unidad II. `CG` corresponde a la competencia general de innovación y solución de problemas.

### Checklist de sustentación individual

**Presentación técnica — 8 min**

- [ ] Explicar alcance arquitectónico, módulos propios y límites entre módulos.
- [ ] Recorrer el contrato REST y DTOs sin exponer entities JPA.
- [ ] Justificar una regla de negocio y la frontera transaccional.
- [ ] Diferenciar evidencia implementada de brechas pendientes.

**Demo técnica — 5 min**

- [ ] Ejecutar un CRUD propio con un caso válido y un error 400/409 real.
- [ ] Mostrar Oracle, Modulith, cabecera–detalle, CORS y logs solo si cuentan con evidencia viva; de lo contrario, declarar cada pendiente. Ejecutar filtros/reporte de Actividades con datos reales durante la demo.
- [ ] Mostrar el resultado actual de pruebas aplicables.

**Preguntas individuales — 5 min**

- [ ] Explicar DTO de entrada/salida, validación, excepción global, ORM, transacción, modularidad y CORS con relación al código propio.

### Subaspectos de la sustentación

| Subaspecto | Qué debe demostrarse en Unidad I |
|---|---|
| 1. Aporte individual | Cada integrante demuestra lo que construyó en su backend. |
| 2. Comunicación y orden | Claridad, estructura, tiempo y lenguaje técnico. |
| 3. Presentación personal y actitud | Puntualidad, presentación adecuada, actitud profesional, respeto y coherencia institucional. |
| 4. Repositorio y estándares | Topics académicos configurados desde S2, organización, commits y reproducibilidad. |
| 5. MkDocs o equivalente | Documentación U1 publicada, navegable y alineada con este `lp2-demo.md`. |
| 6. Pitch/demo ejecutiva | Introducción breve con apoyo visual; no reemplaza la demo técnica S06. |
## 9. Procedencia y siguientes pasos

- Fuente de estructura y rúbrica: plantilla **LP2 - Producto de Unidad 1** entregada por la docencia.
- Fuente de obligaciones de demo: **S06 - Evaluación de la Unidad I** entregada por la docencia.
- Seguimiento operativo: [Matriz de trazabilidad S06](rebuild/18-s06-evaluation-traceability.md).
- Alcance de Actividades: [requisitos y fundamento](rebuild/16-activities-module-requirements-and-rationale.md).
- Alcance de Metas: [requisitos y dependencias](rebuild/17-metas-requirements-and-dependencies.md).
## Local seed execution status

The manual SQL seed was executed successfully using the `SALUDABLEMENTE_APP`
account against the local Oracle container. Re-running the data scripts did not
duplicate demo data. The recommendation confidence precision issue (`0.90`
rounding to `1`) was corrected in commit `eb408a2`; after re-execution, the
recommendation detail was inserted correctly.

The backend and Swagger UI responded successfully on port `8080`, and the
Maven suite passed locally with 210 tests and zero failures.

This document must not be used as BD2/team evidence or as a statement that S06
is complete. The next required evidence is the manual Swagger demonstration;
BD2 validation remains a separate later activity.
