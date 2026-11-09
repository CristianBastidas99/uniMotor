@echo off
REM ==========================================================
REM  uniMotor - Construye la imagen de la demo (TomEE 8) y la
REM  ejecuta en local, como en Render, contra el MySQL de Docker.
REM  Uso: scripts\demo-docker.bat   (desde la raiz del proyecto)
REM  Requiere: Docker Desktop abierto, contenedores unimotor-mysql
REM  y unimotor-mailpit, y el puerto 8080 libre (detener Payara).
REM ==========================================================
cd /d "%~dp0.."

echo [1/3] Iniciando MySQL y Mailpit...
docker start unimotor-mysql unimotor-mailpit >nul 2>&1

echo [2/3] Construyendo la imagen unimotor-demo...
docker build -t unimotor-demo .
if errorlevel 1 (
  echo   Error al construir la imagen.
  exit /b 1
)

echo [3/3] Ejecutando el contenedor (512 MB, como el plan gratuito de Render)...
docker rm -f unimotor-demo >nul 2>&1
docker run -d --name unimotor-demo --memory=512m --memory-swap=512m -p 8080:8080 ^
  -e DB_HOST=host.docker.internal -e DB_USER=root -e DB_PASSWORD=12345 -e DB_SSL_MODE=DISABLED ^
  -e MAIL_HOST=host.docker.internal -e MAIL_PORT=1025 -e MAIL_STARTTLS=false ^
  unimotor-demo

echo.
echo Web:        http://localhost:8080/proyecto-web/   (tarda cerca de 1 minuto en arrancar)
echo Escritorio: configuracion "Escritorio (TomEE local)" de IntelliJ
echo Log:        docker logs -f unimotor-demo
echo Detener:    docker rm -f unimotor-demo
