-- Manual migration only. Do not execute automatically from Spring Boot.
-- This migration is a deployment prerequisite for concurrent activity create/update operations.
--
-- It defines its own coordination table; it does not infer any physical column from ACTIVIDADES.
-- A DBA must confirm that SALUDABLEMENTE_OWNER can create tables and procedures, that the
-- names below are available, and that application connections use the same Oracle schema/database.

SELECT object_name, object_type, status
FROM all_objects
WHERE owner = 'SALUDABLEMENTE_OWNER'
  AND object_name IN ('AGENDA_ACTIVIDAD_BLOQUEO', 'LOCK_AGENDA_ACTIVIDAD')
ORDER BY object_name, object_type;

CREATE TABLE SALUDABLEMENTE_OWNER.AGENDA_ACTIVIDAD_BLOQUEO (
    LUGAR VARCHAR2(100 CHAR) NOT NULL,
    FECHA DATE NOT NULL,
    CONSTRAINT PK_AGENDA_ACTIVIDAD_BLOQUEO PRIMARY KEY (LUGAR, FECHA)
);

CREATE OR REPLACE PROCEDURE SALUDABLEMENTE_OWNER.LOCK_AGENDA_ACTIVIDAD (
    P_LUGAR IN VARCHAR2,
    P_FECHA IN DATE
) AS
    V_LOCKED NUMBER;
BEGIN
    BEGIN
        INSERT INTO SALUDABLEMENTE_OWNER.AGENDA_ACTIVIDAD_BLOQUEO (LUGAR, FECHA)
        VALUES (P_LUGAR, P_FECHA);
    EXCEPTION
        WHEN DUP_VAL_ON_INDEX THEN NULL;
    END;

    SELECT 1
    INTO V_LOCKED
    FROM SALUDABLEMENTE_OWNER.AGENDA_ACTIVIDAD_BLOQUEO
    WHERE LUGAR = P_LUGAR
      AND FECHA = P_FECHA
    FOR UPDATE;
END;
/

-- The procedure never commits. Its insert and SELECT ... FOR UPDATE participate in the caller's
-- transaction. On a first-row race, Oracle serializes the primary-key insert; the losing caller
-- handles DUP_VAL_ON_INDEX and then waits for the same row lock before returning.
--
-- Rollback only after stopping backend versions that call LOCK_AGENDA_ACTIVIDAD and assessing data:
-- DROP PROCEDURE SALUDABLEMENTE_OWNER.LOCK_AGENDA_ACTIVIDAD;
-- DROP TABLE SALUDABLEMENTE_OWNER.AGENDA_ACTIVIDAD_BLOQUEO;