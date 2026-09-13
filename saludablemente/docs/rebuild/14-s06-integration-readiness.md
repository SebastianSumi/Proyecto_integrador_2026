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

## Gate CORS transversal

> **Decidido:** CORS se configurará una única vez y de forma transversal para `/api/**`. No se usará `@CrossOrigin` en controllers ni se codificarán orígenes en Java.

| Aspecto | Decisión de implementación | Estado |
|---|---|---|
| Alcance | Una configuración web transversal para `/api/**`. | Pendiente de código. |
| Orígenes | Propiedad externa `allowed-origins`, distinta por ambiente. | Pendiente de que el equipo confirme el origen frontend autorizado. |
| Métodos y headers | Propiedades externas para métodos permitidos y headers permitidos. | Pendiente de política del cliente. |
| Credenciales y caché | Propiedades externas para credenciales y `max-age`. | Pendiente de política de credenciales. |
| Controllers | Sin `@CrossOrigin` por recurso. | Decidido. |

No se implementará una configuración efectiva hasta confirmar el origen frontend autorizado y la política de credenciales. Configurar valores de ejemplo o permisivos antes de esa decisión daría una garantía falsa y podría abrir el backend más de lo necesario.

### Plan de prueba CORS

| Escenario | Request HTTP | Evidencia requerida | Estado |
|---|---|---|---|
| Origen permitido | Request a `/api/**` con `Origin` autorizado. | Cabecera `Access-Control-Allow-Origin` coherente con la propiedad. | No ejecutado. |
| Origen rechazado | Request a `/api/**` con `Origin` no autorizado. | Ausencia de autorización CORS para ese origen. | No ejecutado. |
| Preflight | `OPTIONS` con `Origin`, `Access-Control-Request-Method` y, si aplica, headers solicitados. | Métodos/headers/credenciales/max-age coincidentes con propiedades. | No ejecutado. |

Estas pruebas se ejecutarán con el backend iniciado. La documentación no declara CORS aprobado ni reemplaza la evidencia HTTP.

## Ruta de evidencia de Actividades

1. Con Oracle autorizado, crear y consultar una Actividad.
2. Probar `horaInicio >= horaFin` y mostrar 400.
3. Probar un solapamiento secuencial y mostrar 409.
4. Registrar y cancelar una Inscripción; repetir cancelación y explicar idempotencia.
5. Si V001/V002 fueron aplicadas, ejecutar además la prueba concurrente real acordada. Si no, declarar esa garantía pendiente.
6. Verificar en consola un `INFO` de escritura y un `WARN` de error esperado sin datos sensibles.
7. Cuando se confirmen origen y credenciales, ejecutar los tres escenarios CORS (permitido, rechazado y preflight) descritos en este documento. Para asociación ORM, cabecera–detalle, consultas/reporte y Modulith, seguir la [matriz S06](18-s06-evaluation-traceability.md); Actividades no los atribuye a sí misma.

## Criterios de honestidad

- No se afirmará CORS hasta confirmar origen y credenciales, contar con configuración por propiedad y ejecutar prueba HTTP.
- No se afirmará cabecera–detalle hasta probar una transacción atómica compuesta, con fallo y rollback.
- No se publicará una relación JPA directa con módulos de compañeros: la colaboración usa contratos públicos.
- No se declarará V001/V002 activa ni Oracle verificado hasta contar con evidencia de ejecución controlada.
