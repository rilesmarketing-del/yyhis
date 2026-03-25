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
$stopScriptPath = Join-Path $projectRoot "stop-hospital-system.ps1"
$cmdEntry = Get-ChildItem -Path $projectRoot -Filter "*.cmd" -File | Where-Object {
    (Get-Content -Path $_.FullName -Raw).Contains("stop-hospital-system.ps1")
} | Select-Object -First 1
$vbsEntry = Get-ChildItem -Path $projectRoot -Filter "*.vbs" -File | Where-Object {
    (Get-Content -Path $_.FullName -Raw).Contains("stop-hospital-system.ps1")
} | Select-Object -First 1

Assert-True (Test-Path $stopScriptPath) "Expected stop script at $stopScriptPath"
Assert-True ($null -ne $cmdEntry) "Expected stop cmd entry in $projectRoot"
Assert-True ($null -ne $vbsEntry) "Expected stop vbs entry in $projectRoot"

. $stopScriptPath

Assert-True ($null -ne (Get-Command Get-HospitalManagedPorts -ErrorAction SilentlyContinue)) "Expected Get-HospitalManagedPorts helper"
Assert-True ($null -ne (Get-Command Get-HospitalPortProcessId -ErrorAction SilentlyContinue)) "Expected Get-HospitalPortProcessId helper"
Assert-True ($null -ne (Get-Command Get-HospitalStopPlan -ErrorAction SilentlyContinue)) "Expected Get-HospitalStopPlan helper"
Assert-True ($null -ne (Get-Command Stop-HospitalProcessByPort -ErrorAction SilentlyContinue)) "Expected Stop-HospitalProcessByPort helper"
Assert-True ($null -ne (Get-Command Stop-HospitalSystem -ErrorAction SilentlyContinue)) "Expected Stop-HospitalSystem helper"

$managedPorts = Get-HospitalManagedPorts
Assert-True ($managedPorts.Frontend -eq 5173) "Expected managed frontend port to be 5173"
Assert-True ($managedPorts.Backend -eq 8080) "Expected managed backend port to be 8080"

$plan = Get-HospitalStopPlan -PortProcessOverride @{
    5173 = 43210
    8080 = $null
}

Assert-True ($plan.ShouldStopFrontend) "Expected frontend stop plan when a frontend process id is provided"
Assert-True (-not $plan.ShouldStopBackend) "Expected backend stop plan to stay false when no process id is provided"
Assert-True ($plan.FrontendProcessId -eq 43210) "Expected frontend process id to be preserved in stop plan"

$dryRunPlan = Stop-HospitalSystem -PortProcessOverride @{
    5173 = 12345
    8080 = 67890
} -SkipKill -SkipWait

Assert-True ($dryRunPlan.ShouldStopFrontend) "Expected dry stop to mark frontend for shutdown"
Assert-True ($dryRunPlan.ShouldStopBackend) "Expected dry stop to mark backend for shutdown"

$cmdEntryContent = Get-Content -Path $cmdEntry.FullName -Raw
Assert-True ($cmdEntryContent.Contains("stop-hospital-system.ps1")) "Expected cmd entry to call stop-hospital-system.ps1"
Assert-True ($cmdEntryContent.Contains("HOSPITAL_STOP_HIDDEN")) "Expected cmd entry to use hidden relaunch marker"
Assert-True ($cmdEntryContent.Contains("Start-Process")) "Expected cmd entry to use Start-Process for hidden relaunch"

$vbsEntryContent = Get-Content -Path $vbsEntry.FullName -Raw
Assert-True ($vbsEntryContent.Contains("stop-hospital-system.ps1")) "Expected vbs entry to call stop-hospital-system.ps1"
Assert-True ($vbsEntryContent.Contains("shell.Run command, 0, False")) "Expected vbs entry to run hidden without waiting"

Write-Host "Stop launcher regression checks passed."
