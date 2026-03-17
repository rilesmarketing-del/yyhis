@echo off
setlocal
set "SCRIPT_DIR=%~dp0"
C:\Windows\System32\WindowsPowerShell\v1.0\powershell.exe -NoLogo -ExecutionPolicy Bypass -File "%SCRIPT_DIR%launch-hospital-system.ps1"
