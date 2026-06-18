param(
    [switch]$SkipBuild,
    [int]$StartupTimeoutSeconds = 90
)

$ErrorActionPreference = "Stop"

function Write-Step {
    param([string]$Message)
    Write-Host "==> $Message"
}

function Invoke-JsonGet {
    param([string]$Uri)
    return Invoke-RestMethod -Uri $Uri -Method Get -TimeoutSec 10
}

function Invoke-TextGet {
    param([string]$Uri)
    return Invoke-WebRequest -Uri $Uri -UseBasicParsing -TimeoutSec 10
}

function Assert-TextContains {
    param(
        [string]$Text,
        [string]$Expected,
        [string]$Message
    )
    if (-not $Text.Contains($Expected)) {
        throw $Message
    }
}

function Wait-ForHttp {
    param(
        [string]$Uri,
        [int]$TimeoutSeconds
    )
    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    $lastError = $null
    while ((Get-Date) -lt $deadline) {
        try {
            return Invoke-TextGet -Uri $Uri
        } catch {
            $lastError = $_
            Start-Sleep -Seconds 3
        }
    }
    throw "Timed out waiting for $Uri. Last error: $lastError"
}

Write-Step "Starting Docker Compose stack"
if ($SkipBuild) {
    docker compose up -d
} else {
    docker compose up -d --build
}

Write-Step "Waiting for backend health"
$backendHealth = Wait-ForHttp -Uri "http://localhost:8080/api/health" -TimeoutSeconds $StartupTimeoutSeconds
Assert-TextContains -Text $backendHealth.Content -Expected "portfolio-cms-backend" -Message "Backend health did not identify the portfolio backend service."

Write-Step "Checking frontend shell"
$frontendHome = Wait-ForHttp -Uri "http://localhost:5173/" -TimeoutSeconds $StartupTimeoutSeconds
Assert-TextContains -Text $frontendHome.Content -Expected '<div id="root"></div>' -Message "Frontend HTML shell did not include the React root."

Write-Step "Checking frontend-to-backend proxy"
$proxiedHealth = Invoke-JsonGet -Uri "http://localhost:5173/api/health"
if ($proxiedHealth.status -ne "ok" -or $proxiedHealth.service -ne "portfolio-cms-backend") {
    throw "Frontend proxy health check returned an unexpected payload: $($proxiedHealth | ConvertTo-Json -Compress)"
}

Write-Step "Checking compose service status"
$composeJson = docker compose ps --format json | ConvertFrom-Json
$requiredServices = @("db", "backend", "frontend")
foreach ($service in $requiredServices) {
    $entry = $composeJson | Where-Object { $_.Service -eq $service } | Select-Object -First 1
    if ($null -eq $entry) {
        throw "Docker Compose service '$service' was not found."
    }
    if ($entry.State -ne "running") {
        throw "Docker Compose service '$service' is '$($entry.State)', expected 'running'."
    }
}

Write-Step "Docker Compose smoke passed"
