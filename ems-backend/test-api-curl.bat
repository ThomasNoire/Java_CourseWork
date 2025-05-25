@echo off
echo =====================================================
echo Testing API endpoints with curl
echo =====================================================

echo Testing login endpoint...
curl -X POST http://localhost:8081/login -H "Content-Type: application/json" -d "{\"email\":\"admin@example.com\",\"password\":\"admin123\"}"
echo.
echo.

echo Testing get employees endpoint...
FOR /F "tokens=*" %%g IN ('curl -X POST http://localhost:8081/login -H "Content-Type: application/json" -d "{\"email\":\"admin@example.com\",\"password\":\"admin123\"}" ^| jq -r ".token"') do (SET TOKEN=%%g)
echo Token: %TOKEN%
echo.

curl -X GET http://localhost:8081/api/employees -H "Authorization: Bearer %TOKEN%" -H "Content-Type: application/json"
echo.
echo.

echo Testing create employee endpoint...
curl -X POST http://localhost:8081/api/employees -H "Authorization: Bearer %TOKEN%" -H "Content-Type: application/json" -d "{\"firstName\":\"Test\",\"lastName\":\"Employee\",\"email\":\"test.employee@example.com\"}"
echo.
echo.

echo Testing complete!
pause
