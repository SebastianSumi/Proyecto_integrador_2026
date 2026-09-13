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
| Logs | Acuerdo/configuración transversal y límites de service. | Entidades o DTOs. |
| ORM relacionado | Una asociación propia confirmada, su DTO y prueba de navegación. | Convertir IDs escalares externos en relación JPA improvisada. |
| Cabecera–detalle | DTO compuesto y operación transaccional de un agregado aprobado. | Renombrar la relación Actividad–Inscripción existente. |
| Verificación modular | Prueba Spring Modulith aplicable y resultado visible. | Una declaración documental. |

## Estado de las garantías Oracle

- `V001__enrollment_active_uniqueness.sql` está preparado para unicidad de inscripción activa; su garantía inicia solo después de ejecución manual autorizada y registro contra el Oracle usado.
- `V002__activity_schedule_coordination.sql` está preparado para serializar la agenda por lugar/fecha; su garantía inicia solo después de ejecución manual autorizada y prueba concurrente real.
- Estas migraciones no usan Flyway, no se ejecutan desde el backend y no prueban por sí solas que Oracle esté integrado.

## Ruta de evidencia de Actividades

1. Con Oracle autorizado, crear y consultar una Actividad.
2. Probar `horaInicio >= horaFin` y mostrar 400.
3. Probar un solapamiento secuencial y mostrar 409.
4. Registrar y cancelar una Inscripción; repetir cancelación y explicar idempotencia.
5. Si V001/V002 fueron aplicadas, ejecutar además la prueba concurrente real acordada. Si no, declarar esa garantía pendiente.
6. Para criterios globales de CORS, logs, asociación ORM, cabecera–detalle, consultas/reporte y Modulith, seguir la [matriz S06](18-s06-evaluation-traceability.md); Actividades no los atribuye a sí misma.

## Criterios de honestidad

- No se afirmará CORS hasta contar con configuración por propiedad y prueba HTTP.
- No se afirmará cabecera–detalle hasta probar una transacción atómica compuesta, con fallo y rollback.
- No se publicará una relación JPA directa con módulos de compañeros: la colaboración usa contratos públicos.
- No se declarará V001/V002 activa ni Oracle verificado hasta contar con evidencia de ejecución controlada.
