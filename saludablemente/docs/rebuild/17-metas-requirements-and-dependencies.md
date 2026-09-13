# Preparación del módulo Metas: requisitos y dependencias

**Decisión actual.** Metas no inicia implementación Java todavía. El módulo requiere contratos públicos de Personal y Evaluación Nutricional que aún no están definidos en el código. Esta ficha fija lo confirmado, separa las decisiones pendientes y deja una ruta segura para comenzar sin acoplarse a repositories ajenos.

## Ruta rápida

1. Personal publica un contrato para validar y consultar la persona activa.
2. Evaluación Nutricional publica un contrato para entregar un indicador autorizado de una evaluación terminada.
3. El equipo acuerda cómo una evaluación afecta el avance de una meta.
4. Con esos contratos, Metas se construye capa por capa y con pruebas focalizadas.

## Alcance confirmado

| Tema | Hecho confirmado | Fuente |
|---|---|---|
| Propietario | Metas es un módulo no transaccional asignado a Pedro. | Matriz de módulos. |
| Relación principal | Una meta pertenece a una persona; una persona puede tener varias metas. | Modelo lógico. |
| Datos lógicos | `id_meta`, persona, tipo, valor objetivo, fecha de inicio, fecha límite, estado y valor actual opcional. | Modelo lógico. |
| Estado inicial | El modelo lógico indica estado por defecto `En curso`. | Modelo lógico. |
| Integraciones | Metas depende de Personal y Evaluación Nutricional. | Matriz de módulos. |
| Límite modular | Metas debe usar servicios públicos, nunca repositories de Personal o Evaluación Nutricional. | Guía de contribución y matriz. |

## Lo que Metas necesita de otros módulos

| Dependencia | Contrato público necesario | Uso esperado por Metas | Estado |
|---|---|---|---|
| Personal | Validar que una persona exista y esté activa; obtener su identidad mínima. | Crear o consultar metas de una persona válida. | Pendiente de definición/publicación. |
| Evaluación Nutricional | Exponer una evaluación terminada y el indicador autorizado para seguimiento. | Actualizar o calcular avance solo con datos autorizados. | Pendiente de definición/publicación. |

No se define aquí la firma Java, endpoint ni evento de esos contratos. Deben acordarse con sus módulos propietarios antes de codificar Metas.

## Preguntas de negocio pendientes

Estas reglas no aparecen confirmadas en el modelo lógico actual y no se deben inventar en entity, DTO, service o API:

- Qué valores admite `tipo` y qué indicador nutricional, si alguno, corresponde a cada tipo.
- Cómo se calcula `valorActual` desde una evaluación: reemplazo, acumulación, promedio u otra fórmula.
- Qué significa completar, vencer, cancelar o reabrir una meta, y qué transiciones de estado son válidas.
- Si se permite más de una meta activa del mismo tipo para una persona.
- Si una evaluación actualiza metas automáticamente, por acción explícita o mediante un evento.
- Qué debe ocurrir cuando la fecha límite vence.

## Gate de inicio

No iniciar entity ni contratos HTTP hasta cumplir estos puntos:

- [ ] Personal expone y documenta su contrato público requerido.
- [ ] Evaluación Nutricional expone y documenta el indicador autorizado requerido.
- [ ] Se acuerda la regla de avance y el ciclo de estados de Meta.
- [ ] Se confirma si la actualización llega de forma síncrona o por evento.
- [ ] Se valida que ningún contrato requiera acceso directo a repository externo.

## Plan de implementación después de los gates

1. Confirmar campos, relaciones por ID y estados; implementar `entity` y prueba focalizada.
2. Crear DTOs de entrada/salida con validaciones basadas solo en reglas acordadas.
3. Implementar mapper MapStruct y prueba directa.
4. Agregar repository propio con consultas mínimas justificadas.
5. Implementar service transaccional y colaboración exclusiva con contratos públicos.
6. Exponer controller `/api/v1` con `@Valid` y errores HTTP globales.
7. Ejecutar prueba focalizada, `mvn test` y `git diff --check` por cada capa; actualizar la documentación afectada.

## Fuera de alcance de esta ficha

- No crea entity, DTO, mapper, repository, service, controller ni endpoints de Metas.
- No crea SQL, Oracle, migraciones, POM, Docker o configuración.
- No decide DDL, relaciones JPA entre módulos ni reglas de cálculo no confirmadas.

## Referencias

- [Matriz de módulos, propiedad y dependencias](05-module-ownership-and-dependency-matrix.md)
- [Modelo lógico de datos y traducción Oracle](06-logical-data-model-and-oracle-translation.md)
- [Vertical de referencia para módulos posteriores](12-reference-vertical-for-next-modules.md)
