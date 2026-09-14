# Verifies static local Oracle harness assets without starting Docker or Oracle.
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$required = @('compose-dev.yml', '.env.example', 'src/main/resources/application-local-oracle.yml', 'database/oracle/local/README.md', 'database/oracle/local/CANONICAL_JPA_SCHEMA_INVENTORY.md', 'database/oracle/local/init/00-create-schema-users.sh', 'database/oracle/local/init/01-create-schema-objects.sh', 'database/oracle/local/init/99-grant-runtime-access.sh')
foreach ($relative in $required) { if (-not (Test-Path (Join-Path $root $relative))) { throw "Missing required asset: $relative" } }
$compose = Get-Content (Join-Path $root 'compose-dev.yml') -Raw
if ($compose -notmatch 'gvenzl/oracle-free:23\.5-slim-faststart') { throw 'Compose must pin the reviewed Oracle image.' }
if ($compose -match 'ORACLE_PASSWORD:\s*\d') { throw 'Compose contains a literal Oracle password.' }
if ($compose -notmatch 'container-entrypoint-initdb\.d') { throw 'Compose must mount the local init scripts.' }
$profile = Get-Content (Join-Path $root 'src/main/resources/application-local-oracle.yml') -Raw
if ($profile -notmatch 'ddl-auto: validate') { throw 'Local Oracle profile must validate only.' }
$requiredTables = @('ACTIVIDADES','INSCRIPCIONES','METAS','TEAMS','PERSONA','PREFERENCIA_COMUNICACION','CREDENCIAL_PROGRAMA','CATALOGO_PRUEBA','EVALUACION_APTITUD','DETALLE_PRUEBA_FISICA','EVALUACION_NUTRICIONAL','DETALLE_ANTROPOMETRICO','DETALLE_BIOQUIMICO','ASISTENCIAS','NOTICIAS','NOTIFICACIONES','ALERTA_CLINICA','ALERTA_CLINICA_DETALLE','CATALOGO_ACCION_CORRECTIVA','RESOLUCION_CLINICA','BITACORA_SEGURIDAD','BITACORA_LECTURA','BITACORA_TRANSACCIONAL','EVENTO_AUDITORIA_DLQ','MANIFIESTO_ARCHIVADO_FRIO','PARTICION_BITACORA','POLITICA_RETENCION','SESION_USUARIO','TAREA_EXPORTACION','SUSCRIPTOR_WEBHOOK','LOG_INTEROPERABILIDAD_FHIR','BITACORA_DESCARGA_EXPORTACION','REPORTE_PERSONAL','COLA_GENERACION_REPORTES','REPORTE_DESCARGA_BITACORA','RECOMENDACION_IA','RECOMENDACION_IA_DETALLE')
$ddl = (Get-ChildItem (Join-Path $root 'database/oracle/local/init') -Filter '*.sql.template' | Get-Content -Raw) -join "`n"
foreach ($table in $requiredTables) { if ($ddl -notmatch "CREATE TABLE $table") { throw "DDL omits mapped table: $table" } }
Write-Output 'Local Oracle asset checks passed.'


