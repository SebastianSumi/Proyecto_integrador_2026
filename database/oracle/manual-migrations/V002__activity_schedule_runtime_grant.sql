-- Manual follow-up only. Execute after V002__activity_schedule_coordination.sql succeeds.
-- Do not mount this script in the local container initialization directory.
--
-- The local runtime account defaults to SALUDABLEMENTE_APP. If DB_USERNAME is changed,
-- replace that identifier below with the reviewed runtime account before executing.
-- Run as SALUDABLEMENTE_OWNER or a DBA with authority to grant this object privilege.

GRANT EXECUTE ON SALUDABLEMENTE_OWNER.LOCK_AGENDA_ACTIVIDAD TO SALUDABLEMENTE_APP;

-- Verification:
SELECT grantee, privilege, table_name
FROM all_tab_privs
WHERE owner = 'SALUDABLEMENTE_OWNER'
  AND table_name = 'LOCK_AGENDA_ACTIVIDAD'
  AND grantee = 'SALUDABLEMENTE_APP';
