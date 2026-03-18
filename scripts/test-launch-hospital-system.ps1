$ErrorActionPreference = "Stop"

function Assert-True {
    param(
        [bool]$Condition,
        [string]$Message
    )

    if (-not $Condition) {
        throw $Message
    }
}

$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$launcherPath = Join-Path $projectRoot "launch-hospital-system.ps1"

Assert-True (Test-Path $launcherPath) "Expected launcher script at $launcherPath"

. $launcherPath

Assert-True ($null -ne (Get-Command Get-HospitalSystemUrl -ErrorAction SilentlyContinue)) "Expected Get-HospitalSystemUrl helper"
Assert-True ($null -ne (Get-Command Get-FrontendLogPaths -ErrorAction SilentlyContinue)) "Expected Get-FrontendLogPaths helper"
Assert-True ($null -ne (Get-Command Get-BackendLogPaths -ErrorAction SilentlyContinue)) "Expected Get-BackendLogPaths helper"
Assert-True ($null -ne (Get-Command Get-HospitalFrontendLaunchCommand -ErrorAction SilentlyContinue)) "Expected Get-HospitalFrontendLaunchCommand helper"
Assert-True ($null -ne (Get-Command Test-HospitalReadyStatusCode -ErrorAction SilentlyContinue)) "Expected Test-HospitalReadyStatusCode helper"
Assert-True ((Get-Command Start-HospitalSystem).Parameters.ContainsKey("SkipHealthCheck")) "Expected Start-HospitalSystem to expose SkipHealthCheck"

$systemUrl = Get-HospitalSystemUrl
Assert-True ($systemUrl -eq "http://localhost:5173") "Expected frontend url to be http://localhost:5173"

$frontendLogs = Get-FrontendLogPaths
$backendLogs = Get-BackendLogPaths
$allRunningPlan = Get-HospitalLaunchPlan -PortStateOverride @{
    5173 = $true
    8080 = $true
}
$frontendMissingPlan = Get-HospitalLaunchPlan -PortStateOverride @{
    5173 = $false
    8080 = $true
}
$dryRunPlan = Start-HospitalSystem -PortStateOverride @{
    5173 = $true
    8080 = $true
} -SkipBrowser -SkipHealthCheck
$entryFile = Get-ChildItem -Path $projectRoot -Filter *.cmd -File | Where-Object {
    (Get-Content -Path $_.FullName -Raw).Contains("launch-hospital-system.ps1")
} | Select-Object -First 1

Assert-True ($frontendLogs.StandardOutput.EndsWith("frontend\launch-frontend.log")) "Expected frontend stdout log path"
Assert-True ($frontendLogs.StandardError.EndsWith("frontend\launch-frontend.err.log")) "Expected frontend stderr log path"
Assert-True ($backendLogs.StandardOutput.EndsWith("backend\launch-backend.log")) "Expected backend stdout log path"
Assert-True ($backendLogs.StandardError.EndsWith("backend\launch-backend.err.log")) "Expected backend stderr log path"
Assert-True (-not $allRunningPlan.ShouldStartFrontend) "Expected frontend to stay untouched when already running"
Assert-True (-not $allRunningPlan.ShouldStartBackend) "Expected backend to stay untouched when already running"
Assert-True ($frontendMissingPlan.ShouldStartFrontend) "Expected frontend restart flag when frontend is missing"
Assert-True (-not $frontendMissingPlan.ShouldStartBackend) "Expected backend to stay untouched when backend is already running"
Assert-True (-not $dryRunPlan.ShouldStartFrontend) "Expected dry run to preserve frontend running state"
Assert-True (-not $dryRunPlan.ShouldStartBackend) "Expected dry run to preserve backend running state"
Assert-True ($null -ne $entryFile) "Expected a double-click entry file that calls launch-hospital-system.ps1"
Assert-True (Test-HospitalReadyStatusCode -StatusCode 404) "Expected Test-HospitalReadyStatusCode to treat a 404 frontend response as ready"
Assert-True (-not (Test-HospitalReadyStatusCode -StatusCode 500)) "Expected Test-HospitalReadyStatusCode to reject 500 responses"

$entryContent = Get-Content -Path $entryFile.FullName -Raw
Assert-True ($entryContent.Contains("launch-hospital-system.ps1")) "Expected entry file to call launch-hospital-system.ps1"
Assert-True ($entryContent.Contains("ExecutionPolicy Bypass")) "Expected entry file to bypass execution policy for launch"
Assert-True ($entryContent.Contains("HOSPITAL_LAUNCHER_HIDDEN")) "Expected entry file to mark hidden relaunch state"
Assert-True ($entryContent.Contains("Start-Process")) "Expected entry file to use Start-Process for hidden relaunch"
Assert-True ($entryContent.Contains("-WindowStyle Hidden")) "Expected entry file to relaunch hidden"

$frontendLaunchCommand = Get-HospitalFrontendLaunchCommand
Assert-True ($frontendLaunchCommand.Contains("npm run dev")) "Expected frontend launch command to use the default Vite dev entry"
Assert-True (-not $frontendLaunchCommand.Contains("127.0.0.1 5173")) "Expected frontend launch command to avoid positional host/port arguments"
Assert-True (-not $frontendLaunchCommand.Contains("--host=")) "Expected frontend launch command to avoid the incompatible --host flag form"

Write-Host "Launcher regression checks passed."
