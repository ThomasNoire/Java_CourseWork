# EMS Backend API Testing Report

## Testing Summary
- **Date:** May 22, 2025
- **System:** Employee Management System (EMS) Backend
- **Environment:** Docker Containers (API Server + PostgreSQL)

## System Architecture

EMS (Employee Management System) є бекенд-додатком, побудованим на наступних компонентах:

1. **Spring Boot Application** - Java-бекенд на порту 8081
2. **PostgreSQL Database** - Працює в Docker-контейнері
3. **JWT Authentication** - Захищена аутентифікація на основі токенів
4. **RESTful API** - CRUD-операції для керування працівниками

## Ролі користувачів та права доступу

Система реалізує контроль доступу на основі ролей з двома основними ролями:

### 1. Адміністратор (ADMIN)
Адміністратори мають повний доступ до системи:
- Перегляд списку всіх працівників
- Перегляд деталей конкретних працівників за ID
- Створення нових працівників
- Оновлення інформації про існуючих працівників
- Видалення працівників з системи

### 2. Звичайний користувач (USER)
Звичайні користувачі мають обмежений доступ:
- Перегляд списку всіх працівників
- Перегляд деталей конкретних працівників за ID
- Не можуть створювати нових працівників (заборонено)
- Не можуть оновлювати інформацію про працівників (заборонено)
- Не можуть видаляти працівників (заборонено)

## API Endpoints

### Аутентифікація
- **/login** (POST) - Аутентифікація користувачів та отримання JWT-токена
- **/register** (POST) - Реєстрація нових користувачів (призначається роль USER за замовчуванням)

### Керування працівниками
- **/api/employees** (GET) - Отримання всіх працівників (доступно для ADMIN та USER)
- **/api/employees/{id}** (GET) - Отримання працівника за ID (доступно для ADMIN та USER)
- **/api/employees** (POST) - Створення нового працівника (доступно тільки для ADMIN)
- **/api/employees/{id}** (PUT) - Оновлення інформації про працівника (доступно тільки для ADMIN)
- **/api/employees/{id}** (DELETE) - Видалення працівника (доступно тільки для ADMIN)

## Test Results

### Authentication Endpoints
| Endpoint | Method | Result | Notes |
|----------|--------|--------|-------|
| `/login` | POST | ✅ Successful | Admin and regular user authentication work correctly |
| `/register` | POST | ✅ Successful | New users can be registered with USER role |

### Employee Endpoints
| Endpoint | Method | User Role | Result | Notes |
|----------|--------|-----------|--------|-------|
| `/api/employees` | GET | ADMIN | ✅ Successful | Can retrieve all employees |
| `/api/employees` | GET | USER | ✅ Successful | Regular users can also access the list |
| `/api/employees/{id}` | GET | ADMIN | ✅ Successful | Can retrieve specific employee |
| `/api/employees/{id}` | GET | USER | ✅ Successful | Regular users can retrieve specific employee |
| `/api/employees` | POST | ADMIN | ✅ Successful | Can create new employee |
| `/api/employees` | POST | USER | ❌ Forbidden | Regular users cannot create employees |
| `/api/employees/{id}` | PUT | ADMIN | ✅ Successful | Can update existing employee |
| `/api/employees/{id}` | PUT | USER | ❌ Forbidden | Regular users cannot update employees |
| `/api/employees/{id}` | DELETE | ADMIN | ✅ Successful | Can delete employees |
| `/api/employees/{id}` | DELETE | USER | ❌ Forbidden | Regular users cannot delete employees |

## Authorization Verification
The security roles are working as expected:
1. **ADMIN Role:**
   - Full access to all endpoints
   - Can perform all CRUD operations

2. **USER Role:**
   - Read-only access
   - Can only view employee data
   - Cannot create, update, or delete employees

## Системні покращення

1. **Сумісність версій Java**:
   - Змінено з Java 21 на Java 17 для кращої сумісності

2. **Виправлення конфігурації**:
   - Видалено суперечливі налаштування профілів у application-test.properties

3. **Підхід до тестування**:
   - Перехід від JUnit-тестів до прямих HTTP-запитів для більш реалістичного тестування
   - Тестування на основі PowerShell для кращої автоматизації та звітності

4. **Обробка помилок**:
   - Покращена обробка помилок у API-тестах
   - Краще логування результатів тестування

## Як запустити систему

1. **Запустити систему**:
   ```
   .\run-test-api-complete.bat
   ```

2. **Доступ до API**:
   - Swagger UI: http://localhost:8081/swagger-ui/index.html
   - Прямий API: http://localhost:8081/api/employees

3. **Аутентифікація**:
   - Облікові дані адміністратора: admin@example.com / admin123
   - Облікові дані користувача: user@example.com / user123

4. **Зупинити систему**:
   ```
   docker-compose down
   ```

## Висновок
Бекенд-система EMS успішно реалізує захищену систему управління працівниками на основі ролей з відповідними контролями доступу. API надає всі необхідні CRUD-операції для управління працівниками з належними перевірками авторизації, щоб забезпечити, що тільки адміністратори можуть змінювати дані, тоді як звичайні користувачі мають доступ тільки для читання.

## How to Run Tests
```
cd c:\Users\barch\OneDrive\Рабочий стол\backend2\ems-backend
powershell -ExecutionPolicy Bypass -File .\test-api-simple.ps1
```
