@echo off
setlocal
if "%~1"=="" (
  echo Usage: scripts\jtestlib-source.bat dev^|release
  echo   dev     - local ../../jtestlib ^(editable^)
  echo   release - git source from test/pyproject.toml
  exit /b 1
)
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0jtestlib-source.ps1" %*
exit /b %ERRORLEVEL%
