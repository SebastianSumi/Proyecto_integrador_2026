# Migraciones Oracle manuales

Esta carpeta conserva cambios DDL revisables que **no se ejecutan automáticamente**. No usa Flyway ni modifica el arranque de Spring; la ejecución la coordina el equipo con el DBA o responsable de la base.

## Aplicación

1. Revisar los prerrequisitos y consultas de cada script contra el esquema objetivo.
2. Confirmar que los nombres físicos de tabla y columnas coinciden con el diccionario Oracle cuando el script dependa de tablas existentes.
3. Ejecutar los scripts una sola vez, en orden ascendente de versión, con un usuario autorizado para crear índices, tablas y procedimientos en `SALUDABLEMENTE_OWNER`.
4. Registrar ambiente, fecha y responsable de la ejecución fuera de este repositorio según el acuerdo del equipo.
5. Desplegar el backend que llama una migración solo después de verificar que esa migración terminó correctamente en el mismo Oracle.

## Convención

Los archivos siguen `VNNN__descripcion.sql`. El prefijo ordena la aplicación manual; no implica que alguna herramienta los detecte o ejecute.

## Rollback

Cada script debe documentar su reversión. Antes de revertir DDL se debe evaluar el impacto sobre datos y sobre la versión del backend que dependa de esa garantía.

## Alcance actual

| Script | Garantía | Activación |
|---|---|---|
| `V001__enrollment_active_uniqueness.sql` | Una sola inscripción `INSCRITA` por actividad y persona. | Índice `UK_INSCRIPCION_VIGENTE`. |
| `V002__activity_schedule_coordination.sql` | Serializa comprobación y escritura de horarios por el mismo `lugar` exacto y `fecha`. | Tabla y procedimiento `LOCK_AGENDA_ACTIVIDAD`. |

`V002` crea sus propios objetos de coordinación. No modifica `ACTIVIDADES`, no normaliza ni cambia mayúsculas/minúsculas de `lugar`, y no debe ejecutarse si sus nombres ya están ocupados por objetos ajenos. Sus consultas iniciales y rollback son parte del procedimiento manual.
