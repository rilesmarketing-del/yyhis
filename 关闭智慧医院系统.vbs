Option Explicit

Dim shell
Dim fileSystem
Dim scriptDir
Dim stopperPath
Dim powershellPath
Dim command

Set shell = CreateObject("WScript.Shell")
Set fileSystem = CreateObject("Scripting.FileSystemObject")

scriptDir = fileSystem.GetParentFolderName(WScript.ScriptFullName)
stopperPath = fileSystem.BuildPath(scriptDir, "stop-hospital-system.ps1")
powershellPath = shell.ExpandEnvironmentStrings("%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe")
command = """" & powershellPath & """ -NoLogo -NoProfile -ExecutionPolicy Bypass -File """ & stopperPath & """"

shell.Run command, 0, False
