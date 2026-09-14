#!/usr/bin/env bash
set -euo pipefail

# A fresh owner receives its complete mapped baseline. A completed owner is preserved;
# a partially populated owner stops provisioning so this baseline never silently mixes DDL sets.
table_count() {
  local owner="$1"
  sqlplus -s "${owner}/${SCHEMA_OWNER_PASSWORD}@//localhost:1521/FREEPDB1" <<'SQL'
WHENEVER SQLERROR EXIT SQL.SQLCODE
set heading off feedback off pages 0
select count(*) from user_tables;
exit
SQL
}

run_owner() {
  local owner="$1"
  local expected_tables="$2"
  local script="$3"
  local count
  count=$(table_count "$owner")
  count=$(echo "$count" | tr -d '[:space:]')
  if [[ "$count" == "0" ]]; then
    sqlplus -s "${owner}/${SCHEMA_OWNER_PASSWORD}@//localhost:1521/FREEPDB1" @"/container-entrypoint-initdb.d/${script}"
    count=$(table_count "$owner")
    count=$(echo "$count" | tr -d '[:space:]')
    if [[ "$count" != "$expected_tables" ]]; then
      echo "${owner}: expected ${expected_tables} tables after ${script} but found ${count}." >&2
      exit 1
    fi
  elif [[ "$count" == "$expected_tables" ]]; then
    echo "${owner}: complete local baseline already exists; preserving volume."
  else
    echo "${owner}: expected ${expected_tables} tables but found ${count}; reset or reconcile explicitly." >&2
    exit 1
  fi
}

run_owner SALUDABLEMENTE_OWNER 4 10-saludablemente-owner.sql.template
run_owner SLB_PERSONAL 3 20-personal.sql.template
run_owner SLB_APTITUDFISICA 3 30-aptitud.sql.template
run_owner SLB_NUTRICIONAL 3 40-nutricional.sql.template
run_owner SALUD_PERSONAL 3 50-salud-personal.sql.template
run_owner SALUD_ALERTA_CLINICA 4 60-alertas.sql.template
run_owner SALUD_AUDITORIA 8 70-auditoria.sql.template
run_owner SALUD_EXPORTACION 4 80-exportacion.sql.template
run_owner SALUD_PERFIL_REPORTE 3 90-perfil-reporte.sql.template
run_owner SALUD_RECOMENDACIONES_IA 2 91-recomendaciones.sql.template
