@echo off
REM ==========================================================
REM  uniMotor - Inicia MySQL (Docker) y Payara
REM  Requiere: Docker Desktop abierto, JAVA_HOME y PAYARA_HOME
REM ==========================================================
if "%PAYARA_HOME%"=="" set PAYARA_HOME=C:\Users\cfbas\dev\payara5

echo [1/2] Iniciando MySQL (contenedor unimotor-mysql)...
docker start unimotor-mysql >nul 2>&1
if errorlevel 1 (
  echo   No se pudo iniciar el contenedor. Abra Docker Desktop e intente de nuevo.
  exit /b 1
)

echo [2/2] Iniciando Payara (domain1)...
call "%PAYARA_HOME%\bin\asadmin.bat" start-domain domain1

echo.
echo Consola de administracion: http://localhost:4848
echo Aplicacion web:            http://localhost:8080/proyecto-web/
