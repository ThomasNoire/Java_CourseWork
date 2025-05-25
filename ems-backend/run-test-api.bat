@echo off
echo ===================================================
echo Запуск повного автоматичного тестування системи
echo ===================================================

powershell -ExecutionPolicy Bypass -File "%~dp0test-api.ps1"

pause
