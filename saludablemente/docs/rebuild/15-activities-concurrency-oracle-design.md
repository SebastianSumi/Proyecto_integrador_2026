# Diseño Oracle de concurrencia para Actividades

**Estado: listo para activación manual.** El backend incorpora las dos defensas de concurrencia, pero Oracle solo las garantiza después de ejecutar `V001` y `V002` en el mismo esquema donde corre la aplicación. No hay Flyway ni DDL automático.

## Garantías objetivo

| Regla | Mecanismo | Estado |
|---|---|---|
| Una persona tiene como máximo una inscripción `INSCRITA` por actividad. | Índice único Oracle basado en función. | Implementado en `V001`; activo tras ejecución manual. |
| Dos actividades no se solapan en el mismo `lugar` exacto y `fecha`. | Fila de coordinación bloqueada por Oracle antes de consultar y escribir. | Implementado en `V002` y Java; activo tras ejecución manual. |

## Inscripción vigente

`V001__enrollment_active_uniqueness.sql` crea `UK_INSCRIPCION_VIGENTE`, equivalente a:

```sql
UNIQUE (
  CASE WHEN estado = 'INSCRITA' THEN actividad_id END,
  CASE WHEN estado = 'INSCRITA' THEN persona_id END
)
```

El service conserva la consulta previa para UX y usa `saveAndFlush()` para observar la violación dentro de la transacción. Solo un `ORA-00001` que menciona ese índice se transforma en 409; otro error de integridad se propaga sin disfrazarse de duplicado.

## Horarios solapados

`V002__activity_schedule_coordination.sql` crea `AGENDA_ACTIVIDAD_BLOQUEO` con clave primaria `(LUGAR, FECHA)` y el procedimiento `LOCK_AGENDA_ACTIVIDAD`.

### Algoritmo de escritura

1. `ActividadService` valida `horaInicio < horaFin`.
2. Para crear, llama el procedimiento con el `lugar` y `fecha` solicitados; para actualizar, bloquea tanto la clave anterior como la nueva.
3. En una actualización, las claves se ordenan por `lugar` exacto y luego fecha antes de bloquearlas. La misma clave se bloquea una sola vez.
4. El procedimiento inserta u obtiene la fila de coordinación y aplica `SELECT ... FOR UPDATE` sin hacer `COMMIT`.
5. Con el lock tomado, el service reutiliza `ActividadRepository.existeSolapamiento(...)` y guarda solo si no hay cruce.
6. El commit libera los locks; la siguiente solicitud de esa agenda entonces consulta el estado ya confirmado.

El procedimiento trata correctamente la primera fila: si dos transacciones intentan crearla, Oracle serializa la clave primaria. La que pierde espera, recibe `DUP_VAL_ON_INDEX` o inserta tras un rollback, y después bloquea la fila antes de continuar. Un lock JPA sobre actividades existentes no bastaría porque la primera agenda no tendría una fila para bloquear.

`lugar` se transmite tal como llega al service: no hay trim, normalización ni case-folding. Por lo tanto, la garantía conserva exactamente la semántica actual de igualdad de `ActividadRepository`.

## Activación y prueba requerida

1. Un DBA confirma nombres disponibles, permisos y que la aplicación se conecta al mismo Oracle.
2. Ejecuta `V001` y `V002` en orden desde `database/oracle/manual-migrations/`.
3. Antes de desplegar, prueba dos requests concurrentes que creen o muevan actividades al mismo `lugar`/`fecha` con horarios cruzados: una debe persistir y la otra devolver 409.
4. Prueba también una actualización que cambia de agenda para comprobar el orden determinista de locks.

Las pruebas unitarias validan delegación y orden, no pueden demostrar bloqueo Oracle real. Antes de ejecutar `V002`, los creates/updates de Actividad que llaman el procedimiento no están operativos contra esa base; el script es requisito de despliegue de esta versión.

## Rollback

Detener primero una versión del backend que llame `LOCK_AGENDA_ACTIVIDAD`. Luego evaluar datos y ejecutar el rollback comentado en `V002`. Revertir el DDL mientras el backend nuevo está activo haría fallar sus escrituras.
