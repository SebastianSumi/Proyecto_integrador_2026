# Preparación S06: soporte de Actividades

> **Estado:** Actividades e Inscripciones están implementadas como verticales Java. Este documento explica su evidencia específica; la evaluación completa se controla en la [matriz global S06](18-s06-evaluation-traceability.md).

## Qué aporta Actividades a la evaluación

| Aporte | Evidencia local | Límite para S06 |
|---|---|---|
| CRUD REST | Actividad lista, obtiene, crea y actualiza con DTO validado. | Debe demostrarse contra Oracle vivo para afirmar integración BD2. |
| Regla de programación | Intervalo válido y rechazo de solapamiento secuencial por lugar/fecha. | La carrera concurrente requiere V002 aplicada y prueba real en Oracle. |
| Inscripción | Registro valida existencia de Actividad, evita duplicado en flujo normal y cancela idempotentemente. | La carrera concurrente requiere V001 aplicada y prueba real en Oracle. |
| Límites de módulos | Inscripción consume `ActividadService`, no su repository. | No es asociación ORM/DTO relacionado ni cabecera–detalle. |
| Errores | 400/404/409 centralizados. | No reemplaza logs de servidor. |

## Responsabilidades correctas

| Área | Lugar correcto | No corresponde a |
|---|---|---|
| Regla de Actividad/Inscripción | Service del submódulo dueño y pruebas focalizadas. | Controller, entity o repository ajeno. |
| CORS | Configuración transversal mediante propiedades y prueba HTTP. | `package-info.java`, DTO o controller de Actividades. |
| Logs | Services para escrituras exitosas y handler global para errores, sin datos sensibles. | Entidades, DTOs, requests completos o repositories. |
| ORM relacionado | Una asociación propia confirmada, su DTO y prueba de navegación. | Convertir IDs escalares externos en relación JPA improvisada. |
| Cabecera–detalle | DTO compuesto y operación transaccional de un agregado aprobado. | Renombrar la relación Actividad–Inscripción existente. |
| Verificación modular | Prueba Spring Modulith aplicable y resultado visible. | Una declaración documental. |

## Estado de las garantías Oracle

- `V001__enrollment_active_uniqueness.sql` está preparado para unicidad de inscripción activa; su garantía inicia solo después de ejecución manual autorizada y registro contra el Oracle usado.
- `V002__activity_schedule_coordination.sql` está preparado para serializar la agenda por lugar/fecha; su garantía inicia solo después de ejecución manual autorizada y prueba concurrente real.
- Estas migraciones no usan Flyway, no se ejecutan desde el backend y no prueban por sí solas que Oracle esté integrado.

## Evidencia de integración: bootstrap y Modulith

`SaludablementeApplication` es el bootstrap único con `@SpringBootApplication`; al ubicarse en `pe.edu.upeu.saludablemente`, Spring Boot detecta los componentes de los módulos actuales. `SaludablementeApplicationTest` ejecuta `ApplicationModules.of(SaludablementeApplication.class).verify()` sobre la estructura física declarada y pasó localmente (1/1, 2026-09-13).

La prueba confirma que las dependencias actuales no violan la topología que Spring Modulith infiere de los paquetes. No se añadieron anotaciones ni límites artificiales solo para aprobarla. Persisten los límites de integración: el equipo debe conservar este bootstrap único y acordar cualquier frontera explícita futura antes de ampliar módulos. Esta evidencia habilita la verificación local de Modulith, pero no reemplaza el arranque real con Oracle, Swagger ni una prueba de infraestructura compartida.

## Gate CORS transversal

> **Decidido:** CORS se configurará una única vez y de forma transversal para `/api/**`. No se usará `@CrossOrigin` en controllers ni se codificarán orígenes en Java.

| Aspecto | Decisión de implementación | Estado |
|---|---|---|
| Alcance | Un `CorsFilter` transversal para `/api/**`. | Implementado. |
| Orígenes | `app.cors.allowed-origins`; valor local temporal `http://localhost:4200`, sustituible con `CORS_ALLOWED_ORIGINS`. | Implementado; confirmar el origen de cada ambiente antes de desplegar. |
| Métodos y headers | `app.cors.allowed-methods` y `app.cors.allowed-headers`, sustituibles con `CORS_ALLOWED_METHODS` y `CORS_ALLOWED_HEADERS`. | Implementado. |
| Credenciales y caché | `app.cors.allow-credentials=false` y `app.cors.max-age=3600`, sustituibles con `CORS_ALLOW_CREDENTIALS` y `CORS_MAX_AGE`. | Implementado sin cookies ni credenciales. |
| Controllers | Sin `@CrossOrigin` por recurso. | Decidido. |

La configuración local autorizada permite únicamente Angular en `http://localhost:4200` y no permite credenciales. Cada ambiente debe reemplazar estos valores mediante variables de entorno; no se debe ampliar la lista de orígenes sin una necesidad de cliente confirmada.

### Plan de prueba CORS

| Escenario | Request HTTP | Evidencia requerida | Estado |
|---|---|---|---|
| Origen permitido | Request a `/api/**` con `Origin` autorizado. | Cabecera `Access-Control-Allow-Origin` coherente con la propiedad. | Cubierto por `CorsConfigTest`. |
| Origen rechazado | Request a `/api/**` con `Origin` no autorizado. | Ausencia de autorización CORS para ese origen. | Cubierto por `CorsConfigTest`. |
| Preflight | `OPTIONS` con `Origin`, `Access-Control-Request-Method` y, si aplica, headers solicitados. | Métodos/headers/credenciales/max-age coincidentes con propiedades. | Cubierto por `CorsConfigTest`. |

Las pruebas MockMvc verifican la política HTTP sin depender de Oracle. Antes de la demo S06 se debe repetir los tres escenarios con el backend iniciado y el origen configurado para ese ambiente.

## Ruta de evidencia de Actividades

1. Con Oracle autorizado, crear y consultar una Actividad.
2. Probar `horaInicio >= horaFin` y mostrar 400.
3. Probar un solapamiento secuencial y mostrar 409.
4. Registrar y cancelar una Inscripción; repetir cancelación y explicar idempotencia.
5. Si V001/V002 fueron aplicadas, ejecutar además la prueba concurrente real acordada. Si no, declarar esa garantía pendiente.
6. Verificar en consola un `INFO` de escritura y un `WARN` de error esperado sin datos sensibles.
7. Repetir con el backend iniciado los tres escenarios CORS (permitido, rechazado y preflight) usando el origen configurado para la demo. Para asociación ORM, cabecera–detalle, consultas/reporte y Modulith, seguir la [matriz S06](18-s06-evaluation-traceability.md); Actividades no los atribuye a sí misma.

## Criterios de honestidad

- No se afirmará CORS para un ambiente hasta configurar su origen, confirmar la política de credenciales y ejecutar prueba HTTP contra ese backend.
- No se afirmará cabecera–detalle hasta probar una transacción atómica compuesta, con fallo y rollback.
- No se publicará una relación JPA directa con módulos de compañeros: la colaboración usa contratos públicos.
- No se declarará V001/V002 activa ni Oracle verificado hasta contar con evidencia de ejecución controlada.
