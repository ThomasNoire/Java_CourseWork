@echo off
echo =====================================================
echo Running complete system test for EMS Backend
echo =====================================================

powershell -ExecutionPolicy Bypass -File "%~dp0test-api-complete.ps1"

pause
