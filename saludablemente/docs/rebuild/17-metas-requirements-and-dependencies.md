# Preparación del módulo Metas: requisitos y dependencias

**Decisión actual.** Metas puede iniciar como vertical autónoma con `personaId` escalar, sin relación JPA ni acceso a repositories externos. La integración con Personal y Evaluación Nutricional permanece diferida hasta que sus propietarios publiquen contratos públicos. Esta ficha separa ese núcleo implementable de las decisiones de integración pendientes.

## Ruta rápida

1. Construir el núcleo propio de Metas con `personaId` obligatorio y reglas locales confirmadas.
2. Personal publica un contrato para validar y consultar la persona activa.
3. Evaluación Nutricional publica un contrato para entregar un indicador autorizado de una evaluación terminada.
4. El equipo acuerda cómo esas mediciones afectan el avance de una meta.
5. Integrar exclusivamente mediante esos contratos públicos y pruebas focalizadas.

## Alcance confirmado

| Tema | Hecho confirmado | Fuente |
|---|---|---|
| Propietario | Metas es un módulo no transaccional asignado a Pedro. | Matriz de módulos. |
| Relación principal | Una meta pertenece a una persona; una persona puede tener varias metas. | Modelo lógico. |
| Datos lógicos | `id_meta`, persona, tipo, valor objetivo, fecha de inicio, fecha límite, estado y valor actual opcional. | Modelo lógico. |
| Estado inicial | El modelo lógico indica estado por defecto `En curso`. | Modelo lógico. |
| Propósito | Una Meta expresa el objetivo de mejora de salud de una persona, su medición objetivo, avance observado y plazo. No es la fuente clínica de la medición. | Decisión de módulo acordada. |
| Cumplimiento inicial | Un supervisor confirma explícitamente `CUMPLIDA`; Metas no calcula ni infiere cumplimiento desde valores o evaluaciones. | Decisión de módulo acordada. |
| Eliminación inicial | Se permite eliminar únicamente una meta `EN_CURSO`; una `CUMPLIDA` se conserva como historial. | Decisión de módulo acordada. |
| Integraciones | Metas depende de Personal y Evaluación Nutricional. | Matriz de módulos. |
| Límite modular | Metas debe usar servicios públicos, nunca repositories de Personal o Evaluación Nutricional. | Guía de contribución y matriz. |

## Lo que Metas necesita de otros módulos

| Dependencia | Contrato público necesario | Uso esperado por Metas | Estado |
|---|---|---|---|
| Personal | Validar que una persona exista y esté activa; obtener su identidad mínima. | Validar el `personaId` antes de crear o consultar metas en una integración posterior. | Pendiente de definición/publicación; el núcleo usa referencia escalar. |
| Evaluación Nutricional | Exponer una evaluación terminada y el indicador autorizado para seguimiento. | Proponer o actualizar avance solo con datos autorizados, cuando exista una regla aprobada. | Pendiente de definición/publicación. |

No se define aquí la firma Java, endpoint ni evento de esos contratos. Deben acordarse con sus módulos propietarios antes de integrar Metas con ellos.

## Reglas del núcleo inicial

- `personaId` es obligatorio e inmutable después de crear la meta; representa al padre lógico de la Meta.
- El núcleo no puede comprobar todavía que la persona exista o esté activa: esa validación se añadirá mediante el contrato público de Personal.
- El repository consulta por personaId escalar. No requiere @EntityGraph, porque Meta no declara asociaciones JPA que cargar; se reevalúa solo si un contrato futuro justifica una asociación propia.
- `valorActual` puede registrarse o actualizarse por la operación supervisada, pero Metas no lo deriva ni lo recalcula desde Evaluación Nutricional.
- La confirmación de cumplimiento es explícita por el supervisor; no se deduce con `>=` ni `<=` porque la dirección depende del indicador.
- Solo una meta `EN_CURSO` puede modificarse o eliminarse. Las metas `CUMPLIDA` y `VENCIDA` son inmutables en el núcleo inicial para preservar historial.
- `VENCIDA` queda reservado en el modelo. No se aplicará automáticamente hasta acordar quién la activa, cuándo y si admite reapertura.

## API del núcleo inicial

La API expone DTOs, no entities JPA, bajo `/api/v1/metas`:

- `GET /api/v1/metas`: lista las metas.
- `GET /api/v1/metas/persona/{personaId}`: lista las metas del padre lógico indicado.
- `GET /api/v1/metas/{id}`: consulta una meta.
- `POST /api/v1/metas`: crea una meta `EN_CURSO`.
- `PUT /api/v1/metas/{id}`: actualiza únicamente los campos mutables de una meta en curso.
- `PATCH /api/v1/metas/{id}/cumplimiento`: confirma explícitamente una meta como `CUMPLIDA`.
- `DELETE /api/v1/metas/{id}`: elimina únicamente una meta `EN_CURSO`.

La validación de cuerpos usa `@Valid`; las excepciones de negocio y de recurso se traducen en el manejo global existente. No se agrega CORS por módulo: es una configuración transversal del despliegue.

## Preguntas de negocio pendientes

Estas reglas no aparecen confirmadas en el modelo lógico actual y no se deben inventar en entity, DTO, service o API:

- Qué valores admite `tipo` y qué indicador nutricional, si alguno, corresponde a cada tipo.
- Cómo se calcula `valorActual` desde una evaluación: reemplazo, acumulación, promedio u otra fórmula.
- Qué condición de autorización debe cumplir el supervisor que confirma el cumplimiento.
- Qué significa vencer o reabrir una meta, quién activa esas transiciones y qué transiciones son válidas.
- Si se permite más de una meta activa del mismo tipo para una persona.
- Si una evaluación propone una actualización de `valorActual` por acción explícita o mediante un evento.
- Qué debe ocurrir cuando la fecha límite vence.

## Gates de integración

El núcleo local puede comenzar. No se debe implementar la integración automática con módulos externos hasta cumplir estos puntos:

- [ ] Personal expone y documenta su contrato público requerido.
- [ ] Evaluación Nutricional expone y documenta el indicador autorizado requerido.
- [ ] Se acuerda la regla de avance automático y el ciclo de estados diferido de Meta.
- [ ] Se confirma si una futura actualización desde Evaluación llega de forma síncrona o por evento.
- [ ] Se valida que ningún contrato requiera acceso directo a repository externo.

## Plan de implementación del núcleo

1. Confirmar campos, relaciones por ID y estados; implementar `entity` y prueba focalizada.
2. Crear DTOs de entrada/salida con validaciones basadas solo en reglas acordadas.
3. Implementar mapper MapStruct y prueba directa.
4. Agregar repository propio con consultas mínimas justificadas.
5. Implementar service transaccional con creación, consulta, actualización de campos permitidos, confirmación explícita de cumplimiento y eliminación de `EN_CURSO`.
6. Exponer controller `/api/v1` con `@Valid` y errores HTTP globales.
7. Ejecutar prueba focalizada, `mvn test` y `git diff --check` por cada capa; actualizar la documentación afectada.

## Fuera de alcance de esta ficha

- No crea entity, DTO, mapper, repository, service, controller ni endpoints de Metas.
- No crea SQL, Oracle, migraciones, POM, Docker o configuración.
- No decide DDL, relaciones JPA entre módulos, cálculo automático, sincronización de evaluaciones ni reglas de vencimiento no confirmadas.

## Referencias

- [Matriz de módulos, propiedad y dependencias](05-module-ownership-and-dependency-matrix.md)
- [Modelo lógico de datos y traducción Oracle](06-logical-data-model-and-oracle-translation.md)
- [Vertical de referencia para módulos posteriores](12-reference-vertical-for-next-modules.md)
