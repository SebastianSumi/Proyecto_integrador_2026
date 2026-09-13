# Migraciones Oracle manuales

Esta carpeta conserva cambios DDL revisables que **no se ejecutan automáticamente**. No usa Flyway ni modifica el arranque de Spring; la ejecución la coordina el equipo con el DBA o responsable de la base.

## Aplicación

1. Revisar los prerrequisitos y consultas de cada script contra el esquema objetivo.
2. Confirmar que los nombres físicos de tabla y columnas coinciden con el diccionario Oracle.
3. Ejecutar los scripts una sola vez, en orden ascendente de versión, con un usuario autorizado para crear índices en `SALUDABLEMENTE_OWNER`.
4. Registrar ambiente, fecha y responsable de la ejecución fuera de este repositorio según el acuerdo del equipo.

## Convención

Los archivos siguen `VNNN__descripcion.sql`. El prefijo ordena la aplicación manual; no implica que alguna herramienta los detecte o ejecute.

## Rollback

Cada script debe documentar su reversión. Antes de revertir DDL se debe evaluar el impacto sobre datos y sobre la versión del backend que dependa de esa garantía.

## Alcance actual

`V001__enrollment_active_uniqueness.sql` protege una sola inscripción `INSCRITA` por actividad y persona. La serialización de horarios solapados permanece pendiente y no se resuelve con este índice.
