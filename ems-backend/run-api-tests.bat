@echo off
echo =====================================================
echo Running Full API Tests for EMS Backend
echo =====================================================

powershell -ExecutionPolicy Bypass -File "%~dp0test-api-simple.ps1"

REM Open the test report
start "" "%~dp0api-test-report.md"

echo.
echo Tests completed and report opened.
echo To stop Docker containers, run: docker-compose down
echo =====================================================

pause
