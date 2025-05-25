@echo off
echo Starting EMS Backend application with Docker...

REM Зупинка контейнерів, якщо вони вже запущені
docker-compose down

REM Створення .env файлу, якщо він не існує
echo SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/ems_backend > .env
echo SPRING_DATASOURCE_USERNAME=postgres >> .env
echo SPRING_DATASOURCE_PASSWORD=lolkas228 >> .env
echo JWT_SECRET=Xdks8h1aF2Ieqf9Nc9jztnKZoU9rSbmUpxkBtiQgLlVYFuydq1S4uDgF9ikR6dvw >> .env

REM Запуск Docker Compose
docker-compose up -d

echo Waiting for application to start...
timeout /t 20

echo Running integration tests...
mvnw.cmd test

echo Tests completed. You can now manually test the API with the test-api.bat script.
echo The application is running at http://localhost:8081
echo Swagger UI is available at http://localhost:8081/swagger-ui/index.html

pause 