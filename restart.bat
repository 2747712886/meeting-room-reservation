@echo off
setlocal

if "%~1"=="/?" goto :help
if /i "%~1"=="help" goto :help

set "ROOT_DIR=%~dp0"
set "BACKEND_DIR=%ROOT_DIR%reservation-backend"
set "FRONTEND_DIR=%ROOT_DIR%reservation-frontend"

echo [1/4] Starting Docker dependencies...
pushd "%ROOT_DIR%"
docker compose up -d
if errorlevel 1 (
  echo Failed to start Docker dependencies.
  popd
  pause
  exit /b 1
)
popd

echo [2/4] Releasing backend/frontend ports...
call :kill_port 8080
call :kill_port 5173

echo [3/4] Starting backend on http://127.0.0.1:8080 ...
start "reservation-backend" /min cmd /c "cd /d ""%BACKEND_DIR%"" && mvn.cmd spring-boot:run > backend-run.out.log 2> backend-run.err.log"

echo Waiting for backend health check...
call :wait_backend
if errorlevel 1 (
  echo Backend did not become ready in time.
  echo Check logs:
  echo   %BACKEND_DIR%\backend-run.out.log
  echo   %BACKEND_DIR%\backend-run.err.log
  pause
  exit /b 1
)

echo [4/4] Starting frontend on http://127.0.0.1:5173 ...
start "reservation-frontend" /min cmd /c "cd /d ""%FRONTEND_DIR%"" && npm.cmd run dev -- --host 127.0.0.1 > frontend-run.out.log 2> frontend-run.err.log"

echo.
echo Restart commands have been issued.
echo Backend:  http://127.0.0.1:8080/api/health
echo Frontend: http://127.0.0.1:5173/
echo RabbitMQ: http://127.0.0.1:15672/  admin / 123456
echo.
echo Check logs if a service does not start:
echo   %BACKEND_DIR%\backend-run.out.log
echo   %BACKEND_DIR%\backend-run.err.log
echo   %FRONTEND_DIR%\frontend-run.out.log
echo   %FRONTEND_DIR%\frontend-run.err.log
pause
exit /b 0

:help
echo Usage: restart.bat
echo.
echo Restarts local development services:
echo   1. docker compose up -d
echo   2. stop listeners on ports 8080 and 5173
echo   3. start backend and frontend in minimized windows
exit /b 0

:kill_port
set "PORT=%~1"
for /f "tokens=5" %%p in ('netstat -ano ^| findstr /r /c:":%PORT% .*LISTENING"') do (
  if not "%%p"=="0" (
    echo Stopping process on port %PORT%: %%p
    taskkill /PID %%p /T /F >nul 2>nul
  )
)
exit /b 0

:wait_backend
for /l %%i in (1,1,30) do (
  powershell -NoProfile -Command "try { $r = Invoke-WebRequest -Uri 'http://127.0.0.1:8080/api/health' -UseBasicParsing -TimeoutSec 2; if ($r.StatusCode -eq 200) { exit 0 } } catch { exit 1 }"
  if not errorlevel 1 (
    echo Backend is ready.
    exit /b 0
  )
  echo Waiting for backend... %%i/30
  timeout /t 3 /nobreak >nul
)
exit /b 1
