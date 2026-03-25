@echo off
setlocal
set "SCRIPT_DIR=%~dp0"
set "POWERSHELL_EXE=%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe"

if /I not "%HOSPITAL_STOP_HIDDEN%"=="1" (
    "%POWERSHELL_EXE%" -NoLogo -NoProfile -WindowStyle Hidden -ExecutionPolicy Bypass -Command "$env:HOSPITAL_STOP_HIDDEN='1'; Start-Process -WindowStyle Hidden -FilePath $env:ComSpec -WorkingDirectory '%SCRIPT_DIR%' -ArgumentList '/c','\"\"%~f0\"\"'"
    exit /b
)

"%POWERSHELL_EXE%" -NoLogo -NoProfile -ExecutionPolicy Bypass -File "%SCRIPT_DIR%stop-hospital-system.ps1"
