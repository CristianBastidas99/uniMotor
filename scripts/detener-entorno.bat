@echo off
REM ==========================================================
REM  uniMotor - Detiene Payara y MySQL (Docker)
REM ==========================================================
if "%PAYARA_HOME%"=="" set PAYARA_HOME=C:\Users\cfbas\dev\payara5

echo Deteniendo Payara...
call "%PAYARA_HOME%\bin\asadmin.bat" stop-domain domain1

echo Deteniendo MySQL...
docker stop unimotor-mysql
