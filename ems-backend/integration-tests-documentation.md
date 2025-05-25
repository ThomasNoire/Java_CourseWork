# Документація інтеграційних тестів EMS Backend

## Огляд

Ця документація описує структуру та функціональність інтеграційних тестів для Employee Management System (EMS) Backend. Інтеграційні тести перевіряють взаємодію різних компонентів системи та забезпечують коректну роботу всієї системи в цілому.

## Структура тестів

Інтеграційні тести розділені на кілька категорій:

1. **FullSystemTest** - повне системне тестування з перевіркою всіх основних сценаріїв
2. **EmployeeControllerIntegrationTest** - тести для контролера співробітників
3. **SecurityAccessTest** - тести для перевірки безпеки та прав доступу
4. **SimpleServerTest** - простий тест для перевірки роботи сервера
5. **AdditionalIntegrationTests** - додаткові тести для покриття різних сценаріїв
6. **ValidationAndErrorHandlingTests** - тести для валідації даних та обробки помилок
7. **JwtSecurityTests** - тести для перевірки безпеки JWT

## User Stories та їх покриття

### User Story 1: Реєстрація та аутентифікація користувачів

**Позитивні сценарії:**
- ✅ Реєстрація нового користувача (`testRegisterUser`)
- ✅ Вхід адміністратора з коректними обліковими даними (`testAdminLogin`)
- ✅ Вхід звичайного користувача з коректними обліковими даними (`testUserLogin`)
- ✅ Отримання JWT токена при успішній аутентифікації (`testSuccessfulLogin_shouldReturnToken`)

**Негативні сценарії:**
- ✅ Реєстрація з недійсними даними (`testInvalidRegistrationData_shouldReturn400`)
- ✅ Спроба зареєструвати користувача з email, який вже існує (`testDuplicateEmail_shouldReturnError`)
- ✅ Вхід з неправильними обліковими даними (`testInvalidLoginCredentials_shouldReturn401`)
- ✅ Реєстрація з порожнім email (`testRegisterUserWithEmptyEmail_shouldReturnValidationError`)
- ✅ Реєстрація з занадто коротким паролем (`testRegisterUserWithShortPassword_shouldReturnValidationError`)

### User Story 2: Управління співробітниками (CRUD операції)

**Позитивні сценарії:**
- ✅ Створення нового співробітника адміністратором (`testCreateEmployee`, `testAdminCanCreateEmployee`)
- ✅ Отримання списку всіх співробітників (`testAdminCanGetEmployees`, `testUserCanGetEmployees`)
- ✅ Отримання інформації про конкретного співробітника (`testGetEmployeeById_withAuth_shouldReturnEmployee`, `testBothRolesCanGetEmployeeById`)
- ✅ Оновлення інформації про співробітника адміністратором (`testUpdateEmployee_withAuth_shouldReturnUpdatedEmployee`, `testAdminCanUpdateEmployee`)
- ✅ Видалення співробітника адміністратором (`testDeleteEmployee_withAuth_shouldReturnSuccessMessage`, `testAdminCanDeleteEmployee`)

**Негативні сценарії:**
- ✅ Спроба створити співробітника звичайним користувачем (`testUserCannotCreateEmployee`)
- ✅ Спроба оновити інформацію про співробітника звичайним користувачем (`testUserCannotUpdateEmployee`)
- ✅ Спроба видалити співробітника звичайним користувачем (`testUserCannotDeleteEmployee`)
- ✅ Створення співробітника з недійсними даними (`testCreateEmployeeWithInvalidData_shouldReturn400`)
- ✅ Оновлення неіснуючого співробітника (`testUpdateNonExistentEmployee_shouldReturn404`)
- ✅ Видалення неіснуючого співробітника (`testDeleteNonExistentEmployee_shouldReturn404`)
- ✅ Отримання неіснуючого співробітника (`testGetNonExistentEmployee_shouldReturn404`)

### User Story 3: Безпека та права доступу

**Позитивні сценарії:**
- ✅ Доступ адміністратора до захищених ресурсів (`testAdminRolePermissions`)
- ✅ Доступ користувача до дозволених ресурсів (`testUserRolePermissions`)
- ✅ Доступ до публічних ендпоінтів без аутентифікації (`testPublicEndpoints`)

**Негативні сценарії:**
- ✅ Спроба доступу без аутентифікації (`testUnauthenticatedAccess`, `testAuthenticationWithoutToken`)
- ✅ Спроба доступу з недійсним токеном (`testInvalidToken_shouldReturn401`)
- ✅ Спроба доступу з токеном з минулим строком дії (`testExpiredToken_shouldDenyAccess`)
- ✅ Спроба доступу з підробленим токеном (`testTamperedToken_shouldDenyAccess`)
- ✅ Спроба доступу користувача до ресурсів адміністратора (`testTokenWithUserRoleForAdminEndpoint_shouldDeny`)

### User Story 4: Валідація даних

**Позитивні сценарії:**
- ✅ Створення співробітника з валідними даними (покрито іншими тестами)

**Негативні сценарії:**
- ✅ Створення співробітника з порожнім іменем (`testCreateEmployeeWithEmptyFirstName_shouldReturnValidationError`)
- ✅ Створення співробітника з порожнім прізвищем (`testCreateEmployeeWithEmptyLastName_shouldReturnValidationError`)
- ✅ Створення співробітника з недійсним email (`testCreateEmployeeWithInvalidEmail_shouldReturnValidationError`)
- ✅ Створення співробітника з email, який вже існує (`testCreateEmployeeWithDuplicateEmail_shouldReturnError`)

## Запуск тестів

Для запуску всіх інтеграційних тестів використовуйте файл `run-integration-tests.bat`. Цей скрипт виконує наступні дії:

1. Будує проект
2. Запускає Docker-контейнери
3. Очікує запуску системи
4. Запускає всі інтеграційні тести
5. Зупиняє Docker-контейнери після завершення тестів

```
cd c:\Users\barch\OneDrive\Рабочий стол\backend2\ems-backend
.\run-integration-tests.bat
```

## Висновки

Система EMS Backend повністю покрита інтеграційними тестами, які перевіряють всі основні сценарії використання, включаючи позитивні та негативні випадки. Тести забезпечують коректну роботу аутентифікації, авторизації, управління співробітниками та валідації даних.

Тести використовують підхід зі справжніми HTTP-запитами до запущеного сервера, що дозволяє перевірити реальну поведінку системи в умовах, близьких до реального використання.

Інтеграційні тести доповнюють PowerShell-скрипти для тестування API, створені раніше, і разом вони забезпечують високу якість та надійність системи.

## Примітки щодо оновлень

### Облікові дані адміністратора
- Адміністративний користувач створюється з наступними обліковими даними:
  - Email: `admin@example.com`
  - Пароль: `admin123`
- Ці облікові дані використовуються у всіх тестах та скриптах
