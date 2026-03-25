$ErrorActionPreference = "Stop"

function Get-HospitalManagedPorts {
    return [pscustomobject]@{
        Frontend = 5173
        Backend  = 8080
    }
}

function Get-HospitalPortProcessId {
    param(
        [Parameter(Mandatory = $true)]
        [int]$Port,
        [hashtable]$PortProcessOverride
    )

    if ($null -ne $PortProcessOverride -and $PortProcessOverride.ContainsKey($Port)) {
        return $PortProcessOverride[$Port]
    }

    $listener = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($null -eq $listener) {
        return $null
    }

    return [int]$listener.OwningProcess
}

function Get-HospitalStopPlan {
    param(
        [hashtable]$PortProcessOverride
    )

    $ports = Get-HospitalManagedPorts
    $frontendProcessId = Get-HospitalPortProcessId -Port $ports.Frontend -PortProcessOverride $PortProcessOverride
    $backendProcessId = Get-HospitalPortProcessId -Port $ports.Backend -PortProcessOverride $PortProcessOverride

    return [pscustomobject]@{
        FrontendPort      = $ports.Frontend
        BackendPort       = $ports.Backend
        FrontendProcessId = $frontendProcessId
        BackendProcessId  = $backendProcessId
        ShouldStopFrontend = $null -ne $frontendProcessId
        ShouldStopBackend  = $null -ne $backendProcessId
    }
}

function Stop-HospitalProcessByPort {
    param(
        [Parameter(Mandatory = $true)]
        [int]$Port,
        [hashtable]$PortProcessOverride,
        [switch]$SkipKill
    )

    $processId = Get-HospitalPortProcessId -Port $Port -PortProcessOverride $PortProcessOverride
    if ($null -eq $processId) {
        return $false
    }

    if (-not $SkipKill) {
        Stop-Process -Id $processId -Force -ErrorAction SilentlyContinue
    }

    return $true
}

function Wait-HospitalPortReleased {
    param(
        [Parameter(Mandatory = $true)]
        [int]$Port,
        [int]$TimeoutSeconds = 20
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)

    while ((Get-Date) -lt $deadline) {
        $listener = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1
        if ($null -eq $listener) {
            return $true
        }

        Start-Sleep -Milliseconds 400
    }

    return $false
}

function Stop-HospitalSystem {
    param(
        [hashtable]$PortProcessOverride,
        [switch]$SkipKill,
        [switch]$SkipWait
    )

    $plan = Get-HospitalStopPlan -PortProcessOverride $PortProcessOverride

    if ($plan.ShouldStopFrontend) {
        Stop-HospitalProcessByPort -Port $plan.FrontendPort -PortProcessOverride $PortProcessOverride -SkipKill:$SkipKill | Out-Null
    }

    if ($plan.ShouldStopBackend) {
        Stop-HospitalProcessByPort -Port $plan.BackendPort -PortProcessOverride $PortProcessOverride -SkipKill:$SkipKill | Out-Null
    }

    if (-not $SkipKill -and -not $SkipWait) {
        if ($plan.ShouldStopFrontend -and -not (Wait-HospitalPortReleased -Port $plan.FrontendPort)) {
            throw "Frontend shutdown timed out on port $($plan.FrontendPort)."
        }

        if ($plan.ShouldStopBackend -and -not (Wait-HospitalPortReleased -Port $plan.BackendPort)) {
            throw "Backend shutdown timed out on port $($plan.BackendPort)."
        }
    }

    return $plan
}

if ($MyInvocation.InvocationName -ne ".") {
    Stop-HospitalSystem | Out-Null
}
