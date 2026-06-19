@echo off
echo ========================================================
echo STARTING THE SYSTEM (Enhanced Futuristic Edition)
echo ========================================================

echo.
echo [1/3] Starting Memory Service (Python/FastAPI)...
start "Memory Service" cmd /c "cd memory-service && pip install -r requirements.txt && python -m uvicorn main:app --port 8001"

echo.
echo [2/3] Starting Backend Engine (Spring Boot)...
start "Backend Service" cmd /c "cd SystemBackend && mvn spring-boot:run"

echo.
echo [3/3] Starting Frontend Interface (React Native / Expo)...
start "Frontend Service" cmd /c "cd SystemApp && npm install && npx expo start"

echo.
echo ========================================================
echo All services have been launched in separate windows!
echo - Wait a minute for Spring Boot and Expo to fully load.
echo - Scan the QR code in the Frontend window using Expo Go on your phone.
echo ========================================================
pause
