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
$scriptPath = Join-Path $projectRoot "scripts\create-launcher-shortcut.ps1"
$cmdPath = (Get-ChildItem -Path $projectRoot -Filter "*.cmd" -File | Select-Object -First 1).FullName
$vbsPath = (Get-ChildItem -Path $projectRoot -Filter "*.vbs" -File | Select-Object -First 1).FullName
$tempRoot = Join-Path ([System.IO.Path]::GetTempPath()) ("hospital-launcher-shortcut-test-" + [System.Guid]::NewGuid().ToString("N"))
$tempIcon = Join-Path $tempRoot "hospital-launcher.ico"
$tempShortcut = Join-Path $tempRoot "智慧医院系统.lnk"

Assert-True ($null -ne $cmdPath) "Expected a launcher cmd entry in the project root"
Assert-True (Test-Path $cmdPath) "Expected launcher cmd entry at $cmdPath"
Assert-True ($null -ne $vbsPath) "Expected a launcher vbs entry in the project root"
Assert-True (Test-Path $vbsPath) "Expected launcher vbs entry at $vbsPath"
Assert-True (Test-Path $scriptPath) "Expected shortcut creation script at $scriptPath"

New-Item -ItemType Directory -Path $tempRoot -Force | Out-Null

. $scriptPath

Assert-True ($null -ne (Get-Command Get-HospitalLauncherPaths -ErrorAction SilentlyContinue)) "Expected Get-HospitalLauncherPaths helper"
Assert-True ($null -ne (Get-Command New-HospitalLauncherIcon -ErrorAction SilentlyContinue)) "Expected New-HospitalLauncherIcon helper"
Assert-True ($null -ne (Get-Command New-HospitalLauncherShortcut -ErrorAction SilentlyContinue)) "Expected New-HospitalLauncherShortcut helper"

$paths = Get-HospitalLauncherPaths

Assert-True ($null -ne $paths.VbsPath) "Expected launcher path discovery to expose the VBS entry"
Assert-True ((Resolve-Path $paths.VbsPath).Path -eq (Resolve-Path $vbsPath).Path) "Expected launcher path discovery to point to the VBS entry"

New-HospitalLauncherIcon -OutputPath $tempIcon | Out-Null

Assert-True (Test-Path $tempIcon) "Expected icon file to be created"
Assert-True ((Get-Item $tempIcon).Length -gt 0) "Expected icon file to be non-empty"

New-HospitalLauncherShortcut -ShortcutPath $tempShortcut -TargetPath $vbsPath -IconPath $tempIcon -WorkingDirectory $projectRoot | Out-Null

Assert-True (Test-Path $tempShortcut) "Expected launcher shortcut to be created"

$shell = New-Object -ComObject WScript.Shell
$shortcut = $shell.CreateShortcut($tempShortcut)

Assert-True ((Resolve-Path $shortcut.TargetPath).Path -eq (Resolve-Path $vbsPath).Path) "Expected shortcut target to point to the hidden VBS launcher entry"
Assert-True ($shortcut.WorkingDirectory -eq $projectRoot) "Expected shortcut working directory to be the project root"
Assert-True ($shortcut.IconLocation.StartsWith($tempIcon)) "Expected shortcut icon location to point to the generated ico"

Install-HospitalLauncherShortcut | Out-Null

$installedShortcut = $shell.CreateShortcut($paths.ShortcutPath)

Assert-True ((Resolve-Path $installedShortcut.TargetPath).Path -eq (Resolve-Path $vbsPath).Path) "Expected installed launcher shortcut to point to the hidden VBS launcher entry"

Remove-Item -Path $tempRoot -Recurse -Force

Write-Host "Launcher shortcut regression checks passed."
