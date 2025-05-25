@echo off
echo =====================================================
echo Running simplified API tests for EMS Backend
echo =====================================================

powershell -ExecutionPolicy Bypass -File "%~dp0test-api-simple.ps1"

pause
