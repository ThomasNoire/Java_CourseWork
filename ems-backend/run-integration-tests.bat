@echo off
echo =====================================================
echo Running Integration Tests for EMS Backend
echo =====================================================

cd /d "%~dp0"

echo.
echo Building project...
call mvn clean package -DskipTests

echo.
echo Starting Docker containers...
docker-compose up -d

echo.
echo Waiting for system startup (30 seconds)...
timeout /t 30 /nobreak > nul

echo.
echo Running all integration tests...
call mvn test -Dtest=IntegrationTests.*

echo.
echo Tests completed. Stopping Docker containers...
docker-compose down

echo.
echo =====================================================
echo Integration Testing completed!
echo =====================================================
pause
