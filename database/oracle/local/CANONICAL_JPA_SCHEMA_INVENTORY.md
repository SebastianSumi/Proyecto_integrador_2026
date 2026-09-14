# Oracle local schema inventory

**Decision:** the current JPA mappings are the only source of truth for the next local Oracle DDL. This inventory is a review artifact, not executable SQL and does not alter Docker, configuration, entities, or legacy scripts.

## Quick path

1. Review the required schemas and tables below against their owning entities.
2. Generate new, idempotent local-only DDL in the stated order; do not retrofit the legacy scripts in place.
3. Start Oracle only after the DDL receives review, then run validation and the runtime test plan.

**Scope measured from code:** 37 `@Entity` classes, 10 explicit schemas, and one mapped sequence. All unquoted identifiers are physically uppercase in Oracle even where Java writes lowercase names.

## Provisioning decisions

| Topic | Canonical decision |
|---|---|
| Source of truth | JPA `@Table`, `@Column`, identifier annotations and mapped associations in `src/main/java`. |
| Application account | One least-privilege runtime account needs `CREATE SESSION` plus only the DML/sequence privileges granted by each owner. Do **not** grant `DBA`. |
| IDs | `GenerationType.IDENTITY` requires Oracle identity columns; UUID mappings require `RAW(16)`; the one named sequence must exist exactly as mapped. |
| Cross-module IDs | Scalar IDs such as `idPersona`, `idTeam`, `actividadId` are logical references unless an entity declares `@JoinColumn`; do not invent cross-schema FKs. |
| Enum storage | Fields annotated `@Enumerated(EnumType.STRING)` require `VARCHAR2` sized for the mapped enum. |
| Validation | The application is configured with `ddl-auto: validate`; therefore the local DDL must be complete before runtime. |

## Canonical owner inventory

`PK` means the entity identifier. “Logical” means the code deliberately keeps only a scalar ID, with no JPA FK.

### `SALUDABLEMENTE_OWNER` — Activities, enrollments, teams and goals

| Table | Entity owner | Key columns / physical relations |
|---|---|---|
| `ACTIVIDADES` | `actividades.actividad.Actividad` | PK `ID` identity; `NOMBRE`, `FECHA`, `HORA_INICIO`, `HORA_FIN`, `LUGAR`, `ESTADO`, `CREADOR_ID` (logical). |
| `INSCRIPCIONES` | `actividades.inscripcion.Inscripcion` | PK `ID` identity; `ACTIVIDAD_ID` FK → `ACTIVIDADES(ID)`; `PERSONA_ID` logical; `ESTADO`, `INSCRITA_EN`, `CANCELADA_EN`. |
| `METAS` | `metas.meta.Meta` | PK `ID` identity; `PERSONA_ID` logical; `TIPO_META`, `DESCRIPCION`, `VALOR_OBJETIVO`, `VALOR_ACTUAL`, `FECHA_INICIO`, `FECHA_LIMITE`, `ESTADO`. |
| `TEAMS` | `teams.team.Team` | PK `ID` identity; `NAME`, `DESCRIPTION`, `ACTIVE`. |

**Additional local objects:** `UK_INSCRIPCION_VIGENTE` (V001 function-based unique index) and `AGENDA_ACTIVIDAD_BLOQUEO` plus `LOCK_AGENDA_ACTIVIDAD` (V002) belong here, after their base tables.

### `SLB_PERSONAL` — Personal

| Table | Entity owner | Key columns / physical relations |
|---|---|---|
| `PERSONA` | `personal.Persona` | PK `ID_PERSONA` identity; `NOMBRES`, surnames, `CELULAR` unique, birth/sex fields, `ID_TEAM` logical, `ACTIVO`. |
| `PREFERENCIA_COMUNICACION` | `personal.PreferenciaComunicacion` | PK `ID_PREFERENCIA` identity; `ID_PERSONA` unique FK → `PERSONA(ID_PERSONA)`; communication fields. |
| `CREDENCIAL_PROGRAMA` | `personal.CredencialPrograma` | PK `ID_CREDENCIAL` identity; `ID_PERSONA` FK → `PERSONA(ID_PERSONA)`; `CODIGO_QR_HASH` unique, credential state/type dates. |

### `SLB_APTITUDFISICA` — Aptitud Física

| Table | Entity owner | Key columns / physical relations |
|---|---|---|
| `CATALOGO_PRUEBA` | `aptitudfisica.CatalogoPrueba` | PK `ID_PRUEBA` identity; name, unit, description, active. |
| `EVALUACION_APTITUD` | `aptitudfisica.EvaluacionAptitud` | PK `ID_EVALUACION_APTITUD` identity; `ID_PERSONA` logical; registration date, total score, diagnostic and synchronization fields. |
| `DETALLE_PRUEBA_FISICA` | `aptitudfisica.DetallePruebaFisica` | PK `ID_DETALLE_APTITUD` identity; `ID_EVALUACION_APTITUD` FK → evaluation; `ID_PRUEBA` FK → catalog; obtained value and partial score. |

**S06 header-detail test target:** `EvaluacionAptitudServiceImpl.registrarEvaluacion(...)`. It constructs the evaluation and all physical-test details, computes `puntajeGlobal`/diagnostic, and saves the aggregate in one `@Transactional` method. The Oracle test must force an invalid detail (for example nonexistent catalog test) and prove neither header nor details persist.

### `SLB_NUTRICIONAL` — Nutritional evaluation

| Table | Entity owner | Key columns / physical relations |
|---|---|---|
| `EVALUACION_NUTRICIONAL` | `nutricional.EvaluacionNutricional` | PK `ID_EVALUACION` identity; `ID_PERSONA` logical, date/period/state/observations. |
| `DETALLE_ANTROPOMETRICO` | `nutricional.DetalleAntropometrico` | PK `ID_ANTROPOMETRICO` identity; `ID_EVALUACION` unique FK → evaluation; measures and diagnostics. |
| `DETALLE_BIOQUIMICO` | `nutricional.DetalleBioquimico` | PK `ID_BIOQUIMICO` identity; nullable, unique `ID_EVALUACION` FK → evaluation; biochemical data/import metadata. |

### `SALUD_PERSONAL` — Attendance, news and notifications

| Table | Entity owner | Key columns / physical relations |
|---|---|---|
| `ASISTENCIAS` | `asistencia.AsistenciaEntity` | PK `ID` identity; `ID_ACTIVIDAD` and `ID_PERSONA` logical; unique pair `(ID_ACTIVIDAD, ID_PERSONA)`; marking metadata. |
| `NOTICIAS` | `noticia.NoticiaEntity` | PK `ID_NOTICIA` identity; title/content/image/publication/state; author ID logical. |
| `NOTIFICACIONES` | `notificacion.NotificacionEntity` | PK `ID_NOTIFICACION` identity; `ID_PERSONA` logical; title/message/source/reference/read/send date. |

### `SALUD_ALERTA_CLINICA` — Clinical alerts

| Table | Entity owner | Key columns / physical relations |
|---|---|---|
| `ALERTA_CLINICA` | `alertas_clinicas.alerta.AlertaClinicaEntity` | PK `ID_ALERTA RAW(16)` UUID; person/evaluation IDs logical; severity/risk/state/snapshot/timestamps/version. |
| `ALERTA_CLINICA_DETALLE` | `alertas_clinicas.alerta.AlertaClinicaDetalleEntity` | PK `ID_DETALLE RAW(16)` UUID; `ID_ALERTA` FK → alert; indicator/reference/deviation/reincidence values. |
| `CATALOGO_ACCION_CORRECTIVA` | `alertas_clinicas.resolucion.CatalogoAccionCorrectivaEntity` | PK `ID_ACCION` from `SEQ_ACCION_CORRECTIVA`; `CODIGO_TIPIFICADO` unique; description/follow-up values. |
| `RESOLUCION_CLINICA` | `alertas_clinicas.resolucion.ResolucionClinicaEntity` | PK `ID_RESOLUCION RAW(16)` UUID; `ID_ALERTA` unique FK → alert; nullable `ID_ACCION_CORRECTIVA` FK → catalog; evaluator/resolution data. |

### `SALUD_AUDITORIA` — Audit

| Table | Entity owner | Key columns / physical relations |
|---|---|---|
| `BITACORA_SEGURIDAD` | `auditoria.AlertaSeguridadEntity` | PK `ID_ALERTA RAW(16)` UUID; anomaly/severity/state and resolution metadata. |
| `BITACORA_LECTURA` | `auditoria.BitacoraLecturaEntity` | Composite PK `(ID_LECTURA RAW(16), FECHA_ACCESO)`; person/entity/reader/access metadata. |
| `BITACORA_TRANSACCIONAL` | `auditoria.BitacoraTransaccionalEntity` | Composite PK `(ID_BITACORA RAW(16), FECHA_REGISTRO)`; sequence/event/entity/actor/hash/audit fields. |
| `EVENTO_AUDITORIA_DLQ` | `auditoria.EventoAuditoriaDlqEntity` | PK `ID_FALLO RAW(16)` UUID; event payload/error/retry state/timestamps. |
| `MANIFIESTO_ARCHIVADO_FRIO` | `auditoria.ManifiestoArchivadoFrioEntity` | PK `ID_MANIFIESTO RAW(16)` UUID; export range/storage/hash/purge state. |
| `PARTICION_BITACORA` | `auditoria.ParticionBitacoraEntity` | PK `ID_PARTICION RAW(16)` UUID; source/range/status/archive/purge metadata; `ID_MANIFIESTO` is scalar. |
| `POLITICA_RETENCION` | `auditoria.PoliticaRetencionEntity` | PK `ID_POLITICA RAW(16)` UUID; `TIPO_DATO` unique and retention settings. |
| `SESION_USUARIO` | `auditoria.SesionUsuarioEntity` | PK `ID_SESION RAW(16)` UUID; user/session/geolocation/device/revocation fields. |

### `SALUD_EXPORTACION` — Exportation

| Table | Entity owner | Key columns / physical relations |
|---|---|---|
| `TAREA_EXPORTACION` | `exportacion.TareaExportacionEntity` | PK `ID_TAREA RAW(16)` UUID; requester ID logical; state/format/privacy/filter/progress/storage/error fields. |
| `SUSCRIPTOR_WEBHOOK` | `exportacion.SuscriptorWebhookEntity` | PK `ID_SUSCRIPTOR RAW(16)` UUID; system/endpoint/HMAC secret/events/active/date. |
| `LOG_INTEROPERABILIDAD_FHIR` | `exportacion.LogInteroperabilidadFhirEntity` | PK `ID_LOG RAW(16)` UUID; client/resource/query/HTTP/IP/timing/date. |
| `BITACORA_DESCARGA_EXPORTACION` | `exportacion.BitacoraDescargaExportacionEntity` | PK `ID_DESCARGA RAW(16)` UUID; task/user/IP/agent/date. |

### `SALUD_PERFIL_REPORTE` — Reports

| Table | Entity owner | Key columns / physical relations |
|---|---|---|
| `REPORTE_PERSONAL` | `perfil_reporte.ReportePersonalEntity` | PK `ID_REPORTE RAW(16)` UUID; person ID logical; period/version/snapshot/storage/hash/state fields. |
| `COLA_GENERACION_REPORTES` | `perfil_reporte.ColaGeneracionReportesEntity` | PK `ID_TAREA RAW(16)` UUID; person/period/queue/retry/error/timestamps. |
| `REPORTE_DESCARGA_BITACORA` | `perfil_reporte.ReporteDescargaBitacoraEntity` | PK `ID_DESCARGA RAW(16)` UUID; report ID and requester ID logical; download metadata. |

### `SALUD_RECOMENDACIONES_IA` — Recommendations

| Table | Entity owner | Key columns / physical relations |
|---|---|---|
| `RECOMENDACION_IA` | `recomendaciones_ia.RecomendacionIAEntity` | PK `ID_RECOMENDACION RAW(16)` UUID; person/evaluation IDs logical; model/prompt/confidence/lifecycle fields. |
| `RECOMENDACION_IA_DETALLE` | `recomendaciones_ia.RecomendacionDetalleEntity` | PK `ID_DETALLE RAW(16)` UUID; `ID_RECOMENDACION` FK → recommendation; section and structured content. |

`CONTENIDO_ESTRUCTURADO` is a `Map<String,Object>` declared as `CLOB`; its Hibernate serialization/type support must be proven before emitting final Oracle DDL or a runtime test will fail.

## Ordered local DDL plan

1. Create the ten owner schemas and a least-privilege runtime account; grant only connection, table DML and `SEQ_ACCION_CORRECTIVA` access required by the mapped objects.
2. Create identity-backed parent tables and the sole sequence: `SLB_PERSONAL.PERSONA`, both evaluations, catalogs, activity/team/meta, and the UUID-backed roots.
3. Create physical child tables and only the same-schema FKs listed above.
4. Add unique constraints: person phone, credential hash, preference/evaluation one-to-ones, alert resolution, corrective-action code, attendance pair and retention type.
5. Create the five module-owned mapped table sets whose existing SQL may be used only after column-by-column reconciliation: alert, audit, export, report and recommendation.
6. Create `SALUDABLEMENTE_APP.EVENT_PUBLICATION` only if Spring Modulith persistent publication registry is enabled for the final runtime configuration.
7. Apply V001 then V002, after their preconditions confirm `SALUDABLEMENTE_OWNER` objects exist and are empty/consistent.
8. Seed only the required `CATALOGO_PRUEBA` rows and test persons; run `ddl-auto: validate`, then integration tests.

## Conflicts and blockers to resolve before executable DDL

| Priority | Evidence | Required resolution |
|---|---|---|
| P0 | `compose-dev.yml` has hard-coded `123456`, creates only `SALUDABLEMENTE_APP`, and mounts no initialization scripts. | Replace with local environment variables/secrets and an explicit reviewed initialization mechanism; do not reuse exposed credentials. |
| P0 | `src/main/resources/db/00_init_schemas.sql` omits `SALUDABLEMENTE_OWNER` and `SALUD_PERSONAL`, hard-codes passwords, and grants `DBA`. | Replace with least-privilege local provisioning DDL; do not execute it as-is. |
| P0 | Legacy `oracle/S01_02_tablas.sql` places `TEAMS` in `SALUD_PERSONAL` and defines plural `PERSONAS`; mappings require `SALUDABLEMENTE_OWNER.TEAMS` and `SLB_PERSONAL.PERSONA`. | Do not run S01_02 for the unified app; generate new DDL from this inventory. |
| P1 | Legacy `S01_personal.sql`, `S02_aptitudfisica.sql`, `S03_nutricional.sql` overlap mapped tables but are not the complete unified schema. | Reconcile column types, nullability, enum text and identity semantics before selectively replacing them. |
| P1 | The app reads objects across explicit schemas while the runtime account is distinct from every owner. | Define and test owner-to-runtime grants for every mapped table and sequence. |
| P1 | `RecomendacionDetalleEntity` maps a Java `Map` to `CLOB`. | Agree/prove the Hibernate JSON/converter strategy before final DDL/runtime validation. |
| P2 | V001/V002 rely on `SALUDABLEMENTE_OWNER` but the old base scripts do not create its activity/enrollment tables. | Apply them only after the new owner DDL and preserve their documented Oracle concurrency tests. |

## Review checklist

- [ ] Every row above maps to the current entity package and exact `@Table` schema/name.
- [ ] No cross-module scalar ID became an invented physical FK.
- [ ] Identity, UUID/RAW(16), sequence and composite-key requirements are all represented.
- [ ] Legacy SQL is retained only as evidence, not as the local unified source of truth.
- [ ] The final DDL has an explicit environment-variable credential boundary and no `DBA` runtime grant.

## Explicitly out of scope

This document does not create Docker assets, DDL, users, data, migrations, configuration, entities, tests, commits or a remote push. It also does not replace Oracle BD2 evidence; it prepares a reproducible local Oracle review baseline.
