@echo off
echo ===================================================
echo Запуск повного автоматичного тестування системи
echo ===================================================

powershell -ExecutionPolicy Bypass -File "%~dp0test-api.ps1"

pause

echo.
echo 2. Testing USER Login
curl -X POST http://localhost:8081/login -H "Content-Type: application/json" -d "{\"email\":\"user@example.com\", \"password\":\"user123\"}" > user_login.json
echo.

echo.
echo 3. Testing ADMIN Login
curl -X POST http://localhost:8081/login -H "Content-Type: application/json" -d "{\"email\":\"booking.bogdan@gmail.com\", \"password\":\"admin\"}" > admin_login.json
echo.

rem Extracting tokens (using findstr because Windows doesn't have jq)
findstr "token" user_login.json > user_token.txt
findstr "token" admin_login.json > admin_token.txt

rem Cleaner output would require PowerShell for JSON parsing

echo.
echo 4. USER role access tests:

echo.
echo 4.1. USER trying to GET employees (should SUCCEED)
curl -X GET http://localhost:8081/api/employees -H "Authorization: Bearer YOUR_USER_TOKEN_HERE"
echo.

echo.
echo 4.2. USER trying to POST new employee (should FAIL with 403 Forbidden)
curl -X POST http://localhost:8081/api/employees -H "Content-Type: application/json" -H "Authorization: Bearer YOUR_USER_TOKEN_HERE" -d "{\"firstName\":\"Test\", \"lastName\":\"User\", \"email\":\"testcreation@example.com\"}"
echo.

echo.
echo 5. ADMIN role access tests:

echo.
echo 5.1. ADMIN trying to GET employees (should SUCCEED)
curl -X GET http://localhost:8081/api/employees -H "Authorization: Bearer YOUR_ADMIN_TOKEN_HERE"
echo.

echo.
echo 5.2. ADMIN trying to POST new employee (should SUCCEED)
curl -X POST http://localhost:8081/api/employees -H "Content-Type: application/json" -H "Authorization: Bearer YOUR_ADMIN_TOKEN_HERE" -d "{\"firstName\":\"Admin\", \"lastName\":\"Created\", \"email\":\"admincreated@example.com\"}" > created_employee.json
echo.

echo.
echo 5.3. ADMIN trying to PUT (update) employee (should SUCCEED)
curl -X PUT http://localhost:8081/api/employees/1 -H "Content-Type: application/json" -H "Authorization: Bearer YOUR_ADMIN_TOKEN_HERE" -d "{\"firstName\":\"Updated\", \"lastName\":\"Employee\", \"email\":\"updated@example.com\"}"
echo.

echo.
echo 5.4. ADMIN trying to DELETE employee (should SUCCEED)
curl -X DELETE http://localhost:8081/api/employees/1 -H "Authorization: Bearer YOUR_ADMIN_TOKEN_HERE"
echo.

echo.
echo 6. Testing unauthenticated access (should FAIL with 401 Unauthorized)
curl -X GET http://localhost:8081/api/employees
echo.

echo.
echo 7. Testing public endpoints (should SUCCEED)
curl -X GET http://localhost:8081/swagger-ui/index.html
echo.

echo.
echo ==============================================
echo NOTE: You need to manually replace YOUR_USER_TOKEN_HERE and YOUR_ADMIN_TOKEN_HERE
echo with the actual tokens from user_token.txt and admin_token.txt files
echo ==============================================

pause 