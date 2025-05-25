@echo off
echo ===================================================
echo Checking server status with simple test
echo ===================================================

echo Running simple server test...
mvnw.cmd test -Dtest=IntegrationTests.SimpleServerTest

pause
