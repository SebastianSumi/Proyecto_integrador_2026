# Playbook de entrega por módulo

**Decisión de trabajo.** Se entrega un módulo por vez y una capa por vez. Cada capa cierra una necesidad verificable antes de pasar a la siguiente; no se crea infraestructura, excepciones o repositorios “por si acaso”.

## Vertical de referencia verificada

**Teams** es la referencia estructural vigente para los módulos posteriores. Está cerrada desde `entity` hasta `controller`, con el manejo transversal `exception`; su evidencia final registrada es `mvn test` 26/26 PASS y `git diff --check` PASS (solo advertencias LF/CRLF preexistentes).

Al iniciar otro módulo, se reutilizan sus límites de responsabilidad, no sus detalles de dominio: paquete por módulo y capas internas, entidades no serializadas, DTOs separados, mapper sin reglas, repository propio, servicio como frontera transaccional, controller delgado y errores transversales. Las reglas, campos, relaciones y excepciones de negocio se derivan únicamente del módulo nuevo. La guía operativa está en `docs/rebuild/12-reference-vertical-for-next-modules.md`.

## Secuencia obligatoria

| Paso | Entregable | Pregunta de salida |
|---:|---|---|
| 0 | Ficha de módulo | ¿El alcance, dueño, dependencias y reglas vienen del brief y están sin ambigüedad? |
| 1 | Dominio/entidad | ¿La entidad expresa invariantes, estado y relaciones mínimas? |
| 2 | DTO | ¿El contrato separa entrada, salida y validaciones HTTP? |
| 3 | Mapper | ¿La conversión evita exponer entidades y no oculta reglas de negocio? |
| 4 | Repositorio | ¿Las consultas son necesarias, limitadas al módulo dueño y evitan N+1 probado? |
| 5 | Servicio | ¿Las reglas, dependencias públicas y frontera transaccional están en un solo lugar? |
| 6 | Controller | ¿La API `/api/v1` valida, delega y devuelve códigos HTTP coherentes? |
| 7 | Pruebas | ¿Se probaron caso exitoso, regla, error y rollback cuando corresponde? |

## Ficha mínima antes de código

```markdown
# <Módulo>
- Hecho del brief: <requisito o regla con página>
- Entidad/flujo dueño: <nombre>
- Entradas y salidas públicas: <DTOs/servicios>
- Dependencias permitidas: <módulo + contrato público>
- Reglas e invariantes: <lista verificable>
- Persistencia: <tablas, constraints e índices por aprobar>
- Casos de prueba: <éxito, rechazo, borde, transacción>
- Decisiones pendientes: <no inventar>
```

## Módulos confirmados por el brief

| Módulo | Tipo indicado | Núcleo de la primera ficha |
|---|---|---|
| Personal | No transaccional | Persona, celular activo único, baja lógica y edad calculada. |
| Evaluación nutricional | Transaccional | Cabecera y detalles antropométrico/bioquímico; IMC, reglas OMRON e importación. |
| Aptitud física | Transaccional | Cinco pruebas, unicidad persona-fecha y sincronización offline. |
| Actividades | Transaccional | Programación, solapamiento por lugar y cambios de estado. |
| Teams | No transaccional | Alta, edición, inactivación y conteo de personas activas. |
| Metas | No transaccional | Objetivo, actualización desde evaluación y cumplimiento. |
| Asistencias | Transaccional | Actividad-persona, hora, offline e impedimento por actividad no vigente. |
| Noticias | No transaccional | Borrador/publicación/despublicación y autor. |
| Notificaciones | No transaccional | Origen, destinatario, lectura e historial. |
| Alertas clínicas | Transaccional | Regla, severidad y atención trazable. |
| Recomendaciones con IA | Transaccional | Dos recomendaciones, contexto, vigencia y trazabilidad. |
| Perfil/reporte personalizado | No transaccional | Vista agregada y snapshot PDF por periodo. |
| Auditoría | No transaccional | Registro inmutable de cambios sensibles. |
| Exportación/interoperabilidad | No transaccional | Solicitud, filtros, archivo y ejecución asíncrona por umbral. |
| Seguridad y usuarios/Auth | Transversal | Cuenta, roles y autorización. |

El orden entre módulos debe respetar sus dependencias: seguridad y personal anteceden a los módulos que los referencian; actividades antecede a asistencias; evaluación nutricional antecede a alertas, metas y recomendaciones. El orden final se aprueba antes de implementar.

## Cuándo crear excepciones

| Situación | Acción |
|---|---|
| Recurso inexistente | Excepción de recurso no encontrado, manejada globalmente. |
| DTO inválido | Bean Validation; el manejador global devuelve errores de campos útiles. |
| Regla de negocio violada | Excepción específica del caso, con `409 Conflict` si hay conflicto de estado/invariante. |
| Error técnico de infraestructura | No filtrar detalles internos; registrar con correlación y devolver respuesta segura. |

Crear una excepción compartida solo cuando al menos dos módulos necesitan el mismo significado. Antes de eso, la excepción pertenece al módulo que expresa la regla.

## Progresión de pruebas

1. Dominio: invariantes y transiciones de estado puras.
2. Servicio: regla de negocio, dependencia pública simulada y éxito/error.
3. Persistencia: constraints, consulta y comportamiento transaccional real cuando la regla dependa de Oracle.
4. API: validación, autorización futura, código HTTP y contrato JSON.
5. Módulo: prueba de límite con Spring Modulith y prueba del flujo principal.

## Contraste con la guía BOMERP

| Patrón útil observado | Cómo se adopta o mejora |
|---|---|
| Paquetes por capacidad y capas (`ventas/venta/...`) | Se conserva la agrupación por módulo y capas internas. |
| DTO de request/response, validación y mapper | Se adopta; ninguna entidad se serializa directamente. |
| Servicio transaccional que coordina una operación cabecera-detalle | Se adopta para flujos transaccionales reales. |
| `@EntityGraph` en lectura de detalle | Solo tras probar una necesidad de fetch/N+1. |
| `@RestControllerAdvice` y filtro de correlación | Se adopta como patrón transversal, con contrato de error consistente por definir. |
| CORS por propiedades | Se adopta sin valores secretos y con orígenes mínimos por entorno. |
| Passwords visibles en compose/YAML/SQL de guía | Se rechaza: solo variables locales no versionadas. |
| Grants DML amplios de guía | Se revisan tabla por tabla; el runtime recibe solo lo necesario. |

## Definición de terminado por módulo

- [ ] La ficha enlaza cada regla a una fuente o decisión aprobada.
- [ ] Las entidades, DTOs y mapper no exponen detalles de otro módulo.
- [ ] El servicio es la única frontera transaccional de la operación.
- [ ] Las constraints Oracle respaldan reglas de integridad relevantes.
- [ ] API documentada bajo `/api/v1` y respuestas de error coherentes.
- [ ] Pruebas verdes para éxito, rechazo, límites y rollback cuando aplique.
- [ ] Prueba de límite modular verde.
- [ ] No hay secretos, DDL ad hoc ni cambios fuera del alcance de la capa.

## Evidencia

- `C:\Users\pfloa\Downloads\Saludablemente_Brief.pdf`, pp. 4-7: fichas de módulos, reglas y alcance.
- `C:\Users\pfloa\PedroProjects\MyRepositories\LP2\bomerp-backend\src\main\java\pe\edu\upeu\bomerp\ventas\venta\...`: patrón de capas, DTOs, mapper, repositorio, servicio, controller y transacción.
- `C:\Users\pfloa\PedroProjects\MyRepositories\LP2\bomerp-backend\src\main\java\pe\edu\upeu\bomerp\exception\...` y `filter\CorrelationIdFilter.java`: patrones transversales de manejo de errores y trazabilidad.
## Documentos previos y orden de construcción

Antes de diseñar una migración o provisionar Oracle, revisar en este orden:

1. `docs/rebuild/05-module-ownership-and-dependency-matrix.md`: responsable, dependencia y contrato público del módulo.
2. `docs/rebuild/06-logical-data-model-and-oracle-translation.md`: entidad lógica, restricciones evidenciadas y traducciones Oracle pendientes.
3. La ficha del módulo y su capa de dominio con pruebas proporcionales.
4. Una migración concreta aprobada, su rollback y una autorización independiente de ejecución.

El baseline de cuentas Oracle está aprobado, pero no autoriza crear usuarios, objetos, grants ni ejecutar SQL.
