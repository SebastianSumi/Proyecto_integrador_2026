# Diseño Oracle de concurrencia para Actividades

**Estado: inscripción implementada como migración manual; agenda pendiente.** Actividad e Inscripción detectan conflictos en el flujo normal, pero una verificación previa a guardar no protege contra dos solicitudes concurrentes. La garantía de inscripción se incorpora en un script manual versionado; no existe Flyway ni ejecución automática desde Spring.

## Garantías objetivo

| Regla | Garantía de base de datos propuesta | Estado actual |
|---|---|---|
| Una persona tiene como máximo una inscripción `INSCRITA` por actividad. | Índice único Oracle basado en función que indexa `actividad_id` y `persona_id` solo cuando el estado es `INSCRITA`. | Disponible en script manual; queda activa solo tras ejecutarlo en Oracle. |
| Dos actividades no se solapan en un lugar y fecha. | Serialización por una fila de agenda de `(lugar normalizado, fecha)` bloqueada con `SELECT ... FOR UPDATE` antes de comprobar y escribir. | Pendiente de migración y servicio. |

## Inscripción vigente

`database/oracle/manual-migrations/V001__enrollment_active_uniqueness.sql` crea el índice único basado en función `UK_INSCRIPCION_VIGENTE` equivalente a:

```sql
UNIQUE (
  CASE WHEN estado = 'INSCRITA' THEN actividad_id END,
  CASE WHEN estado = 'INSCRITA' THEN persona_id END
)
```

Oracle permite múltiples pares de valores nulos en un índice único; por eso las filas `CANCELADA` no bloquean una nueva inscripción. El script deriva `INSCRIPCIONES`, `ACTIVIDAD_ID`, `PERSONA_ID` y `ESTADO` del mapeo JPA y de la convención actual, pero exige confirmar esos nombres mediante `ALL_TAB_COLUMNS` antes de ejecutarlo. También detecta duplicados existentes, que deben resolverse antes del `CREATE INDEX`.

Al activarlo, el servicio conserva la consulta previa para UX y usa `saveAndFlush` para recibir la violación dentro de su frontera transaccional. Solo traduce un `ORA-00001` que identifica `UK_INSCRIPCION_VIGENTE` a `InscripcionVigenteException` (409); una FK, `NOT NULL` u otro error de integridad se propaga sin disfrazarse de duplicado.

## Solapamiento de agenda

Oracle no ofrece una exclusion constraint para intervalos horario como esta regla. Un índice sobre `lugar`, `fecha`, `hora_inicio` y `hora_fin` acelera consultas, pero no impide intervalos cruzados. La solución propuesta es una tabla de coordinación de agenda, con una fila única por `(lugar_normalizado, fecha)`:

1. En la misma transacción, crear u obtener la fila de coordinación.
2. Bloquear esa fila con `SELECT ... FOR UPDATE`.
3. Ejecutar la consulta de solapamiento y crear o actualizar la actividad.
4. Confirmar; las solicitudes del mismo lugar y fecha quedan serializadas.

Una actualización que cambia lugar o fecha debe bloquear ambas claves en orden determinista para evitar deadlocks. La migración debe definir cómo crear la fila de forma segura ante carreras y los índices de soporte; el servicio no debe basarse solo en `@Transactional`.

## Activación y pendientes

La inscripción requiere ejecutar manualmente `V001__enrollment_active_uniqueness.sql` según su README. Antes de implementar el bloqueo de agenda se necesita:

- cerrar los nombres físicos y la representación Oracle de `LocalTime`;
- probar solicitudes concurrentes contra Oracle y verificar que la violación del índice se traduzca únicamente a 409.

Hasta que el script se ejecute, no se afirma concurrencia segura para Inscripción. El solapamiento de agenda continúa pendiente de DDL y servicio.
