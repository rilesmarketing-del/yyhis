$ErrorActionPreference = "Stop"

Add-Type -AssemblyName System.Drawing

function Get-HospitalProjectRoot {
    return (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
}

function Get-HospitalLauncherPaths {
    $projectRoot = Get-HospitalProjectRoot
    $cmdFile = Get-ChildItem -Path $projectRoot -Filter "*.cmd" -File | Select-Object -First 1

    if ($null -eq $cmdFile) {
        throw "Launcher cmd entry was not found in $projectRoot."
    }

    $iconDir = Join-Path $projectRoot "assets\icons"

    return [pscustomobject]@{
        ProjectRoot   = $projectRoot
        CmdPath       = $cmdFile.FullName
        IconSvgPath   = Join-Path $iconDir "hospital-launcher.svg"
        IconIcoPath   = Join-Path $iconDir "hospital-launcher.ico"
        ShortcutPath  = Join-Path $projectRoot ($cmdFile.BaseName + ".lnk")
    }
}

function New-HospitalRoundedRectPath {
    param(
        [Parameter(Mandatory = $true)]
        [float]$X,
        [Parameter(Mandatory = $true)]
        [float]$Y,
        [Parameter(Mandatory = $true)]
        [float]$Width,
        [Parameter(Mandatory = $true)]
        [float]$Height,
        [Parameter(Mandatory = $true)]
        [float]$Radius
    )

    $path = New-Object System.Drawing.Drawing2D.GraphicsPath
    $diameter = $Radius * 2

    $path.AddArc($X, $Y, $diameter, $diameter, 180, 90)
    $path.AddArc($X + $Width - $diameter, $Y, $diameter, $diameter, 270, 90)
    $path.AddArc($X + $Width - $diameter, $Y + $Height - $diameter, $diameter, $diameter, 0, 90)
    $path.AddArc($X, $Y + $Height - $diameter, $diameter, $diameter, 90, 90)
    $path.CloseFigure()

    return $path
}

function New-HospitalLauncherIcon {
    param(
        [Parameter(Mandatory = $true)]
        [string]$OutputPath
    )

    $outputDirectory = Split-Path -Path $OutputPath -Parent
    if (-not (Test-Path $outputDirectory)) {
        New-Item -ItemType Directory -Path $outputDirectory -Force | Out-Null
    }

    $size = 256
    $bitmap = New-Object System.Drawing.Bitmap $size, $size
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
    $pngStream = New-Object System.IO.MemoryStream
    $iconStream = New-Object System.IO.MemoryStream
    $binaryWriter = New-Object System.IO.BinaryWriter $iconStream

    try {
        $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
        $graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
        $graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
        $graphics.Clear([System.Drawing.Color]::Transparent)

        $backgroundPath = New-HospitalRoundedRectPath -X 18 -Y 18 -Width 220 -Height 220 -Radius 52
        $backgroundBrush = New-Object System.Drawing.Drawing2D.LinearGradientBrush(
            (New-Object System.Drawing.PointF 24, 20),
            (New-Object System.Drawing.PointF 228, 232),
            ([System.Drawing.Color]::FromArgb(245, 252, 250)),
            ([System.Drawing.Color]::FromArgb(255, 243, 219))
        )
        $borderPen = New-Object System.Drawing.Pen ([System.Drawing.Color]::FromArgb(184, 222, 213), 4)

        $graphics.FillPath($backgroundBrush, $backgroundPath)
        $graphics.DrawPath($borderPen, $backgroundPath)

        $glowPath = New-Object System.Drawing.Drawing2D.GraphicsPath
        $glowPath.AddEllipse(152, 40, 68, 68)
        $glowBrush = New-Object System.Drawing.Drawing2D.PathGradientBrush $glowPath
        $glowBrush.CenterColor = [System.Drawing.Color]::FromArgb(220, 255, 215, 119)
        $glowBrush.SurroundColors = @([System.Drawing.Color]::FromArgb(0, 255, 215, 119))
        $graphics.FillEllipse($glowBrush, 152, 40, 68, 68)

        $matrix = New-Object System.Drawing.Drawing2D.Matrix
        $matrix.RotateAt(12, (New-Object System.Drawing.PointF 128, 128))
        $graphics.Transform = $matrix

        $crossBrush = New-Object System.Drawing.Drawing2D.LinearGradientBrush(
            (New-Object System.Drawing.PointF 72, 56),
            (New-Object System.Drawing.PointF 184, 200),
            ([System.Drawing.Color]::FromArgb(21, 143, 127)),
            ([System.Drawing.Color]::FromArgb(15, 111, 127))
        )
        $verticalPath = New-HospitalRoundedRectPath -X 103 -Y 52 -Width 50 -Height 152 -Radius 24
        $horizontalPath = New-HospitalRoundedRectPath -X 52 -Y 103 -Width 152 -Height 50 -Radius 24

        $graphics.FillPath($crossBrush, $verticalPath)
        $graphics.FillPath($crossBrush, $horizontalPath)

        $graphics.ResetTransform()

        $dotBrush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(255, 205, 89))
        $graphics.FillEllipse($dotBrush, 172, 60, 24, 24)

        $bitmap.Save($pngStream, [System.Drawing.Imaging.ImageFormat]::Png)
        $pngBytes = $pngStream.ToArray()

        $binaryWriter.Write([UInt16]0)
        $binaryWriter.Write([UInt16]1)
        $binaryWriter.Write([UInt16]1)
        $binaryWriter.Write([byte]0)
        $binaryWriter.Write([byte]0)
        $binaryWriter.Write([byte]0)
        $binaryWriter.Write([byte]0)
        $binaryWriter.Write([UInt16]1)
        $binaryWriter.Write([UInt16]32)
        $binaryWriter.Write([UInt32]$pngBytes.Length)
        $binaryWriter.Write([UInt32]22)
        $binaryWriter.Write($pngBytes)
        $binaryWriter.Flush()

        [System.IO.File]::WriteAllBytes($OutputPath, $iconStream.ToArray())
    } finally {
        if ($null -ne $graphics) { $graphics.Dispose() }
        if ($null -ne $bitmap) { $bitmap.Dispose() }
        if ($null -ne $pngStream) { $pngStream.Dispose() }
        if ($null -ne $binaryWriter) { $binaryWriter.Dispose() }
        if ($null -ne $iconStream) { $iconStream.Dispose() }
        if ($null -ne $backgroundBrush) { $backgroundBrush.Dispose() }
        if ($null -ne $backgroundPath) { $backgroundPath.Dispose() }
        if ($null -ne $borderPen) { $borderPen.Dispose() }
        if ($null -ne $glowBrush) { $glowBrush.Dispose() }
        if ($null -ne $glowPath) { $glowPath.Dispose() }
        if ($null -ne $matrix) { $matrix.Dispose() }
        if ($null -ne $crossBrush) { $crossBrush.Dispose() }
        if ($null -ne $verticalPath) { $verticalPath.Dispose() }
        if ($null -ne $horizontalPath) { $horizontalPath.Dispose() }
        if ($null -ne $dotBrush) { $dotBrush.Dispose() }
    }

    return $OutputPath
}

function New-HospitalLauncherShortcut {
    param(
        [Parameter(Mandatory = $true)]
        [string]$ShortcutPath,
        [Parameter(Mandatory = $true)]
        [string]$TargetPath,
        [Parameter(Mandatory = $true)]
        [string]$IconPath,
        [Parameter(Mandatory = $true)]
        [string]$WorkingDirectory
    )

    $shell = New-Object -ComObject WScript.Shell
    $shortcut = $shell.CreateShortcut($ShortcutPath)
    $shortcut.TargetPath = (Resolve-Path $TargetPath).Path
    $shortcut.WorkingDirectory = $WorkingDirectory
    $shortcut.IconLocation = "$IconPath,0"
    $shortcut.Description = "启动智慧医院系统"
    $shortcut.Save()

    return $ShortcutPath
}

function Remove-StaleHospitalLauncherShortcuts {
    param(
        [Parameter(Mandatory = $true)]
        [string]$ProjectRoot,
        [Parameter(Mandatory = $true)]
        [string]$CmdPath,
        [Parameter(Mandatory = $true)]
        [string]$DesiredShortcutPath
    )

    $shell = New-Object -ComObject WScript.Shell
    $desiredTarget = (Resolve-Path $CmdPath).Path

    Get-ChildItem -Path $ProjectRoot -Filter "*.lnk" -File | ForEach-Object {
        if ($_.FullName -eq $DesiredShortcutPath) {
            return
        }

        $candidate = $shell.CreateShortcut($_.FullName)
        if ($candidate.TargetPath -and (Resolve-Path $candidate.TargetPath -ErrorAction SilentlyContinue).Path -eq $desiredTarget) {
            Remove-Item -Path $_.FullName -Force
        }
    }
}

function Install-HospitalLauncherShortcut {
    $paths = Get-HospitalLauncherPaths

    if (-not (Test-Path $paths.IconSvgPath)) {
        throw "Expected SVG icon source at $($paths.IconSvgPath)."
    }

    New-HospitalLauncherIcon -OutputPath $paths.IconIcoPath | Out-Null
    Remove-StaleHospitalLauncherShortcuts -ProjectRoot $paths.ProjectRoot -CmdPath $paths.CmdPath -DesiredShortcutPath $paths.ShortcutPath
    New-HospitalLauncherShortcut -ShortcutPath $paths.ShortcutPath -TargetPath $paths.CmdPath -IconPath $paths.IconIcoPath -WorkingDirectory $paths.ProjectRoot | Out-Null

    return $paths
}

if ($MyInvocation.InvocationName -ne ".") {
    $result = Install-HospitalLauncherShortcut
    Write-Host "Launcher shortcut created:"
    Write-Host "  Shortcut: $($result.ShortcutPath)"
    Write-Host "  Icon: $($result.IconIcoPath)"
}
