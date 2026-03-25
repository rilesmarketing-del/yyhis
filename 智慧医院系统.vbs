Option Explicit

Dim shell
Dim fileSystem
Dim scriptDir
Dim launcherPath
Dim powershellPath
Dim command

Set shell = CreateObject("WScript.Shell")
Set fileSystem = CreateObject("Scripting.FileSystemObject")

scriptDir = fileSystem.GetParentFolderName(WScript.ScriptFullName)
launcherPath = fileSystem.BuildPath(scriptDir, "launch-hospital-system.ps1")
powershellPath = shell.ExpandEnvironmentStrings("%SystemRoot%\System32\WindowsPowerShell\v1.0\powershell.exe")
command = """" & powershellPath & """ -NoLogo -NoProfile -ExecutionPolicy Bypass -File """ & launcherPath & """"

shell.Run command, 0, False
