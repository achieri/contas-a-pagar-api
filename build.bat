@echo off
echo ========================================
echo   Contas a Pagar API - Build ^& Deploy
echo ========================================
echo.

echo [1/3] Stopping containers...
docker compose down

echo [2/3] Building and starting containers...
docker compose up -d --build

echo [3/3] Waiting for services...
timeout /t 10 /nobreak >nul

echo.
echo ========================================
echo   Deployment Complete!
echo   API:      http://localhost:4021
echo   Swagger:  http://localhost:4021/swagger-ui.html
echo   RabbitMQ: http://localhost:4024  (guest/guest)
echo ========================================
