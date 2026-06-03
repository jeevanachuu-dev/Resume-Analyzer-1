$projectDir = Join-Path $PSScriptRoot "Resume Analyzer"
$projectRunScript = Join-Path $projectDir "run.ps1"

if (-not (Test-Path $projectRunScript)) {
    Write-Error "Cannot find project run script at: $projectRunScript"
    exit 1
}

Push-Location $projectDir
try {
    & $projectRunScript @args
} finally {
    Pop-Location
}
