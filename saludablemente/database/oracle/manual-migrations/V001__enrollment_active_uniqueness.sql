-- Manual migration only. Do not execute automatically from Spring Boot.
-- Prerequisites:
--   1. Confirm the physical names in ALL_TAB_COLUMNS for SALUDABLEMENTE_OWNER.INSCRIPCIONES.
--   2. Resolve every duplicate returned by the query below before creating the index.
--   3. Confirm UK_INSCRIPCION_VIGENTE is not already used in SALUDABLEMENTE_OWNER.
--
-- Expected names are derived from the current JPA mapping and Spring's naming convention:
-- INSCRIPCIONES, ACTIVIDAD_ID, PERSONA_ID and ESTADO. A DBA must verify them in Oracle.

SELECT column_name
FROM all_tab_columns
WHERE owner = 'SALUDABLEMENTE_OWNER'
  AND table_name = 'INSCRIPCIONES'
ORDER BY column_id;

SELECT actividad_id, persona_id, COUNT(*) AS active_enrollment_count
FROM SALUDABLEMENTE_OWNER.INSCRIPCIONES
WHERE estado = 'INSCRITA'
GROUP BY actividad_id, persona_id
HAVING COUNT(*) > 1;

CREATE UNIQUE INDEX SALUDABLEMENTE_OWNER.UK_INSCRIPCION_VIGENTE
    ON SALUDABLEMENTE_OWNER.INSCRIPCIONES (
        CASE WHEN ESTADO = 'INSCRITA' THEN ACTIVIDAD_ID END,
        CASE WHEN ESTADO = 'INSCRITA' THEN PERSONA_ID END
    );

-- Rollback, only after assessing data and backend compatibility:
-- DROP INDEX SALUDABLEMENTE_OWNER.UK_INSCRIPCION_VIGENTE;
