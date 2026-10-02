$ErrorActionPreference = 'Stop'
& (Join-Path $PSScriptRoot 'Start-Windows.cmd') @args
exit $LASTEXITCODE
