$ErrorActionPreference = "Stop"

function Get-HospitalProjectRoot {
    return $PSScriptRoot
}

function Get-HospitalSystemUrl {
    return "http://127.0.0.1:5173"
}

function Get-FrontendLogPaths {
    $projectRoot = Get-HospitalProjectRoot

    return [pscustomobject]@{
        StandardOutput = Join-Path $projectRoot "frontend\launch-frontend.log"
        StandardError  = Join-Path $projectRoot "frontend\launch-frontend.err.log"
    }
}

function Get-BackendLogPaths {
    $projectRoot = Get-HospitalProjectRoot

    return [pscustomobject]@{
        StandardOutput = Join-Path $projectRoot "backend\launch-backend.log"
        StandardError  = Join-Path $projectRoot "backend\launch-backend.err.log"
    }
}

function Test-CommandAvailable {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Name
    )

    return $null -ne (Get-Command $Name -ErrorAction SilentlyContinue)
}

function Test-HospitalPortListening {
    param(
        [Parameter(Mandatory = $true)]
        [int]$Port,
        [hashtable]$PortStateOverride
    )

    if ($null -ne $PortStateOverride -and $PortStateOverride.ContainsKey($Port)) {
        return [bool]$PortStateOverride[$Port]
    }

    $listener = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue
    return $null -ne $listener
}

function Get-HospitalLaunchPlan {
    param(
        [hashtable]$PortStateOverride
    )

    $frontendRunning = Test-HospitalPortListening -Port 5173 -PortStateOverride $PortStateOverride
    $backendRunning = Test-HospitalPortListening -Port 8080 -PortStateOverride $PortStateOverride

    return [pscustomobject]@{
        FrontendRunning     = $frontendRunning
        BackendRunning      = $backendRunning
        ShouldStartFrontend = -not $frontendRunning
        ShouldStartBackend  = -not $backendRunning
    }
}

function Clear-HospitalLogFiles {
    param(
        [Parameter(Mandatory = $true)]
        [pscustomobject]$LogPaths
    )

    foreach ($path in @($LogPaths.StandardOutput, $LogPaths.StandardError)) {
        if (Test-Path $path) {
            Remove-Item -Path $path -Force
        }
    }
}

function Start-HiddenPowerShellCommand {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Command
    )

    $powershellExe = Join-Path $env:WINDIR "System32\WindowsPowerShell\v1.0\powershell.exe"
    Start-Process -FilePath $powershellExe -WindowStyle Hidden -ArgumentList @(
        "-NoLogo",
        "-ExecutionPolicy", "Bypass",
        "-Command", $Command
    ) | Out-Null
}

function Start-HospitalBackend {
    if (-not (Test-CommandAvailable -Name "mvn")) {
        throw "mvn was not found in PATH. Please install Maven first."
    }

    $projectRoot = Get-HospitalProjectRoot
    $backendDir = Join-Path $projectRoot "backend"
    $logs = Get-BackendLogPaths

    Clear-HospitalLogFiles -LogPaths $logs

    $command = "Set-Location '$backendDir'; mvn spring-boot:run 1>> '$($logs.StandardOutput)' 2>> '$($logs.StandardError)'"
    Start-HiddenPowerShellCommand -Command $command
}

function Start-HospitalFrontend {
    if (-not (Test-CommandAvailable -Name "npm")) {
        throw "npm was not found in PATH. Please install Node.js first."
    }

    $projectRoot = Get-HospitalProjectRoot
    $frontendDir = Join-Path $projectRoot "frontend"
    $logs = Get-FrontendLogPaths

    Clear-HospitalLogFiles -LogPaths $logs

    $command = "Set-Location '$frontendDir'; npm run dev -- --host 127.0.0.1 --port 5173 1>> '$($logs.StandardOutput)' 2>> '$($logs.StandardError)'"
    Start-HiddenPowerShellCommand -Command $command
}

function Wait-HospitalUrlReady {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Url,
        [int]$TimeoutSeconds = 90,
        [int]$IntervalMilliseconds = 1000
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)

    while ((Get-Date) -lt $deadline) {
        try {
            $response = Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec 5
            if ($response.StatusCode -ge 200 -and $response.StatusCode -lt 500) {
                return $true
            }
        } catch {
        }

        Start-Sleep -Milliseconds $IntervalMilliseconds
    }

    return $false
}

function Open-HospitalSystem {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Url
    )

    Start-Process $Url | Out-Null
}

function Start-HospitalSystem {
    param(
        [hashtable]$PortStateOverride,
        [switch]$SkipBrowser,
        [switch]$SkipHealthCheck
    )

    $launchPlan = Get-HospitalLaunchPlan -PortStateOverride $PortStateOverride

    if ($launchPlan.ShouldStartBackend) {
        Start-HospitalBackend
    }

    if ($launchPlan.ShouldStartFrontend) {
        Start-HospitalFrontend
    }

    $systemUrl = Get-HospitalSystemUrl

    if (-not $SkipHealthCheck) {
        if (-not (Wait-HospitalUrlReady -Url $systemUrl)) {
            throw "Frontend startup timed out. Check frontend\\launch-frontend.log and frontend\\launch-frontend.err.log."
        }
    }

    if (-not $SkipBrowser) {
        Open-HospitalSystem -Url $systemUrl
    }

    return $launchPlan
}

if ($MyInvocation.InvocationName -ne ".") {
    Start-HospitalSystem | Out-Null
}
