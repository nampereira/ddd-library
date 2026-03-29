@echo off
set PUML_DIR=docs\images

where plantuml >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo Error: plantuml not found. Install with: winget install plantuml >&2
    exit /b 1
)

echo Generating diagrams from %PUML_DIR%...
plantuml %PUML_DIR%\*.puml
echo Done. Images written to %PUML_DIR%\
