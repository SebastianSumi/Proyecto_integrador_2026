# Preflight de Keycloak: valida que cada JSON de import se llame "<realm>-realm.json".
# Keycloak 26+ ABORTA el arranque si no coincide (antes lo toleraba).
# Uso: powershell -File keycloak\check-import.ps1   (ejecutar antes de `docker compose up`)
$ErrorActionPreference = 'Stop'

$dir = Join-Path $PSScriptRoot 'import'
if (-not (Test-Path -LiteralPath $dir)) {
  Write-Error "No existe el directorio de import: $dir"
  exit 1
}

$files = Get-ChildItem -LiteralPath $dir -Filter '*.json' -File
if ($files.Count -eq 0) {
  Write-Error "Sin archivos *.json en $dir; --import-realm no importará nada."
  exit 1
}

$fallos = 0
foreach ($f in $files) {
  try {
    $json = Get-Content -LiteralPath $f.FullName -Raw | ConvertFrom-Json
  } catch {
    Write-Host "ERROR: $($f.Name) no es JSON válido: $($_.Exception.Message)"
    $fallos++
    continue
  }
  $realm = $json.realm
  if ([string]::IsNullOrWhiteSpace($realm)) {
    Write-Host "ERROR: $($f.Name) no tiene campo .realm"
    $fallos++
    continue
  }
  $esperado = "$realm-realm.json"
  if ($f.Name -cne $esperado) {
    Write-Host "ERROR: $($f.Name) contiene el realm '$realm' pero debe llamarse '$esperado' (Keycloak 26+ no arranca si no coincide)."
    $fallos++
  } else {
    Write-Host "OK: $($f.Name) -> realm '$realm'"
  }
}

if ($fallos -gt 0) {
  Write-Host "Preflight FALLIDO con $fallos error(es). Corrige los nombres antes de levantar Keycloak."
  exit 1
}
Write-Host 'Preflight OK: import de Keycloak válido.'
