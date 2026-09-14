# Datos Oracle y seguridad

**Baseline Oracle: APROBADO.** `ORACLE_JDBC_URL` es la única fuente de verdad para host, puerto y servicio PDB. `SYSTEM` se reserva para provisioning administrativo; `SALUDABLEMENTE_OWNER` es el dueño inicial de los objetos; `SALUDABLEMENTE_APP` es el runtime con privilegios mínimos. Esta aprobación no autoriza ejecutar SQL, crear usuarios, tablas o grants.

## Hechos, guía y decisiones

| Tipo | Contenido |
|---|---|
| Hecho del equipo | El modelo describe 20 entidades y relaciones alrededor de `Persona`, evaluación, actividades, usuarios y trazabilidad. |
| Hecho del equipo | Aptitud Física y Asistencia requieren evidencia de sincronización offline. |
| Hecho del equipo | Notificación y Auditoría usan referencias polimórficas sin FK física. |
| Patrón de guía | Separar el usuario propietario de la cuenta de runtime y otorgar privilegios explícitos. |
| **Aprobado** | Un único endpoint canónico: `ORACLE_JDBC_URL`. |
| **Aprobado** | `SALUDABLEMENTE_OWNER` posee tablas, constraints e índices; `SALUDABLEMENTE_APP` no recibe DDL. |
| Pendiente | Herramienta de migraciones, traducción Oracle del modelo y primera migración concreta. |

## Modelo conceptual que debe conservarse

| Grupo | Entidades lógicas del brief/schema |
|---|---|
| Identidad | Rol, Usuario, Usuario_Rol, Team, Persona. |
| Salud | Evaluacion_Nutricional, Antropometrico, Bioquimico, Aptitud_Fisica, referencia OMRON. |
| Programa | Actividad, Asistencia, Meta, Noticia, Notificacion. |
| Seguimiento | Alerta_Clinica, Recomendacion_IA, Reporte_Personal, Auditoria, Exportacion. |

El PDF de schema es una referencia MySQL/MariaDB. No define por sí solo nombres físicos, tipos Oracle, identidades, índices, nulabilidad ni constraints finales; cada uno se aprueba y versiona en una migración Oracle.

## Roles de cuenta aprobados

| Cuenta/schema | Responsabilidad | Límites |
|---|---|---|
| `SYSTEM` | Provisioning administrativo local de usuarios, cuota y grants. | Nunca es la cuenta Spring runtime ni se versiona. |
| `SALUDABLEMENTE_OWNER` | Dueño inicial de tablas, constraints e índices; ejecutor controlado de migraciones aprobadas. | Privilegios de creación estrictamente necesarios y cuota explícita. |
| `SALUDABLEMENTE_APP` | Conexión del backend. | `CREATE SESSION` y solo operaciones explícitamente otorgadas sobre objetos aprobados; sin DDL, DBA ni propiedad de objetos. |

No se crean schemas dueños por módulo en esta fase. Cualquier partición futura exige una decisión documentada con matriz de propiedad, FKs y grants cruzados.

## Endpoint y credenciales aprobados

- `ORACLE_JDBC_URL` contiene el endpoint canónico. No existe una segunda fuente de host, puerto o servicio PDB.
- SQL*Plus o SQLcl derivan su destino de `ORACLE_JDBC_URL` o de un transporte local derivado de forma segura. No se conserva `ORACLE_ADMIN_CONNECT_IDENTIFIER` como endpoint alternativo.
- Las credenciales existen únicamente en `.env` local ignorado por Git. No se copian a SQL, YAML, documentación, logs ni pruebas.
- Spring utiliza solo `ORACLE_APP_USERNAME` y `ORACLE_APP_PASSWORD` junto a `ORACLE_JDBC_URL`.

### Nombres de variables locales requeridas

| Finalidad | Variables |
|---|---|
| Endpoint canónico | `ORACLE_JDBC_URL` |
| Administración | `ORACLE_ADMIN_USERNAME`, `ORACLE_ADMIN_PASSWORD` |
| Schema dueño | `ORACLE_OWNER_USERNAME`, `ORACLE_OWNER_PASSWORD` |
| Runtime | `ORACLE_APP_USERNAME`, `ORACLE_APP_PASSWORD` |

La existencia de estas variables no autoriza conectarse ni crear objetos.

## Migraciones, verificación y rollback

1. Diseñar una migración pequeña por cambio coherente: usuario/grant, tabla, constraint, índice, catálogo o corrección.
2. Revisar en seco SQL Oracle: `NUMBER`, `VARCHAR2`, `TIMESTAMP`, identidades/secuencias, constraints y nombres.
3. Tras autorización de ejecución, aplicar primero en entorno local controlado como owner y verificar desde runtime que los grants sean suficientes y no excesivos.
4. Guardar versión aplicada, checksum, objetos, conteos y prueba de acceso runtime.
5. Definir rollback antes de ejecutar. Un `DROP` solo se admite para datos descartables con respaldo comprobado; cambios con datos requieren migración compensatoria.

**No autorizado todavía:** ejecución Oracle, provisioning, creación de usuarios/schemas, tablas, constraints, índices, grants, semillas, migración de datos o borrado de objetos.

## Reglas de seguridad y datos sensibles

| Riesgo | Control obligatorio |
|---|---|
| Datos de salud | Acceso por rol, trazabilidad y exposición mínima en DTO/API. |
| Integridad | FKs, `CHECK`, `UNIQUE` e índices definidos por regla de negocio. |
| Celular único activo | Solución Oracle específica, diseñada y probada; no trasladar sin revisión la columna generada MySQL. |
| Auditoría | Solo inserción; preservar actor, momento, acción y valores permitidos por la política de privacidad. |
| Referencias polimórficas | Validación en servicio, tipo de origen documentado y prueba de consistencia. |
| IA | No enviar datos personales/sensibles sin contrato y aprobación explícitos. |

## Pendientes

- [ ] Elegir la herramienta de migraciones y su ubicación versionada.
- [ ] Aprobar tablas/catálogos definitivos y la traducción Oracle.
- [ ] Confirmar semillas ficticias, responsable y eliminación segura.
- [ ] Definir política de retención/acceso para salud, auditoría, archivos e IA.
- [ ] Autorizar de forma independiente la primera ejecución Oracle.

## Evidencia

- `C:\Users\pfloa\Downloads\Saludablemente_Brief.pdf`, pp. 8-13: modelo relacional, reglas OMRON, offline y referencias polimórficas.
- `C:\Users\pfloa\Downloads\saludablemente_schema.pdf`, pp. 1-19: referencia lógica y DDL MySQL/MariaDB.
- `C:\Users\pfloa\Downloads\S01_01_esquemas.sql` y `S01_02_tablas.sql`: patrón académico owner/runtime y grants, adaptado sin secretos en texto plano.