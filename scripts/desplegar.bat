@echo off
REM ==========================================================
REM  uniMotor - Compila todo y despliega el EAR en Payara
REM  Uso: scripts\desplegar.bat   (desde la raiz del proyecto)
REM ==========================================================
if "%PAYARA_HOME%"=="" set PAYARA_HOME=C:\Users\cfbas\dev\payara5
cd /d "%~dp0.."

echo [1/2] Compilando con Maven...
call mvn -B clean install -DskipTests
if errorlevel 1 (
  echo   Error de compilacion. No se despliega.
  exit /b 1
)

echo [2/2] Desplegando proyecto-ear en Payara...
for %%f in (proyecto-ear\target\proyecto-ear-*.ear) do (
  call "%PAYARA_HOME%\bin\asadmin.bat" deploy --name proyecto-ear --force=true "%%f"
)

echo.
echo Web: http://localhost:8080/proyecto-web/
echo Log: %PAYARA_HOME%\glassfish\domains\domain1\logs\server.log
