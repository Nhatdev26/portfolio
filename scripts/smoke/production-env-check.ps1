param(
    [string]$Path = ".env.production",
    [switch]$Template
)

$ErrorActionPreference = "Stop"

if (-not (Test-Path -LiteralPath $Path)) {
    throw "Environment file was not found: $Path"
}

$requiredKeys = @(
    "POSTGRES_DB",
    "POSTGRES_USER",
    "POSTGRES_PASSWORD",
    "SERVER_PORT",
    "DB_URL",
    "DB_USERNAME",
    "DB_PASSWORD",
    "CORS_ALLOWED_ORIGINS",
    "PORTFOLIO_API_BASE_URL",
    "ADMIN_SEED_ENABLED",
    "ADMIN_SEED_EMAIL",
    "ADMIN_SEED_PASSWORD",
    "AUTH_JWT_ISSUER",
    "AUTH_JWT_SECRET",
    "AUTH_ACCESS_TOKEN_MINUTES",
    "AUTH_REFRESH_TOKEN_DAYS",
    "FILE_STORAGE_PATH"
)

$values = @{}
Get-Content -LiteralPath $Path | ForEach-Object {
    $line = $_.Trim()
    if ($line -eq "" -or $line.StartsWith("#")) {
        return
    }
    $separator = $line.IndexOf("=")
    if ($separator -lt 1) {
        throw "Invalid .env line: $line"
    }
    $key = $line.Substring(0, $separator).Trim()
    $value = $line.Substring($separator + 1).Trim()
    $values[$key] = $value
}

foreach ($key in $requiredKeys) {
    if (-not $values.ContainsKey($key)) {
        throw "Missing required environment key: $key"
    }
    if ([string]::IsNullOrWhiteSpace($values[$key])) {
        throw "Environment key must not be empty: $key"
    }
}

$devOnlyValues = @(
    "portfolio",
    "change-me-in-real-env",
    "dev-only-change-me-please-use-a-real-32-byte-secret",
    "admin@example.com"
)

foreach ($key in $requiredKeys) {
    $value = $values[$key]
    if ($devOnlyValues -contains $value) {
        throw "Environment key '$key' uses an unsafe development value."
    }
    if (-not $Template -and $value.StartsWith("replace-with-")) {
        throw "Environment key '$key' still uses a placeholder value."
    }
}

if ($values["AUTH_JWT_SECRET"].Length -lt 32) {
    throw "AUTH_JWT_SECRET must be at least 32 characters."
}

if (-not $values["CORS_ALLOWED_ORIGINS"].StartsWith("https://")) {
    throw "CORS_ALLOWED_ORIGINS should use an https origin for production."
}

if (-not $values["PORTFOLIO_API_BASE_URL"].StartsWith("https://")) {
    throw "PORTFOLIO_API_BASE_URL should use an https origin for production."
}

if ($values["ADMIN_SEED_ENABLED"] -notin @("true", "false")) {
    throw "ADMIN_SEED_ENABLED must be true or false."
}

Write-Host "Production environment check passed for $Path"
