param([string]$Destination = 'C:\Dev\workspace\life-setup-agent')
$ErrorActionPreference = 'Stop'
$Source = Split-Path $PSScriptRoot -Parent
if ([IO.Path]::GetFullPath($Source).TrimEnd('\') -eq [IO.Path]::GetFullPath($Destination).TrimEnd('\')) {
 Write-Host "Project is already at $Destination"; exit 0
}
if (Test-Path $Destination) { throw "Destination already exists: $Destination. Choose an empty destination; this script will not overwrite it." }
New-Item -ItemType Directory -Path (Split-Path $Destination -Parent) -Force | Out-Null
Copy-Item -LiteralPath $Source -Destination $Destination -Recurse
Write-Host "Project created at $Destination"
Write-Host "Next: cd $Destination"
Write-Host '.\scripts\Start-Windows.cmd'
