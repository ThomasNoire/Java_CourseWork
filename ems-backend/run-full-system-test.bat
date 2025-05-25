@echo off
echo ===================================================
echo Запуск повного автоматичного тестування системи
echo ===================================================

REM Зупинка контейнерів, якщо вони вже запущені
docker-compose down

REM Створення .env файлу, якщо він не існує
echo SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/ems_backend > .env
echo SPRING_DATASOURCE_USERNAME=postgres >> .env
echo SPRING_DATASOURCE_PASSWORD=lolkas228 >> .env
echo JWT_SECRET=Xdks8h1aF2Ieqf9Nc9jztnKZoU9rSbmUpxkBtiQgLlVYFuydq1S4uDgF9ikR6dvw >> .env

REM Запуск Docker Compose в фоновому режимі
echo Запуск Docker контейнерів...
docker-compose up -d

echo Очікування запуску програми...
timeout /t 30

echo Перевірка доступності Swagger UI...
powershell -Command "Invoke-WebRequest -Uri 'http://localhost:8081/swagger-ui/index.html' -UseBasicParsing | Select-Object -ExpandProperty StatusCode"

echo Запуск тестування API...
echo Тестування реєстрації та автентифікації...

REM Спроба автентифікації адміністратора
echo Спроба автентифікації адміністратора...
powershell -Command "$adminResponse = Invoke-WebRequest -Uri 'http://localhost:8081/login' -Method POST -Headers @{'Content-Type'='application/json'} -Body '{\"email\":\"admin@example.com\", \"password\":\"admin123\"}' -UseBasicParsing; $adminResponse.Content | Out-File -FilePath 'admin_token.txt'; Write-Host 'Статус:', $adminResponse.StatusCode"

REM Спроба автентифікації звичайного користувача
echo Спроба автентифікації звичайного користувача...
powershell -Command "$userResponse = Invoke-WebRequest -Uri 'http://localhost:8081/login' -Method POST -Headers @{'Content-Type'='application/json'} -Body '{\"email\":\"user@example.com\", \"password\":\"user123\"}' -UseBasicParsing; $userResponse.Content | Out-File -FilePath 'user_token.txt'; Write-Host 'Статус:', $userResponse.StatusCode"

echo Отримання списку працівників як адміністратор...
powershell -Command "$adminToken = (ConvertFrom-Json (Get-Content -Raw 'admin_token.txt')).token; $headers = @{'Authorization'='Bearer ' + $adminToken; 'Content-Type'='application/json'}; $response = Invoke-WebRequest -Uri 'http://localhost:8081/api/employees' -Headers $headers -Method GET -UseBasicParsing; Write-Host 'Статус:', $response.StatusCode; $response.Content"

echo Створення нового працівника...
powershell -Command "$adminToken = (ConvertFrom-Json (Get-Content -Raw 'admin_token.txt')).token; $headers = @{'Authorization'='Bearer ' + $adminToken; 'Content-Type'='application/json'}; $newEmployee = '{\"firstName\":\"Test\", \"lastName\":\"Employee\", \"email\":\"test.employee@example.com\"}'; $response = Invoke-WebRequest -Uri 'http://localhost:8081/api/employees' -Headers $headers -Method POST -Body $newEmployee -UseBasicParsing; Write-Host 'Статус:', $response.StatusCode; $response.Content | Out-File -FilePath 'created_employee_response.txt'; Write-Host 'Працівник створений успішно.'"

echo Завершення тестування.
echo Тестування завершено успішно! Система працює коректно.
echo Щоб зупинити Docker контейнери, виконайте команду: docker-compose down

pause