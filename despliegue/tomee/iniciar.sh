#!/bin/sh
# ==========================================================
#  uniMotor - Arranque de TomEE 8 dentro del contenedor Docker
#
#  Genera conf/tomee.xml con los recursos que pide la aplicacion
#  (jdbc/prueba y mail/unimotor) a partir de variables de entorno,
#  ajusta el puerto y la memoria, y arranca TomEE en primer plano.
#
#  Variables de entorno:
#    Base de datos (obligatorias): DB_HOST, DB_USER, DB_PASSWORD
#      Opcionales: DB_PORT (3306), DB_NAME (unimotor),
#                  DB_SSL_MODE (REQUIRED; DISABLED para un MySQL local sin SSL)
#      O bien DB_URL con la URL JDBC completa (reemplaza a host, puerto, base y SSL).
#    Correo (opcional; sin MAIL_HOST los correos se simulan y se escriben en el log):
#      MAIL_HOST, MAIL_PORT (587), MAIL_USER, MAIL_PASSWORD,
#      MAIL_FROM (no-responder@unimotor.local), MAIL_STARTTLS (true)
#    Servidor: PORT (8080; Render la define sola), JAVA_XMX (320m)
# ==========================================================
set -e

TOMEE=/usr/local/tomee

# Escapa &, < y > para poder escribir el valor dentro del XML
xml() {
	printf '%s' "$1" | sed -e 's/&/\&amp;/g' -e 's/</\&lt;/g' -e 's/>/\&gt;/g'
}

if [ -z "$DB_URL" ]; then
	: "${DB_HOST:?Falta la variable DB_HOST (servidor MySQL)}"
	DB_URL="jdbc:mysql://${DB_HOST}:${DB_PORT:-3306}/${DB_NAME:-unimotor}?sslMode=${DB_SSL_MODE:-REQUIRED}&allowPublicKeyRetrieval=true&serverTimezone=America/Bogota&characterEncoding=UTF-8"
fi
: "${DB_USER:?Falta la variable DB_USER (usuario de MySQL)}"
: "${DB_PASSWORD:?Falta la variable DB_PASSWORD (contrasena de MySQL)}"

{
	echo '<?xml version="1.0" encoding="UTF-8"?>'
	echo '<!-- Generado por iniciar.sh al arrancar el contenedor: no editar -->'
	echo '<tomee>'
	echo '  <Resource id="jdbc/prueba" type="javax.sql.DataSource">'
	echo "    JdbcDriver = com.mysql.cj.jdbc.Driver"
	echo "    JdbcUrl = $(xml "$DB_URL")"
	echo "    UserName = $(xml "$DB_USER")"
	echo "    Password = $(xml "$DB_PASSWORD")"
	echo "    JtaManaged = true"
	echo "    MaxActive = 10"
	echo "    MaxIdle = 4"
	echo "    MinIdle = 1"
	echo "    TestOnBorrow = true"
	echo "    ValidationQuery = SELECT 1"
	echo '  </Resource>'
	if [ -n "$MAIL_HOST" ]; then
		echo '  <Resource id="mail/unimotor" type="javax.mail.Session">'
		echo "    mail.transport.protocol = smtp"
		echo "    mail.smtp.host = $(xml "$MAIL_HOST")"
		echo "    mail.smtp.port = ${MAIL_PORT:-587}"
		echo "    mail.smtp.starttls.enable = ${MAIL_STARTTLS:-true}"
		echo "    mail.smtp.connectiontimeout = 10000"
		echo "    mail.smtp.timeout = 10000"
		echo "    mail.from = $(xml "${MAIL_FROM:-no-responder@unimotor.local}")"
		if [ -n "$MAIL_USER" ]; then
			echo "    mail.smtp.auth = true"
			echo "    mail.smtp.user = $(xml "$MAIL_USER")"
			echo "    password = $(xml "$MAIL_PASSWORD")"
		fi
		echo '  </Resource>'
	fi
	echo '  <Deployments dir="apps" />'
	echo '</tomee>'
} > "$TOMEE/conf/tomee.xml"

if [ -n "$MAIL_HOST" ]; then
	echo "uniMotor: correo por ${MAIL_HOST}:${MAIL_PORT:-587}"
else
	echo "uniMotor: sin MAIL_HOST, los correos se simulan (se escriben en el log)"
fi

# Puerto HTTP (Render indica en PORT el puerto en el que debe escuchar el servicio)
sed -i "s/<Connector port=\"[0-9]*\" protocol=\"HTTP\/1.1\"/<Connector port=\"${PORT:-8080}\" protocol=\"HTTP\/1.1\"/" "$TOMEE/conf/server.xml"

# Memoria ajustada a un plan pequeno (512 MB): heap limitado, metaspace acotado y recolector serial
export CATALINA_OPTS="$CATALINA_OPTS -Xms128m -Xmx${JAVA_XMX:-320m} -XX:MaxMetaspaceSize=192m -XX:ReservedCodeCacheSize=48m -Xss512k -XX:+UseSerialGC -Duser.timezone=America/Bogota -Duser.language=es -Duser.country=CO -Dfile.encoding=UTF-8"

exec catalina.sh run
