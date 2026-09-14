# Modelo lógico de datos y traducción Oracle

**Resultado.** El schema PDF aporta un modelo lógico de veinte entidades para MySQL/MariaDB. Este catálogo preserva únicamente propiedades evidenciadas y separa la traducción Oracle propuesta de cualquier DDL. La cuenta física inicial aprobada es `SALUDABLEMENTE_OWNER`, pero aún no existe provisioning ni asignación materializada de objetos.

## Convenciones de lectura

| Marca | Significado |
|---|---|
| NN | `NOT NULL` evidenciado en el schema PDF. |
| UQ | `UNIQUE` evidenciado; implica índice único en el motor objetivo. |
| PK | Clave primaria evidenciada. |
| FK | Clave foránea evidenciada; el origen y la cardinalidad están descritos. |
| No definido | El PDF no establece la propiedad; no se inventa una decisión física. |

## Catálogo lógico: identidad y referencia

| Entidad / módulo candidato | PK y atributos requeridos | FK / cardinalidad / requiredness | UQ, estado, borrado e índices evidenciados |
|---|---|---|---|
| `ROL` / Seguridad | `id_rol` PK; `nombre` NN | Sin FK. | `nombre` UQ. Sin estado ni política de borrado definida. |
| `TEAM` / Teams | `id_team` PK; `nombre` NN; `activo` NN, default verdadero | `PERSONA.id_team` lo referencia; Team 1:N Persona. La FK en Persona es opcional. | Sin UQ de nombre en PDF. Regla funcional: no eliminar con personal activo; se permite inactivar. Índice PK. |
| `PERSONA` / Personal | `id_persona` PK; nombres, apellido paterno, fecha nacimiento, sexo y activo NN | `id_team` FK nullable a Team; Persona N:1 Team. | Celular único entre personas activas mediante columna generada MySQL/UQ; baja lógica por `activo`. PK y UQ condicionado; forma Oracle pendiente. |
| `USUARIO` / Seguridad | `id_usuario` PK; email, password_hash, activo, fecha_creacion, id_persona NN | `id_persona` FK NN a Persona; Usuario 1:1 Persona. | `email` UQ e `id_persona` UQ. Sin política de borrado definida. |
| `USUARIO_ROL` / Seguridad | PK compuesta `id_usuario`, `id_rol` NN | Ambos FK NN; Usuario N:M Rol. | UQ adicional no necesario porque coincide con PK; índices de PK. |
| `TABLA_REFERENCIA_OMRON` / Evaluación Nutricional | `id_referencia` PK; sexo, edad_min, edad_max, indicador, diagnostico NN | Sin FK. | Valores min/max son nullable en el schema. Sin UQ/estado/borrado definido; el brief la considera catálogo o constantes de reglas. |

## Catálogo lógico: salud y programa

| Entidad / módulo candidato | PK y atributos requeridos | FK / cardinalidad / requiredness | UQ, estado, borrado e índices evidenciados |
|---|---|---|---|
| `EVALUACION_NUTRICIONAL` / Evaluación Nutricional | `id_evaluacion` PK; persona, fecha_evaluacion, periodo_semestral NN | `id_persona` FK NN a Persona; Evaluación N:1 Persona. | Sin UQ ni borrado definido. |
| `ANTROPOMETRICO` / Evaluación Nutricional | `id_antropometrico` PK; evaluación, estatura_cm, peso_kg NN | `id_evaluacion` FK NN a Evaluación; relación 1:1. | `id_evaluacion` UQ; `CHECK` estatura/peso > 0. Sin borrado definido. |
| `BIOQUIMICO` / Evaluación Nutricional | `id_bioquimico` PK; evaluación e importador NN | Evaluación FK NN UQ: 1:1 opcional (la fila bioquímica puede no existir); importador FK NN a Usuario, N:1. | Sin otro UQ, estado o borrado definido. |
| `APTITUD_FISICA` / Aptitud Física | `id_aptitud` PK; persona, fecha_registro, sincronizado NN | Persona FK NN; Aptitud N:1 Persona. | UQ `(id_persona, fecha_registro)`; `sincronizado` default falso, fecha de sincronización nullable. Sin borrado definido. |
| `ACTIVIDAD` / Actividades | `id_actividad` PK; nombre, fecha, hora_inicio, hora_fin, lugar, estado, creador NN | creador FK NN a Usuario; Actividad N:1 Usuario y 1:N Asistencia. | Estado default Programada. Regla funcional: impedir solapamiento por lugar; no hay constraint/índice definido para ello ni borrado definido. |
| `META` / Metas | `id_meta` PK; persona, tipo, valor_objetivo, fecha_inicio, fecha_limite, estado NN | Persona FK NN; Meta N:1 Persona. | Estado default En curso; valor_actual nullable. Sin UQ/borrado definido. |
| `ASISTENCIA` / Asistencias | `id_asistencia` PK; actividad, persona, hora_marcado, metodo_registro, sincronizado NN | Actividad y Persona FK NN; Asistencia N:1 a cada una. | UQ `(id_actividad, id_persona)`; sincronizado default falso, fecha nullable. Regla funcional: no registrar en actividad finalizada/cancelada. |
| `NOTICIA` / Noticias | `id_noticia` PK; título, contenido, estado, autor NN | autor FK NN a Usuario; Noticia N:1 Usuario. | Estado default Borrador; fecha publicación nullable. Sin UQ/borrado definido; el brief indica despublicar. |
| `NOTIFICACION` / Notificaciones | `id_notificacion` PK; persona, canal, origen_modulo, leído, fecha_envio NN | persona FK NN a Persona; Notificación N:1 Persona. `id_origen_referencia` nullable polimórfica sin FK. | leído default falso, fecha default actual. Sin UQ/borrado definido. |

## Catálogo lógico: seguimiento y salida

| Entidad / módulo candidato | PK y atributos requeridos | FK / cardinalidad / requiredness | UQ, estado, borrado e índices evidenciados |
|---|---|---|---|
| `ALERTA_CLINICA` / Alertas Clínicas | `id_alerta` PK; persona, evaluación, tipo_indicador, severidad, estado, fecha_generacion NN | Persona, Evaluación y Usuario-atendedor (nullable) como FK; Alerta N:1 Persona/Evaluación y N:1 opcional Usuario. | Estado default Pendiente; fecha_atención nullable. Sin UQ/borrado definido. |
| `RECOMENDACION_IA` / Recomendaciones IA | `id_recomendacion` PK; persona, evaluación, tipo, fecha_generacion, vigente NN | Persona y Evaluación FK NN; Recomendación N:1 a cada una. | vigente default verdadero. Contenido, contexto y modelo son nullable en schema. Sin UQ/borrado definido. |
| `REPORTE_PERSONAL` / Perfil/Reporte | `id_reporte` PK; persona, fecha_generacion, periodo NN | Persona FK NN; Reporte N:1 Persona. | URL nullable. Sin UQ/borrado definido; brief exige historial por período, pero no fija UQ. |
| `AUDITORIA` / Auditoría | `id_auditoria` PK; usuario, módulo, entidad, acción, fecha_evento NN | Usuario FK NN; Auditoría N:1 Usuario. `id_entidad_afectada` nullable polimórfica sin FK. | Fecha default actual. Regla funcional: solo inserción, nunca editar/eliminar. Sin UQ adicional. |
| `EXPORTACION` / Exportación | `id_exportacion` PK; solicitante, tipo_formato, modulo_origen, fecha_generacion NN | solicitante FK NN a Usuario; Exportación N:1 Usuario. | Fecha default actual; filtros y URL nullable. Sin UQ/borrado definido. |

## Relaciones y políticas de eliminación

- El PDF confirma las cardinalidades anotadas arriba, incluida Persona como eje de Evaluación, Aptitud, Asistencia, Meta, Alerta, Recomendación, Reporte y Notificación.
- No define cláusulas `ON DELETE` para las FKs. La política física Oracle de cada relación está **pendiente**; no se debe asumir `CASCADE`.
- Las únicas reglas de ciclo de vida explícitas son: Team se inactiva/no se elimina con personal activo; Persona admite baja lógica; Auditoría no se edita ni elimina; Noticias admite despublicación; estados de actividad, meta, alerta y recomendación se describen arriba.
- Índices adicionales sobre FKs, filtros, reportes o solapamiento de actividades no aparecen en la fuente y quedan pendientes de diseño/perfilado.

## Traducción lógica a Oracle: propuesta no ejecutable

| Elemento lógico MySQL/MariaDB | Traducción Oracle candidata | Estado |
|---|---|---|
| `INT AUTO_INCREMENT` | `NUMBER GENERATED BY DEFAULT AS IDENTITY` | Propuesta; validar nombres y estrategia por migración. |
| `VARCHAR(n)` | `VARCHAR2(n)` | Propuesta directa, pendiente de confirmar semántica/caracteres. |
| `TEXT` | `CLOB` | Propuesta; revisar impacto de API/búsqueda. |
| `DATETIME` | `TIMESTAMP` | Propuesta; definir zona horaria y precisión. |
| `DATE` | `DATE` | Candidato; confirmar si basta precisión de día. |
| `TIME` | No hay equivalencia directa definida por la fuente | **Pendiente:** decidir `TIMESTAMP`, intervalo o representación validada para Actividad. |
| `BOOLEAN` | `NUMBER(1)` con `CHECK` candidato | Propuesta portable; confirmar versión Oracle y convención `0/1`. |
| Columna generada celular activo | Columna virtual o índice único basado en función | **Pendiente:** diseñar y probar en Oracle para conservar unicidad solo de activos. |
| FK sin `ON DELETE` | FK Oracle sin política implícita adicional | **Pendiente:** aprobar política por relación; nunca inferir cascade. |
| PK/UQ | `PRIMARY KEY` / `UNIQUE` con sus índices asociados | Propuesta para preservar lo evidenciado. |

### Mapeo físico inicial

| Alcance lógico | Schema físico candidato | Responsable funcional | Estado de provisioning |
|---|---|---|---|
| Las veinte entidades del catálogo | `SALUDABLEMENTE_OWNER` | Módulo indicado en cada fila | Baseline de cuenta aprobado; objetos y usuarios **no creados**. |
| Acceso desde backend | `SALUDABLEMENTE_APP` | Runtime transversal | Grants por tabla/operación pendientes de migración aprobada; sin DDL. |
| Provisioning | `SYSTEM` | Administración local | No autorizado para ejecución. |

La propiedad funcional de módulo no equivale todavía a un schema Oracle independiente. La asignación física por tabla, grants y cualquier partición futura solo se materializan en migraciones aprobadas.

## Checklist antes de la primera migración

- [ ] Aprobar el diccionario lógico y las propiedades marcadas pendientes.
- [ ] Definir política `ON DELETE`, índices adicionales y representación de `TIME`/booleanos Oracle.
- [ ] Confirmar la fuente clínica OMRON y su estrategia de catálogo/constantes.
- [ ] Elegir herramienta y formato de migraciones.
- [ ] Revisar una migración Oracle concreta y su rollback.
- [ ] Autorizar de manera independiente su ejecución.

## Evidencia

- `C:\Users\pfloa\Downloads\saludablemente_schema.pdf`, pp. 1-19: DDL lógico, columnas, PK, FK, UQ, defaults y semillas de referencia MySQL/MariaDB.
- `C:\Users\pfloa\Downloads\Saludablemente_Brief.pdf`, pp. 8-13: cardinalidades, diccionario, notas OMRON, offline, referencias polimórficas y reglas de ciclo de vida.
- `docs/rebuild/01-oracle-data-and-security.md`: baseline de cuentas Oracle aprobado, sin autorización de ejecución.