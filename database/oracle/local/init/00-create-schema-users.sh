#!/usr/bin/env bash
set -euo pipefail

# This runs only while gvenzl initializes a fresh data volume. It never stores a password.
# Passwords are supplied by Docker environment variables from the ignored .env.local file.
OWNER_PASSWORD="${LOCAL_ORACLE_OWNER_PASSWORD:?LOCAL_ORACLE_OWNER_PASSWORD is required}"
CONNECT="sys/${ORACLE_PASSWORD}@//localhost:1521/FREEPDB1 as sysdba"

sqlplus -s "$CONNECT" <<SQL
WHENEVER SQLERROR EXIT SQL.SQLCODE
DECLARE
  PROCEDURE ensure_user(p_username VARCHAR2) IS
    v_count NUMBER;
  BEGIN
    SELECT COUNT(*) INTO v_count FROM dba_users WHERE username = p_username;
    IF v_count = 0 THEN
      EXECUTE IMMEDIATE 'CREATE USER ' || p_username || ' IDENTIFIED BY "' || '${OWNER_PASSWORD}' || '" DEFAULT TABLESPACE USERS QUOTA UNLIMITED ON USERS';
    END IF;
    EXECUTE IMMEDIATE 'GRANT CREATE SESSION, CREATE TABLE, CREATE SEQUENCE, CREATE PROCEDURE, CREATE TRIGGER TO ' || p_username;
  END;
BEGIN
  ensure_user('SALUDABLEMENTE_OWNER');
  ensure_user('SLB_PERSONAL');
  ensure_user('SLB_APTITUDFISICA');
  ensure_user('SLB_NUTRICIONAL');
  ensure_user('SALUD_PERSONAL');
  ensure_user('SALUD_ALERTA_CLINICA');
  ensure_user('SALUD_AUDITORIA');
  ensure_user('SALUD_EXPORTACION');
  ensure_user('SALUD_PERFIL_REPORTE');
  ensure_user('SALUD_RECOMENDACIONES_IA');
END;
/
EXIT
SQL
