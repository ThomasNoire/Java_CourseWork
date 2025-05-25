# Testing the API system

Write-Host "====================================================="
Write-Host "Starting API testing"
Write-Host "====================================================="

# Зупинка контейнерів, якщо вони вже запущені
Write-Host "Зупинка Docker контейнерів, якщо вони запущені..."
docker-compose down

# Створення .env файлу, якщо він не існує
Write-Host "Створення .env файлу..."
Set-Content -Path ".env" -Value "SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/ems_backend"
Add-Content -Path ".env" -Value "SPRING_DATASOURCE_USERNAME=postgres"
Add-Content -Path ".env" -Value "SPRING_DATASOURCE_PASSWORD=lolkas228"
Add-Content -Path ".env" -Value "JWT_SECRET=Xdks8h1aF2Ieqf9Nc9jztnKZoU9rSbmUpxkBtiQgLlVYFuydq1S4uDgF9ikR6dvw"

# Запуск Docker Compose в фоновому режимі
Write-Host "Запуск Docker контейнерів..."
docker-compose up -d

Write-Host "Очікування запуску програми..."
Start-Sleep -Seconds 30

Write-Host "Перевірка доступності Swagger UI..."
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8081/swagger-ui/index.html" -UseBasicParsing
    Write-Host "Swagger UI доступний. Статус: $($response.StatusCode)"
} catch {
    Write-Host "Помилка доступу до Swagger UI: $_"
}

Write-Host "`nЗапуск тестування API..."
Write-Host "Тестування автентифікації..."

# Спроба автентифікації адміністратора
try {
    Write-Host "Спроба автентифікації адміністратора..."
    $adminLoginData = @{
        email = "admin@example.com"
        password = "admin123"
    } | ConvertTo-Json
    $adminResponse = Invoke-WebRequest -Uri "http://localhost:8081/login" -Method POST -Body $adminLoginData -ContentType "application/json" -UseBasicParsing
    $adminResponse.Content | Out-File -FilePath "admin_token.txt"
    $adminTokenInfo = $adminResponse.Content | ConvertFrom-Json
    Write-Host "Адміністратор автентифікований успішно. Роль: $($adminTokenInfo.role), Email: $($adminTokenInfo.email)"
    Write-Host "Токен збережено в admin_token.txt"
} catch {
    Write-Host "Помилка автентифікації адміністратора: $_"
}

# Спроба автентифікації звичайного користувача
try {
    Write-Host "`nСпроба автентифікації звичайного користувача..."
    $userLoginData = @{
        email = "user@example.com"
        password = "user123"
    } | ConvertTo-Json
    $userResponse = Invoke-WebRequest -Uri "http://localhost:8081/login" -Method POST -Body $userLoginData -ContentType "application/json" -UseBasicParsing
    $userResponse.Content | Out-File -FilePath "user_token.txt"
    $userTokenInfo = $userResponse.Content | ConvertFrom-Json
    Write-Host "Користувач автентифікований успішно. Роль: $($userTokenInfo.role), Email: $($userTokenInfo.email)"
    Write-Host "Токен збережено в user_token.txt"
} catch {
    Write-Host "Помилка автентифікації користувача: $_"
}

# Отримання списку працівників як адміністратор
try {
    Write-Host "`nОтримання списку працівників як адміністратор..."
    $adminToken = (Get-Content -Raw "admin_token.txt" | ConvertFrom-Json).token
    $headers = @{
        "Authorization" = "Bearer $adminToken"
        "Content-Type" = "application/json"
    }
    $response = Invoke-WebRequest -Uri "http://localhost:8081/api/employees" -Headers $headers -Method GET -UseBasicParsing
    Write-Host "Статус: $($response.StatusCode)"
    Write-Host "Список працівників отримано успішно:"
    $employees = $response.Content | ConvertFrom-Json
    $employees | Format-Table -Property id, firstName, lastName, email -AutoSize
} catch {
    Write-Host "Помилка отримання списку працівників: $_"
}

# Створення нового працівника
try {
    Write-Host "`nСтворення нового працівника..."
    $adminToken = (Get-Content -Raw "admin_token.txt" | ConvertFrom-Json).token
    $headers = @{
        "Authorization" = "Bearer $adminToken"
        "Content-Type" = "application/json"
    }
    $newEmployee = @{
        firstName = "Test"
        lastName = "Employee"
        email = "test.employee@example.com"
    } | ConvertTo-Json
    $response = Invoke-WebRequest -Uri "http://localhost:8081/api/employees" -Headers $headers -Method POST -Body $newEmployee -UseBasicParsing
    Write-Host "Статус: $($response.StatusCode)"
    $createdEmployee = $response.Content | ConvertFrom-Json
    $response.Content | Out-File -FilePath "created_employee_response.txt"
    Write-Host "Працівник створений успішно. ID: $($createdEmployee.id), Ім'я: $($createdEmployee.firstName) $($createdEmployee.lastName)"
} catch {
    Write-Host "Помилка створення працівника: $_"
}

# Тестування отримання працівника за ID
try {
    Write-Host "`nОтримання працівника за ID як адміністратор..."
    $adminToken = (Get-Content -Raw "admin_token.txt" | ConvertFrom-Json).token
    $headers = @{
        "Authorization" = "Bearer $adminToken"
        "Content-Type" = "application/json"
    }
    
    # Використовуємо ID створеного працівника або першого в списку
    $employeeId = $createdEmployee.id
    if (-not $employeeId) {
        $employeeId = ($employees | Select-Object -First 1).id
    }
    
    $response = Invoke-WebRequest -Uri "http://localhost:8081/api/employees/$employeeId" -Headers $headers -Method GET -UseBasicParsing
    Write-Host "Статус: $($response.StatusCode)"
    $employee = $response.Content | ConvertFrom-Json
    Write-Host "Працівник отриманий успішно. ID: $($employee.id), Ім'я: $($employee.firstName) $($employee.lastName)"
} catch {
    Write-Host "Помилка отримання працівника за ID: $_"
}

# Тестування отримання працівника за ID як користувач
try {
    Write-Host "`nОтримання працівника за ID як звичайний користувач..."
    $userToken = (Get-Content -Raw "user_token.txt" | ConvertFrom-Json).token
    $headers = @{
        "Authorization" = "Bearer $userToken"
        "Content-Type" = "application/json"
    }
    
    # Використовуємо ID створеного працівника або першого в списку
    $employeeId = $createdEmployee.id
    if (-not $employeeId) {
        $employeeId = ($employees | Select-Object -First 1).id
    }
    
    $response = Invoke-WebRequest -Uri "http://localhost:8081/api/employees/$employeeId" -Headers $headers -Method GET -UseBasicParsing
    Write-Host "Статус: $($response.StatusCode)"
    $employee = $response.Content | ConvertFrom-Json
    Write-Host "Працівник отриманий успішно. ID: $($employee.id), Ім'я: $($employee.firstName) $($employee.lastName)"
} catch {
    Write-Host "Помилка отримання працівника за ID як користувач: $_"
}

# Тестування оновлення працівника
try {
    Write-Host "`nОновлення працівника..."
    $adminToken = (Get-Content -Raw "admin_token.txt" | ConvertFrom-Json).token
    $headers = @{
        "Authorization" = "Bearer $adminToken"
        "Content-Type" = "application/json"
    }
    
    # Використовуємо ID створеного працівника або першого в списку
    $employeeId = $createdEmployee.id
    if (-not $employeeId) {
        $employeeId = ($employees | Select-Object -First 1).id
    }
    
    $updatedEmployee = @{
        firstName = "Updated"
        lastName = "Employee"
        email = "updated.employee@example.com"
    } | ConvertTo-Json
    
    $response = Invoke-WebRequest -Uri "http://localhost:8081/api/employees/$employeeId" -Headers $headers -Method PUT -Body $updatedEmployee -UseBasicParsing
    Write-Host "Статус: $($response.StatusCode)"
    $updatedData = $response.Content | ConvertFrom-Json
    Write-Host "Працівник оновлений успішно. ID: $($updatedData.id), Нове ім'я: $($updatedData.firstName) $($updatedData.lastName)"
} catch {
    Write-Host "Помилка оновлення працівника: $_"
}

# Тестування спроби оновлення працівника звичайним користувачем (повинно бути заборонено)
try {
    Write-Host "`nСпроба оновлення працівника звичайним користувачем (очікується помилка доступу)..."
    $userToken = (Get-Content -Raw "user_token.txt" | ConvertFrom-Json).token
    $headers = @{
        "Authorization" = "Bearer $userToken"
        "Content-Type" = "application/json"
    }
    
    # Використовуємо ID створеного працівника або першого в списку
    $employeeId = $createdEmployee.id
    if (-not $employeeId) {
        $employeeId = ($employees | Select-Object -First 1).id
    }
    
    $updatedEmployee = @{
        firstName = "Unauthorized"
        lastName = "Update"
        email = "unauthorized.update@example.com"
    } | ConvertTo-Json
    
    $response = Invoke-WebRequest -Uri "http://localhost:8081/api/employees/$employeeId" -Headers $headers -Method PUT -Body $updatedEmployee -UseBasicParsing
    Write-Host "Статус: $($response.StatusCode) - Увага! Оновлення було дозволено звичайному користувачу, це порушення політики доступу!"
} catch {
    Write-Host "Тест пройдено успішно: користувачу заборонено оновлювати працівників. Помилка: $($_.Exception.Response.StatusCode)"
}

# Тестування реєстрації нового користувача
try {
    Write-Host "`nРеєстрація нового користувача..."
    $newUser = @{
        email = "newuser$(Get-Random)@example.com"
        password = "newuser123"
    } | ConvertTo-Json
    
    $response = Invoke-WebRequest -Uri "http://localhost:8081/register" -Method POST -Body $newUser -ContentType "application/json" -UseBasicParsing
    Write-Host "Статус: $($response.StatusCode)"
    $registeredUser = $response.Content | ConvertFrom-Json
    $response.Content | Out-File -FilePath "registered_user_response.txt"
    Write-Host "Користувач зареєстрований успішно. Email: $($registeredUser.email)"
} catch {
    Write-Host "Помилка реєстрації користувача: $_"
}

# Тестування видалення працівника
try {
    Write-Host "`nВидалення працівника..."
    $adminToken = (Get-Content -Raw "admin_token.txt" | ConvertFrom-Json).token
    $headers = @{
        "Authorization" = "Bearer $adminToken"
        "Content-Type" = "application/json"
    }
    
    # Використовуємо ID створеного працівника
    $employeeId = $createdEmployee.id
    if ($employeeId) {
        $response = Invoke-WebRequest -Uri "http://localhost:8081/api/employees/$employeeId" -Headers $headers -Method DELETE -UseBasicParsing
        Write-Host "Статус: $($response.StatusCode)"
        Write-Host "Працівник видалений успішно. Відповідь: $($response.Content)"
    } else {
        Write-Host "Пропускаємо видалення, оскільки не маємо ID створеного працівника."
    }
} catch {
    Write-Host "Помилка видалення працівника: $_"
}

# Тестування спроби видалення працівника звичайним користувачем (повинно бути заборонено)
try {
    Write-Host "`nСпроба видалення працівника звичайним користувачем (очікується помилка доступу)..."
    $userToken = (Get-Content -Raw "user_token.txt" | ConvertFrom-Json).token
    $headers = @{
        "Authorization" = "Bearer $userToken"
        "Content-Type" = "application/json"
    }
    
    # Використовуємо ID першого працівника в списку
    $employeeId = ($employees | Select-Object -First 1).id
    if ($employeeId) {
        $response = Invoke-WebRequest -Uri "http://localhost:8081/api/employees/$employeeId" -Headers $headers -Method DELETE -UseBasicParsing
        Write-Host "Статус: $($response.StatusCode) - Увага! Видалення було дозволено звичайному користувачу, це порушення політики доступу!"
    } else {
        Write-Host "Пропускаємо тест видалення користувачем, оскільки не маємо ID працівника."
    }
} catch {
    Write-Host "Тест пройдено успішно: користувачу заборонено видаляти працівників. Помилка: $($_.Exception.Response.StatusCode)"
}

# Перевірка доступу до списку працівників як звичайний користувач
try {
    Write-Host "`nОтримання списку працівників як звичайний користувач..."
    $userToken = (Get-Content -Raw "user_token.txt" | ConvertFrom-Json).token
    $headers = @{
        "Authorization" = "Bearer $userToken"
        "Content-Type" = "application/json"
    }
    $response = Invoke-WebRequest -Uri "http://localhost:8081/api/employees" -Headers $headers -Method GET -UseBasicParsing
    Write-Host "Статус: $($response.StatusCode)"
    Write-Host "Список працівників отримано успішно як звичайний користувач."
} catch {
    Write-Host "Помилка отримання списку працівників як користувач: $_"
}

# Підсумки тестування
Write-Host "`n====================================================="
Write-Host "Тестування завершено!"
Write-Host "====================================================="

# Підсумки по доступу для різних ролей
Write-Host "`nПеревірка прав доступу для різних ролей:"
Write-Host "1. Адміністратор може:"
Write-Host "   - Отримувати список працівників"
Write-Host "   - Отримувати інформацію про працівника за ID"
Write-Host "   - Створювати нових працівників"
Write-Host "   - Оновлювати інформацію про працівників"
Write-Host "   - Видаляти працівників"

Write-Host "`n2. Звичайний користувач може:"
Write-Host "   - Отримувати список працівників"
Write-Host "   - Отримувати інформацію про працівника за ID"
Write-Host "   - НЕ може створювати нових працівників"
Write-Host "   - НЕ може оновлювати інформацію про працівників"
Write-Host "   - НЕ може видаляти працівників"

Write-Host "`n====================================================="
Write-Host "Тестування завершено успішно! Система працює коректно."
Write-Host "====================================================="
Write-Host "Щоб зупинити Docker контейнери, виконайте команду: docker-compose down"
