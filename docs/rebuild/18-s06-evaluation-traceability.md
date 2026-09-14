# S06: matriz de trazabilidad y evidencia de Saludablemente

> **Resultado:** Saludablemente tiene verticales Java verificadas, pero aún no reúne toda la evidencia exigida por S06. Esta matriz convierte cada criterio en una acción demostrable y prohíbe presentar capacidades pendientes como completadas.

## Estado por criterio

| Criterio S06 | Evidencia existente | Brecha real | Acción, propietario y condición de prueba |
|---|---|---|---|
| 1. Proyecto, ORM, Oracle, REST, DTO y documentación | Maven, endpoints `/api/v1`, entities, DTOs y [LP2 demo](../lp2-demo.md). | No hay ejecución Oracle viva registrada. | `SaludablementeApplication` ya aporta el bootstrap único. **BD2/equipo:** habilitar esquema/datasource autorizado. **Pedro:** con esos prerrequisitos, arrancar backend y mostrar request exitosa contra Oracle, sin credenciales en repositorio. |
| 2. CRUD, validaciones, excepciones, logs y pruebas | CRUD local de Teams, Actividades y Metas; transiciones de Inscripción/Meta; 400/404/409. Services registran transiciones exitosas con `INFO`; el handler registra errores esperados con `WARN` e inesperados con `ERROR` y stack trace. | Falta observación en un backend iniciado antes de demo; la suite local actual ya fue ejecutada. | **Pedro:** ejecutar `mvn test`; iniciar el backend y demostrar un caso válido y uno fallido, verificando las trazas sin datos sensibles. |
| 3. Objetos relacionados mediante ORM, DTO y reglas de asociación | `Actividad.inscripciones` e `Inscripcion.actividad` son asociaciones ORM internas LAZY; `GET /api/v1/actividades/{id}/detalle` devuelve DTOs relacionados sin ciclos. `@EntityGraph` se limita a esa lectura. | Falta evidencia en vivo contra Oracle. | **Pedro:** demostrar el endpoint detallado en el backend conectado a BD2. No se atribuye este criterio a relaciones entre módulos. |
| 4. Cabecera–detalle atómica, cálculos, estado, commit y rollback | No hay evidencia que satisfaga este criterio. | No existe DTO compuesto, operación de negocio acordada, cálculo ni prueba de rollback. Actividad–Inscripción actual no es cabecera–detalle. | **Equipo:** definir el agregado y su dueño. **Propietario asignado:** implementar una única operación transaccional; provocar fallo tras iniciar la operación y demostrar que no persiste ni cabecera ni detalle. |
| 5. Filtros, ordenamiento, proyecciones, agregados, reporte y CORS | Actividades expone búsqueda combinable y resumen por estado: `GET /api/v1/actividades/busqueda` y `GET /api/v1/actividades/resumen`. CORS se aplica una vez mediante `CorsConfigurationSource` integrado en Spring Security para `/api/**`, con propiedades externas y pruebas MockMvc de política y de cadena real Spring Security para origen permitido, rechazado y preflight. | La evidencia local cubre parámetros, ordenamiento permitido, proyección, agregado y política CORS local. Falta repetir consultas y CORS contra el backend/Oracle de demo y confirmar valores del ambiente compartido. | **Pedro:** demostrar ambas consultas y los tres escenarios CORS contra datos reales. **Integración/equipo:** proveer origen y política de credenciales del ambiente compartido mediante variables de entorno. |
| 6. Sustentación | Aporte de Pedro: Actividades/Inscripciones/Metas y documentación de reglas. | La sustentación individual aún no se ha ejecutado. | **Pedro:** seguir checklist de demo, explicar decisiones y responder preguntas de S1–S5 con código/evidencia visible. |

## Evidencia de módulos disponible

| Módulo | Implementado en Java | Evidencia registrada | Advertencia para S06 |
|---|---|---|---|
| Teams | Vertical CRUD de referencia, DTO/mapper/service/controller/errores. | Cierre histórico 26/26 PASS. | No sustituye demostración actual con Oracle y Modulith. |
| Actividades | CRUD y regla de intervalo/solapamiento. | Pruebas focalizadas y cierre previo 83/83 PASS. | V002 solo protege carreras tras ejecución manual autorizada en Oracle. |
| Inscripciones | Registro, duplicado normal, cancelación idempotente. | Pruebas focalizadas incluidas en cierre de Actividades. | V001 solo protege duplicado concurrente tras ejecución manual autorizada en Oracle. |
| Metas | CRUD, cumplimiento explícito y eliminación condicionada. | La suite integrada actual completa pasó 196/196 (2026-09-13), incluida la verificación Modulith, el perfil H2, CORS/Security y OpenAPI. | No integra todavía Personal/Evaluación ni aporta asociación ORM relacionada. |

## Dependencias de infraestructura que no se deben falsear

- **Oracle vivo:** se confirma únicamente al arrancar contra el esquema real de BD2 y ejecutar requests con datos reales.
- **Bootstrap y Spring Modulith:** `SaludablementeApplication` es el bootstrap único y `ModularityTests` ejecuta `ApplicationModules.of(SaludablementeApplication.class).verify()` sobre la topología actual de paquetes (1/1 PASS, 2026-09-13). No se inventaron anotaciones ni fronteras para aprobarla. El equipo debe preservar este bootstrap y acordar límites explícitos antes de futuras ampliaciones; la prueba local no reemplaza Oracle, Swagger ni la integración compartida.
- **Migraciones manuales V001/V002:** están preparadas, no se ejecutan automáticamente. Su aplicación y el resultado deben quedar registrados por el responsable autorizado.
- **CORS:** existe una única `CorsConfigurationSource` transversal para `/api/**`, integrada en Spring Security, con `app.cors.*` y sobrescritura por variables de entorno. El valor de desarrollo autorizado es `http://localhost:4200`, con credenciales desactivadas; el ambiente compartido debe aportar sus propios valores. No lleva `@CrossOrigin` por controller.
- **OpenAPI/Swagger:** Springdoc descubre los endpoints y `OpenApiConfig` aporta título, versión y descripción para presentarlos. Sus metadatos no reemplazan una demostración de Swagger con el backend iniciado.
- **Logs:** respuestas de error no son logs. La convención está implementada, pero debe observarse en un backend iniciado antes de atribuir evidencia en vivo.

## Convención de logs de servidor

| Zona | Nivel | Información permitida |
|---|---|---|
| Services de aplicación | `INFO` | Operación, ID técnico y estado resultante después de que una escritura confirma su commit. |
| `GlobalExceptionHandler` | `WARN` | Tipo, estado HTTP y mensaje seguro para 400, 404 y 409 esperados, incluidos JSON o parámetros malformados. |
| `GlobalExceptionHandler` | `ERROR` | Tipo, 500 uniforme y stack trace para una falla inesperada. |

No se registran cuerpos HTTP, descripciones, nombres, correos, credenciales, tokens ni datos clínicos. Las lecturas, DTOs, entities, mappers y repositories tampoco producen logs por defecto. El `INFO` se difiere con sincronización transaccional hasta `afterCommit`; fuera de una transacción activa se ejecuta de inmediato. El escenario mínimo pendiente es iniciar el backend localmente, ejecutar una escritura válida y un 400/404/409, y revisar la consola; Oracle no interviene en esta evidencia.

## Gate de configuración compartida

La configuración integrada separa entornos: `application-dev.yml` toma `DB_URL`, `DB_USERNAME` y `DB_PASSWORD` desde variables de entorno y valida un esquema Oracle ya autorizado; no crea ni modifica el esquema. Las pruebas cargan el perfil `test`, que usa H2 en memoria, `create-drop` y los schemas de mapeos declarados en `src/test/resources/schema.sql`, para evitar dependencia de Oracle local. `SaludablementeApplicationTests` comprueba explícitamente el perfil activo y la conexión JDBC H2. Estas reglas habilitan una suite reproducible, pero no constituyen evidencia de una conexión viva a BD2.

> Si una contraseña Oracle estuvo previamente en una rama, debe rotarse. Quitarla del archivo actual no elimina copias anteriores ni sustituye el procedimiento de seguridad del repositorio.
## Demo individual de Pedro

### Antes de presentar

- [ ] Ejecutar `mvn test` y registrar el total actual, sin reutilizar una cifra histórica si cambió.
- [ ] Ejecutar `git diff --check` y verificar que no haya errores de espacios.
- [ ] Confirmar con BD2 el Oracle/esquema autorizado y que el backend arranca conectado.
- [x] Confirmar bootstrap único y verificación Modulith local: `ModularityTests` pasó 1/1 el 2026-09-13. El equipo debe preservar el bootstrap y acordar límites explícitos antes de ampliar módulos.
- [ ] Confirmar si V001/V002 fueron aplicadas; si no, explicar que las garantías concurrentes no están activas.
- [x] Registrar evidencia local de Spring Modulith: `ApplicationModules.of(SaludablementeApplication.class).verify()` pasó 1/1 el 2026-09-13.
- [ ] Configurar el origen frontend y la política de credenciales del ambiente de demo mediante `CORS_*`.
- [ ] No preparar una demo que afirme CORS en el ambiente compartido, logs o cabecera–detalle si siguen pendientes. Las consultas/reporte locales requieren todavía datos reales en la demo.

### Secuencia de cinco minutos de demo técnica

1. Mostrar el proyecto único, los módulos y el contrato REST de [lp2-demo.md](../lp2-demo.md).
2. Ejecutar un CRUD propio: crear/consultar/actualizar una Actividad o Meta.
3. Provocar un error real: intervalo inválido, solapamiento secuencial, inscripción duplicada normal o actualización de Meta no permitida; mostrar 400/409.
4. Explicar el límite modular: Inscripción usa `ActividadService`; Meta usa `personaId` escalar y no repository externo.
5. Si Oracle, Modulith, CORS, logs o cabecera–detalle no tienen evidencia, nombrarlos como pendientes y mostrar la fila correspondiente de esta matriz. Ejecutar búsqueda y resumen con datos reales antes de atribuirles evidencia S06.

### Evidencia en vivo requerida para cerrar brechas

| Brecha | Escenario mínimo verificable | Resultado esperado |
|---|---|---|
| Oracle | Arrancar backend con datasource BD2 y ejecutar un CRUD. | Persistencia y respuesta real sin error de conexión. |
| Modulith | Ejecutar `ModularityTests`. | `ApplicationModules.of(SaludablementeApplication.class).verify()` pasa sobre la topología actual; revisar límites explícitos si el equipo amplía módulos. |
| CORS | Request desde origen permitido, request desde origen rechazado y preflight `OPTIONS` si aplica. | Cabeceras configuradas desde propiedades, sin hard-codear; solo el origen permitido recibe autorización. |
| Logs | Caso válido y caso fallido con el backend iniciado. | `INFO` para transición exitosa; `WARN` para 400/404/409 o `ERROR` con stack trace para 500, sin secretos. |
| Asociación ORM | Consultar `GET /api/v1/actividades/{id}/detalle` con una actividad que tenga inscripciones. | Navegación controlada sin ciclo de serialización. |
| Cabecera–detalle | Crear compuesto y provocar fallo interno después de comenzar la transacción. | Éxito persiste ambos lados; fallo deja cero persistencia parcial. |
| Consultas/reporte | Filtros opcionales combinados + ordenamiento + resumen agregado. | Datos reales coincidentes y respuesta de proyección/reporte. |

## Reglas de presentación

- Actividad–Inscripción **no** se llama cabecera–detalle mientras no exista una operación compuesta atómica, con cálculo y rollback probado.
- Los IDs escalares entre módulos son un límite modular, no una asociación ORM demostrable.
- V001/V002 no son evidencia de Oracle hasta que se ejecuten en la base usada por la aplicación.
- La documentación guía la demo, pero no reemplaza requests, consola, logs ni resultados de pruebas mostrados en vivo.

## Referencias

- [Producto LP2 Unidad I de Saludablemente](../lp2-demo.md)
- [Preparación S06 específica de Actividades](14-s06-integration-readiness.md)
- [Requisitos de Actividades](16-activities-module-requirements-and-rationale.md)
- [Requisitos y dependencias de Metas](17-metas-requirements-and-dependencies.md)
- [Guía de merge de módulos de Pedro](../merge-pedro-modules.md)
