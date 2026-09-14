-- Synthetic demo seed preflight. Read-only: no DDL or DML.
-- Connect as SALUDABLEMENTE_APP before running the numbered scripts.
SELECT owner, table_name
FROM all_tables
WHERE owner IN ('SALUDABLEMENTE_OWNER', 'SLB_PERSONAL', 'SLB_APTITUDFISICA',
                'SLB_NUTRICIONAL', 'SALUD_PERSONAL', 'SALUD_ALERTA_CLINICA',
                'SALUD_EXPORTACION', 'SALUD_PERFIL_REPORTE', 'SALUD_RECOMENDACIONES_IA')
ORDER BY owner, table_name;

SELECT 'SALUDABLEMENTE_APP can run demo scripts' AS preflight_result FROM dual;