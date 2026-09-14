[CmdletBinding()]
param(
    [switch]$Force
)

$repoRoot = Split-Path -Parent $PSScriptRoot
$environmentFile = Join-Path $repoRoot '.env.local'

function Read-RequiredSimplePassword {
    param([string]$Label)

    while ($true) {
        $secureValue = Read-Host -Prompt $Label -AsSecureString
        $plainValue = [System.Net.NetworkCredential]::new('', $secureValue).Password

        if ($plainValue.Length -lt 8 -or $plainValue.Length -gt 64) {
            Write-Host 'Password must contain 8 to 64 characters.' -ForegroundColor Yellow
            continue
        }

        if ($plainValue -notmatch '^[A-Za-z0-9_]+$' -or
            $plainValue -notmatch '[A-Za-z]' -or
            $plainValue -notmatch '\d') {
            Write-Host 'Use only letters, digits, and underscore, including at least one letter and one digit.' -ForegroundColor Yellow
            continue
        }

        return $plainValue
    }
}

function Read-RuntimeUser {
    while ($true) {
        $value = Read-Host -Prompt 'Runtime Oracle username [SALUDABLEMENTE_APP]'
        if ([string]::IsNullOrWhiteSpace($value)) {
            return 'SALUDABLEMENTE_APP'
        }

        $value = $value.Trim().ToUpperInvariant()
        if ($value -match '^[A-Z][A-Z0-9_$#]{0,29}$') {
            return $value
        }

        Write-Host 'Use an unquoted Oracle identifier: 1-30 uppercase letters, digits, _, $, or #; it must start with a letter.' -ForegroundColor Yellow
    }
}

function Read-OraclePort {
    while ($true) {
        $value = Read-Host -Prompt 'Host port [1524]'
        if ([string]::IsNullOrWhiteSpace($value)) {
            return 1524
        }

        $port = 0
        if ([int]::TryParse($value, [ref]$port) -and $port -ge 1024 -and $port -le 65535) {
            return $port
        }

        Write-Host 'Use a TCP port between 1024 and 65535.' -ForegroundColor Yellow
    }
}

if ((Test-Path -LiteralPath $environmentFile) -and -not $Force) {
    $confirmation = Read-Host -Prompt '.env.local already exists. Replace it? [y/N]'
    if ($confirmation -notmatch '^[Yy]$') {
        Write-Host 'No file was changed.'
        exit 0
    }
}

$sysPassword = Read-RequiredSimplePassword 'Oracle SYS password'
$runtimeUser = Read-RuntimeUser
$runtimePassword = Read-RequiredSimplePassword 'Runtime Oracle password'
$ownerPassword = Read-RequiredSimplePassword 'Local schema owner password'
$port = Read-OraclePort

$lines = @(
    '# Local-only Oracle settings. This file is ignored by Git.',
    "ORACLE_PORT=$port",
    "ORACLE_PASSWORD=$sysPassword",
    "DB_USERNAME=$runtimeUser",
    "DB_PASSWORD=$runtimePassword",
    "LOCAL_ORACLE_OWNER_PASSWORD=$ownerPassword",
    "DB_URL=jdbc:oracle:thin:@localhost:$port/FREEPDB1"
)

[System.IO.File]::WriteAllLines(
    $environmentFile,
    $lines,
    [System.Text.UTF8Encoding]::new($false)
)

Write-Host '.env.local was created without printing credentials.' -ForegroundColor Green
Write-Host 'Create a fresh database: docker compose --env-file .env.local -f compose-dev.yml up -d oracle'
Write-Host 'If initialization credentials change later, run docker compose --env-file .env.local -f compose-dev.yml down -v before creating it again.' -ForegroundColor Yellow
