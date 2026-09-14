# Fundación de Saludablemente

**Estado vigente.** Saludablemente se reconstruye como un monolito modular orientado a la gestión de salud ocupacional. La limpieza legacy filesystem ya fue ejecutada y retenida; Oracle sigue bloqueado. Este documento fija lenguaje, límites y reglas para las capas nuevas.

## Ruta rápida

1. Empezá por `docs/README.md` y el handoff vigente.
2. Respetá el baseline Oracle aprobado y su bloqueo operativo.
3. Construí Teams una capa por vez conforme a `10-teams-layered-module-blueprint.md` y `11-bomerp-class-by-class-reference.md`.
## Estado de las fuentes

| Clasificación | Fuente | Uso en esta reconstrucción |
|---|---|---|
| Hechos del equipo | `C:\Users\pfloa\Downloads\Saludablemente_Brief.pdf` | Fuente de alcance, actores, módulos, reglas y modelo lógico. |
| Hechos del equipo, borrador técnico | `C:\Users\pfloa\Downloads\saludablemente_schema.pdf` | Referencia lógica de entidades y relaciones. Su DDL es MySQL/MariaDB, no Oracle ejecutable. |
| Patrones de guía | `C:\Users\pfloa\PedroProjects\MyRepositories\LP2\bomerp-backend\...` y `C:\Users\pfloa\Downloads\S01_01_esquemas.sql`, `S01_02_tablas.sql` | Patrones académicos contrastados; no son requisitos ni se copian credenciales. |
| Evidencia histórica | Repositorio actual de Saludablemente | Inventario y aprendizaje; no define el nuevo diseño. |

## Contexto del producto

Saludablemente centraliza el seguimiento longitudinal del programa Universidad Saludable: registro de personal, evaluaciones nutricionales y de aptitud, actividades, asistencia, alertas, metas, recomendaciones, reportes y trazabilidad. El brief identifica como actores Administrador, Evaluador, Colaborador y Coordinador de actividades.

### Dentro del alcance confirmado

- Evaluación nutricional semestral con componente antropométrico y bioquímico importado.
- Aptitud física y asistencia con captura offline y sincronización.
- Gestión de personal, teams, actividades, noticias, notificaciones, metas, seguridad, auditoría, exportación, perfil y reporte personalizado.
- Alertas clínicas y recomendaciones de IA con trazabilidad.

### Fuera del alcance confirmado

- Análisis de laboratorio, historia clínica ocupacional completa o diagnóstico médico legal.
- Facturación, planillas e integración real con RRHH/nómina.
- Aplicación móvil nativa; el offline es web con sincronización.
- Integración real con wearables o HL7/FHIR.

## Principios arquitectónicos intencionales

| Principio | Decisión |
|---|---|
| Estructura | Monolito modular: cada módulo contiene su propio dominio, persistencia, aplicación y API. |
| Dependencias | Un módulo consume otro solo mediante un servicio público explícito; nunca mediante su repositorio. |
| API | REST bajo `/api/v1`; las entidades JPA no se exponen como contrato HTTP. |
| Reglas | Validación de forma en DTO; reglas de negocio y transacciones en el servicio de aplicación. |
| Persistencia | Relaciones y restricciones importantes se expresan en Oracle; `ORACLE_JDBC_URL` es el endpoint único, `SALUDABLEMENTE_OWNER` posee objetos y `SALUDABLEMENTE_APP` opera sin DDL. |
| Seguridad | Principio de mínimo privilegio, secretos fuera de Git y auditoría de cambios sensibles. |
| Evolución | Cambios de base de datos versionados y revisables; no `ddl-auto` creador de esquema en entornos compartidos. |
| Calidad | Pruebas enfocadas por comportamiento y pruebas de límites de módulos antes de integrar. |

La plataforma base será Java 21, Spring Boot, Spring Data JPA, Bean Validation y Oracle, consistente con la guía del repositorio y la configuración actual del proyecto. La incorporación de herramientas adicionales de migración, seguridad/JWT, mensajería u offline debe aprobarse por separado.

## Límites de propiedad

| Área propietaria | Responsabilidad confirmada | Dependencias que debe publicar |
|---|---|---|
| Seguridad y usuarios | Cuenta, rol y autorización | Identidad y autorización del usuario autenticado. |
| Personal | Persona, estado y pertenencia a team | Consulta pública de persona activa e identidad de persona. |
| Evaluación nutricional | Evaluación, antropometría y bioquímica | Eventos/consultas de evaluación terminada para alertas, metas y recomendaciones. |
| Aptitud física | Registro de pruebas y sincronización | Consulta o evento de aptitud sincronizada. |
| Actividades | Programación y estado de actividad | Estado público de actividad para asistencias y notificaciones. |
| Resto de módulos | Reglas descritas en el brief | Se definirán al iniciar cada módulo, sin acceso directo a repositorios ajenos. |

## Glosario

| Término | Significado |
|---|---|
| Persona | Colaborador del programa; eje de las relaciones de salud y participación. |
| Usuario | Cuenta autenticable asociada 1:1 con una persona. |
| Team | Equipo o grupo de personas usado para reportes agregados. |
| Evaluación nutricional | Cabecera semestral con detalle antropométrico y bioquímico opcional/importado. |
| Offline | Captura local que conserva evidencia de sincronización y previene duplicados al subir. |
| Módulo dueño | Único módulo que crea y modifica su modelo; otros módulos lo consultan por contrato. |
| Baja lógica | Inactivación que conserva el historial. |

## Decisiones pendientes antes de construir

- [ ] Orden de entrega de los quince módulos y responsables académicos, porque la tabla de asignación del brief requiere validación visual del equipo.
- [ ] Estrategia de identidad Oracle y de migraciones versionadas.
- [ ] Fuente oficial y aprobación de rangos clínicos OMRON; los valores de ejemplo no son evidencia clínica.
- [ ] Contrato de sincronización offline, resolución de conflictos e idempotencia.
- [ ] Alcance técnico, proveedor y controles de datos para recomendaciones de IA.

## Referencias verificables

- `C:\Users\pfloa\Downloads\Saludablemente_Brief.pdf`, pp. 3-8 y 13: dominio, alcance, módulos, relaciones y notas de diseño.
- `C:\Users\pfloa\Downloads\saludablemente_schema.pdf`, pp. 1-19: borrador lógico y DDL de referencia MySQL/MariaDB.
- `C:\Users\pfloa\PedroProjects\MyRepositories\Proyecto_integrador_2026\saludablemente\pom.xml`: Java 21, Spring Boot, JPA, Oracle y Modulith presentes actualmente; se considera evidencia de plataforma, no una obligación de conservar toda elección histórica.
## Mapa de documentación de reconstrucción

- `docs/rebuild/05-module-ownership-and-dependency-matrix.md`: asignación académica verificada, dependencias y orden propuesto.
- `docs/rebuild/06-logical-data-model-and-oracle-translation.md`: catálogo lógico completo y traducción Oracle pendiente de migración.

La construcción precede al provisioning: primero se aprueban módulo, contrato y modelo lógico; luego se aprueba una migración y, de forma separada, su ejecución.
