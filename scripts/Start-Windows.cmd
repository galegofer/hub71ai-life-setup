@echo off
setlocal
where node.exe >nul 2>nul
if errorlevel 1 (
  echo ERROR: Install Node.js 22.12 or newer and reopen your terminal.
  exit /b 1
)
node "%~dp0start-windows.mjs" %*
exit /b %errorlevel%
