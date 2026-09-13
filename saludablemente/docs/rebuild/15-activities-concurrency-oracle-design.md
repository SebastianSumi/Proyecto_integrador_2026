# Diseño Oracle de concurrencia para Actividades

**Estado: diseñado, no implementado.** Actividad e Inscripción detectan conflictos en el flujo normal, pero una verificación previa a guardar no protege contra dos solicitudes concurrentes. No existe aún una herramienta ni una ubicación versionada aprobada para migraciones Oracle, por lo que este documento define el cambio que deberá revisarse antes de escribir o ejecutar DDL.

## Garantías objetivo

| Regla | Garantía de base de datos propuesta | Estado actual |
|---|---|---|
| Una persona tiene como máximo una inscripción `INSCRITA` por actividad. | Índice único Oracle basado en función que indexa `actividad_id` y `persona_id` solo cuando el estado es `INSCRITA`. | Pendiente de migración. |
| Dos actividades no se solapan en un lugar y fecha. | Serialización por una fila de agenda de `(lugar normalizado, fecha)` bloqueada con `SELECT ... FOR UPDATE` antes de comprobar y escribir. | Pendiente de migración y servicio. |

## Inscripción vigente

La migración debe crear un índice único basado en función equivalente a:

```sql
UNIQUE (
  CASE WHEN estado = 'INSCRITA' THEN actividad_id END,
  CASE WHEN estado = 'INSCRITA' THEN persona_id END
)
```

Oracle permite múltiples pares de valores nulos en un índice único; por eso las filas `CANCELADA` no bloquean una nueva inscripción. Los nombres físicos finales, tipos y el nombre de constraint deben definirse en la migración aprobada, no en esta guía.

Al activarlo, el servicio debe detectar la violación de **ese constraint identificado** al persistir/flush y traducirla a `InscripcionVigenteException` (409). No se debe convertir toda excepción de integridad en 409: podría ocultar una FK, `NOT NULL` u otro defecto.

## Solapamiento de agenda

Oracle no ofrece una exclusion constraint para intervalos horario como esta regla. Un índice sobre `lugar`, `fecha`, `hora_inicio` y `hora_fin` acelera consultas, pero no impide intervalos cruzados. La solución propuesta es una tabla de coordinación de agenda, con una fila única por `(lugar_normalizado, fecha)`:

1. En la misma transacción, crear u obtener la fila de coordinación.
2. Bloquear esa fila con `SELECT ... FOR UPDATE`.
3. Ejecutar la consulta de solapamiento y crear o actualizar la actividad.
4. Confirmar; las solicitudes del mismo lugar y fecha quedan serializadas.

Una actualización que cambia lugar o fecha debe bloquear ambas claves en orden determinista para evitar deadlocks. La migración debe definir cómo crear la fila de forma segura ante carreras y los índices de soporte; el servicio no debe basarse solo en `@Transactional`.

## Activación pendiente

Antes de implementar DDL o adaptar servicios se necesita:

- elegir y versionar la herramienta/ruta de migraciones Oracle;
- cerrar los nombres físicos y la representación Oracle de `LocalTime`;
- revisar rollback, datos existentes y el nombre estable del constraint de inscripción;
- probar solicitudes concurrentes contra Oracle y verificar que se traduzcan únicamente los conflictos esperados a 409.

Hasta entonces, no se afirma concurrencia segura ni se añaden scripts aislados fuera de una convención de migración.
