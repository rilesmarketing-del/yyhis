$ErrorActionPreference = "Stop"

function Get-HospitalProjectRoot {
    return $PSScriptRoot
}

function Get-HospitalSystemUrl {
    return "http://localhost:5173"
}

function Get-HospitalBrowserUrl {
    return "http://localhost:5173/login?launcher=fresh"
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

function Get-HospitalFrontendLaunchCommand {
    $logs = Get-FrontendLogPaths
    $projectRoot = Get-HospitalProjectRoot
    $frontendDir = Join-Path $projectRoot "frontend"
    return "Set-Location '$frontendDir'; npm run dev 1>> '$($logs.StandardOutput)' 2>> '$($logs.StandardError)'"
}

function Test-CommandAvailable {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Name
    )

    return $null -ne (Get-Command $Name -ErrorAction SilentlyContinue)
}

function Test-HospitalReadyStatusCode {
    param(
        [Parameter(Mandatory = $true)]
        [int]$StatusCode
    )

    return $StatusCode -ge 200 -and $StatusCode -lt 500
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

    $logs = Get-FrontendLogPaths

    Clear-HospitalLogFiles -LogPaths $logs

    $command = Get-HospitalFrontendLaunchCommand
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
            if (Test-HospitalReadyStatusCode -StatusCode ([int]$response.StatusCode)) {
                return $true
            }
        } catch [System.Net.WebException] {
            if ($null -ne $_.Exception.Response) {
                $statusCode = [int]$_.Exception.Response.StatusCode
                if (Test-HospitalReadyStatusCode -StatusCode $statusCode) {
                    return $true
                }
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
    $browserUrl = Get-HospitalBrowserUrl

    if (-not $SkipHealthCheck) {
        if (-not (Wait-HospitalUrlReady -Url $systemUrl)) {
            throw "Frontend startup timed out. Check frontend\\launch-frontend.log and frontend\\launch-frontend.err.log."
        }
    }

    if (-not $SkipBrowser) {
        Open-HospitalSystem -Url $browserUrl
    }

    return $launchPlan
}

if ($MyInvocation.InvocationName -ne ".") {
    Start-HospitalSystem | Out-Null
}
