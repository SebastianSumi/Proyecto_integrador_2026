# S06: matriz de trazabilidad y evidencia de Saludablemente

> **Resultado:** Saludablemente tiene verticales Java verificadas, pero aún no reúne toda la evidencia exigida por S06. Esta matriz convierte cada criterio en una acción demostrable y prohíbe presentar capacidades pendientes como completadas.

## Estado por criterio

| Criterio S06 | Evidencia existente | Brecha real | Acción, propietario y condición de prueba |
|---|---|---|---|
| 1. Proyecto, ORM, Oracle, REST, DTO y documentación | Spring Boot/Maven, endpoints `/api/v1`, entities, DTOs y [LP2 demo](../lp2-demo.md). | No hay ejecución Oracle viva registrada. | **BD2/equipo:** habilitar esquema/datasource autorizado. **Pedro:** arrancar backend y mostrar request exitosa contra Oracle, sin credenciales en repositorio. |
| 2. CRUD, validaciones, excepciones, logs y pruebas | CRUD local de Teams, Actividades y Metas; transiciones de Inscripción/Meta; 400/404/409 y suite 126/126 PASS. | Logs transversales no están implementados/evidenciados; resultado Maven debe refrescarse antes de demo. | **Equipo de integración:** acordar logs. **Pedro:** ejecutar `mvn test` y demostrar CRUD válido + 400 en vivo. |
| 3. Objetos relacionados mediante ORM, DTO y reglas de asociación | `Actividad.inscripciones` e `Inscripcion.actividad` son asociaciones ORM internas LAZY; `GET /api/v1/actividades/{id}/detalle` devuelve DTOs relacionados sin ciclos. `@EntityGraph` se limita a esa lectura. | Falta evidencia en vivo contra Oracle. | **Pedro:** demostrar el endpoint detallado en el backend conectado a BD2. No se atribuye este criterio a relaciones entre módulos. |
| 4. Cabecera–detalle atómica, cálculos, estado, commit y rollback | No hay evidencia que satisfaga este criterio. | No existe DTO compuesto, operación de negocio acordada, cálculo ni prueba de rollback. Actividad–Inscripción actual no es cabecera–detalle. | **Equipo:** definir el agregado y su dueño. **Propietario asignado:** implementar una única operación transaccional; provocar fallo tras iniciar la operación y demostrar que no persiste ni cabecera ni detalle. |
| 5. Filtros, ordenamiento, proyecciones, agregados, reporte y CORS | Listados básicos; docs separan CORS como transversal. | Faltan filtro combinado, `Sort`, proyección, agregado/reporte y CORS por propiedades con prueba HTTP. | **Equipo:** seleccionar caso de uso y contrato. **Propietario:** implementar consulta y reporte; **integración:** configurar CORS mediante propiedades y probar preflight/origen permitido en vivo. |
| 6. Sustentación | Aporte de Pedro: Actividades/Inscripciones/Metas y documentación de reglas. | La sustentación individual aún no se ha ejecutado. | **Pedro:** seguir checklist de demo, explicar decisiones y responder preguntas de S1–S5 con código/evidencia visible. |

## Evidencia de módulos disponible

| Módulo | Implementado en Java | Evidencia registrada | Advertencia para S06 |
|---|---|---|---|
| Teams | Vertical CRUD de referencia, DTO/mapper/service/controller/errores. | Cierre histórico 26/26 PASS. | No sustituye demostración actual con Oracle y Modulith. |
| Actividades | CRUD y regla de intervalo/solapamiento. | Pruebas focalizadas y cierre previo 83/83 PASS. | V002 solo protege carreras tras ejecución manual autorizada en Oracle. |
| Inscripciones | Registro, duplicado normal, cancelación idempotente. | Pruebas focalizadas incluidas en cierre de Actividades. | V001 solo protege duplicado concurrente tras ejecución manual autorizada en Oracle. |
| Metas | CRUD, cumplimiento explícito y eliminación condicionada. | Cierre de vertical: suite 118/118 PASS. | No integra todavía Personal/Evaluación ni aporta asociación ORM relacionada. |

## Dependencias de infraestructura que no se deben falsear

- **Oracle vivo:** se confirma únicamente al arrancar contra el esquema real de BD2 y ejecutar requests con datos reales.
- **Migraciones manuales V001/V002:** están preparadas, no se ejecutan automáticamente. Su aplicación y el resultado deben quedar registrados por el responsable autorizado.
- **Spring Modulith:** no se declara verde hasta ejecutar la prueba/verificación integrada y mostrar su resultado.
- **CORS:** no existe por el solo hecho de tener controllers. Debe configurarse por propiedades y probarse desde un origen HTTP permitido y, si corresponde, un preflight.
- **Logs:** respuestas de error no son logs. Se requiere trazabilidad de servidor acordada y visible.

## Demo individual de Pedro

### Antes de presentar

- [ ] Ejecutar `mvn test` y registrar el total actual, sin reutilizar una cifra histórica si cambió.
- [ ] Ejecutar `git diff --check` y verificar que no haya errores de espacios.
- [ ] Confirmar con BD2 el Oracle/esquema autorizado y que el backend arranca conectado.
- [ ] Confirmar si V001/V002 fueron aplicadas; si no, explicar que las garantías concurrentes no están activas.
- [ ] Confirmar si existe evidencia de Spring Modulith verde; si no, declararlo pendiente.
- [ ] No preparar una demo que afirme filtros, reporte, CORS, logs, asociación ORM o cabecera–detalle si siguen pendientes.

### Secuencia de cinco minutos de demo técnica

1. Mostrar el proyecto único, los módulos y el contrato REST de [lp2-demo.md](../lp2-demo.md).
2. Ejecutar un CRUD propio: crear/consultar/actualizar una Actividad o Meta.
3. Provocar un error real: intervalo inválido, solapamiento secuencial, inscripción duplicada normal o actualización de Meta no permitida; mostrar 400/409.
4. Explicar el límite modular: Inscripción usa `ActividadService`; Meta usa `personaId` escalar y no repository externo.
5. Si Oracle, Modulith, CORS, logs, cabecera–detalle o reportes no tienen evidencia, nombrarlos como pendientes y mostrar la fila correspondiente de esta matriz.

### Evidencia en vivo requerida para cerrar brechas

| Brecha | Escenario mínimo verificable | Resultado esperado |
|---|---|---|
| Oracle | Arrancar backend con datasource BD2 y ejecutar un CRUD. | Persistencia y respuesta real sin error de conexión. |
| Modulith | Ejecutar su verificación integrada aplicable. | Resultado verde visible. |
| CORS | Request desde origen permitido y preflight si aplica. | Cabeceras configuradas desde propiedades, no hard-codeadas. |
| Logs | Caso válido y caso fallido. | Trazas acordadas correlacionables en servidor, sin secretos. |
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
