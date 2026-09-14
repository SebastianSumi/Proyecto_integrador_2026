#!/usr/bin/env bash
set -euo pipefail

# Spring Modulith's JPA starter scans DefaultJpaEventPublication unconditionally.
# The application account owns this table because the entity has no explicit schema.
table_exists=$(sqlplus -s "${APP_USER}/${APP_USER_PASSWORD}@//localhost:1521/FREEPDB1" <<'SQL'
WHENEVER SQLERROR EXIT SQL.SQLCODE
set heading off feedback off pages 0
select count(*) from user_tables where table_name = 'EVENT_PUBLICATION';
exit
SQL
)
table_exists=$(echo "$table_exists" | tr -d '[:space:]')

if [[ "$table_exists" == "0" ]]; then
  sqlplus -s "${APP_USER}/${APP_USER_PASSWORD}@//localhost:1521/FREEPDB1" \
    @/container-entrypoint-initdb.d/02-event-publication.sql.template
elif [[ "$table_exists" == "1" ]]; then
  echo "${APP_USER}.EVENT_PUBLICATION already exists; preserving volume."
else
  echo "${APP_USER}: unexpected EVENT_PUBLICATION lookup result: ${table_exists}." >&2
  exit 1
fi
