# Guía: publicar gratis una aplicación Java EE 8 en Render

Guía reutilizable, escrita a partir de la demo de uniMotor publicada el 2026-11-08 en
<https://unimotor.onrender.com/proyecto-web/> (rama `despliegue-render` del repositorio
`CristianBastidas99/uniMotor`). Sirve para cualquier EAR o WAR de **Java EE 8** (EJB, JPA, JSF, JAX-RS, JavaMail)
con base **MySQL** que tenga que caber en **512 MB de RAM** y que se quiera desplegar automáticamente desde GitHub.

> Los planes gratuitos cambian. Lo que dice esta guía es lo que había en noviembre de 2026. Antes de seguirla en otro
> proyecto, revisa que cada servicio siga siendo gratuito.

---

## 1. Arquitectura y servicios

| Pieza | Servicio | Plan | Para qué |
|---|---|---|---|
| Servidor de aplicaciones | [Render](https://render.com) | *Free* (web service con Docker) | Compila la imagen desde GitHub y la despliega en cada push |
| Base de datos | [TiDB Cloud](https://tidbcloud.com) | *Starter* (gratis, sin tarjeta) | Compatible con MySQL 8; 5 GiB y 50 M *Request Units* al mes |
| Servidor Java EE | Apache **TomEE 8.0.16 Plume** (imagen Docker oficial) | — | Java EE 8 con EclipseLink y Mojarra, como Payara, pero en ~300 MB |

**Por qué no Payara:** Payara Server necesita cerca de 1 GB de RAM y no cabe en los 512 MB gratuitos de Render.
TomEE **Plume**, y no *Plus* ni *WebProfile*, trae **EclipseLink** (JPA) y **Mojarra** (JSF), así que un proyecto
hecho para Payara o GlassFish despliega casi sin cambios. *Plus* usa OpenJPA y MyFaces.

### Descartados

| Servicio | Problema |
|---|---|
| **Aiven** (MySQL) | Ya no ofrece MySQL gratis a cuentas nuevas. Solo *Developer* (~5 USD/mes) o *Professional*, pagados con 50 USD de créditos de prueba que se acaban. Además exige clave primaria en todas las tablas (`sql_require_primary_key`). |
| **Ethereal**, **Gmail** o cualquier SMTP en 25/465/587 | El plan gratuito de Render **bloquea el SMTP saliente** en los puertos 25, 465 y 587 desde septiembre de 2025. Ethereal abre el puerto 2525, pero no responde. |
| **Mailtrap Sandbox** (SMTP en 2525) | Era la alternativa para el correo, porque Render no bloquea el 2525. Sin probar, porque en uniMotor se decidió dejar el correo simulado. También existen APIs HTTP de correo (Brevo, Resend), pero exigen cambiar el código. |

---

## 2. Límites del plan gratuito de Render

- **512 MB de RAM** y 0,1 CPU: el heap de Java debe ir limitado (`-Xmx320m`).
- **Se duerme tras 15 minutos sin visitas.** La primera visita tarda cerca de un minuto en despertarlo, mientras TomEE arranca.
- **Disco efímero:** lo que la aplicación escriba en disco, como las fotos subidas, se pierde al redesplegar o reiniciar.
  Para imágenes de demostración, usar enlaces externos (por ejemplo, Wikimedia Commons).
- **Sin SMTP saliente** en 25/465/587.
- Escucha en el puerto que indica la variable `PORT`, que Render define sola.
- La compilación, con Maven dentro de Docker, tarda varios minutos.

---

## 3. Paso a paso

### 3.1 Adaptar el proyecto (una vez)

1. Crear los archivos de la [sección 4](#4-plantillas): `Dockerfile`, `despliegue/tomee/*`, `render.yaml`,
   `.dockerignore` y `.gitattributes`.
2. Revisar los [cambios de código para TomEE](#5-cambios-de-código-para-que-el-ear-funcione-en-tomee) que necesite el proyecto.
3. Probar en local con Docker ([sección 6](#6-probar-en-local-antes-de-subir)), con 512 MB, antes de subir nada.

### 3.2 TiDB Cloud (base de datos)

1. Entrar en <https://tidbcloud.com>, pulsar **Sign up** y luego **Continue with GitHub**. No pide tarjeta.
2. En **My TiDB**, pulsar **Create Resource** (botón negro arriba a la derecha; si no se ve, maximizar la ventana o alejar el zoom).
3. Elegir el plan **Starter** ("Free Instances: 0 of 5 used"). **No** elegir *Essential* ni *Dedicated*.
4. Configurar la instancia:
   - **Instance Name**: el nombre del proyecto, de 4 a 64 caracteres, solo letras, números y guiones.
   - **Cloud Provider**: AWS.
   - **Region**: **N. Virginia (us-east-1)**, la misma región que Render (`virginia`). Esto es importante por la latencia: ver el [problema 7.1](#71-la-primera-carga-de-datos-supera-el-timeout-de-transacción).
   - **Monthly Spending Limit**: **0**.
5. Pulsar **Create** y esperar a que el estado quede en *Available*.
6. Entrar a la instancia y pulsar **Connect**:
   - Elegir **Public** y **General**.
   - Pulsar **Generate Password** y copiar la contraseña en ese momento, porque no la vuelve a mostrar.
   - Anotar **host** (`gateway01.us-east-1.prod.aws.tidbcloud.com`), **puerto** (`4000`) y **usuario**.
   - El usuario lleva un prefijo: **`xxxxxxxx.root`**. Sin el prefijo falla con `ERROR 1105: Missing user name prefix`.
7. En **SQL Editor**, ejecutar `CREATE DATABASE <nombre>;`. EclipseLink crea las tablas en el primer arranque si el
   `persistence.xml` tiene `eclipselink.ddl-generation`.
8. Guardar los datos en un archivo local que no se suba al repositorio (en uniMotor, `notas/.env`):
   ```
   DB_HOST=gateway01.us-east-1.prod.aws.tidbcloud.com
   DB_PORT=4000
   DB_NAME=<nombre>
   DB_USER=xxxxxxxx.root
   DB_PASSWORD=...
   ```

Notas sobre TiDB:
- Es compatible con MySQL 8; la versión que reporta es `8.0.11-TiDB-v8.5.3-serverless`.
- Exige TLS. Con Connector/J basta `sslMode=REQUIRED`.
- **Sí acepta tablas sin clave primaria**, como las `@ElementCollection` de JPA, que en Aiven fallaban.
- Los `AUTO_INCREMENT` no son consecutivos. Si el código asume ids seguidos, revisarlo.
- Consultarla desde la consola, usando el cliente de un contenedor MySQL local:
  ```bash
  MSYS_NO_PATHCONV=1 docker exec -e MYSQL_PWD="$DB_PASSWORD" <contenedor-mysql> \
    mysql -h "$DB_HOST" -P 4000 -u "$DB_USER" --ssl-mode=REQUIRED <base>
  ```
  Si Docker no resuelve el nombre del host, usar su IP (en Windows: `Resolve-DnsName <host>`).

### 3.3 Render (aplicación)

1. Hacer commit y push de los archivos de la sección 4 a una rama, por ejemplo `despliegue-render`.
2. Entrar en <https://render.com>, pulsar **Get Started** y luego **GitHub**.
3. Pulsar **New** → **Blueprint** y elegir el repositorio.
   - En **Branch**, elegir la rama que tiene el `render.yaml`.
   - Render lee el `render.yaml` y pide las variables marcadas con `sync: false`: `DB_HOST`, `DB_USER` y `DB_PASSWORD`.
4. Pulsar **Apply** o **Deploy Blueprint**.
5. Seguir el log hasta ver `Server startup in [...] milliseconds` y que la comprobación de salud (`healthCheckPath`) responda 200.
6. La URL queda como `https://<nombre-del-servicio>.onrender.com`.

Desde entonces, cada push a esa rama vuelve a desplegar la aplicación (`autoDeploy: true`).

> Si una variable se quita del `render.yaml`, Render **no la borra** del servicio: hay que borrarla a mano en
> **Environment**.

### 3.4 Darla a conocer

- **README de `main`:** poner al principio un bloque con el enlace, avisar que la primera carga tarda ~1 minuto y
  enlazar los usuarios de prueba.
- **GitHub:** en la página del repositorio, pulsar el engranaje ⚙️ de **About** y pegar la URL en **Website**.
- Si la configuración de despliegue vive en otra rama, enlazarla desde el README:
  `https://github.com/<usuario>/<repo>/tree/<rama>`.

---

## 4. Plantillas

En cada archivo, cambiar lo marcado con `<...>`: nombres de módulos y del EAR, contexto web, paquete y zona horaria.

### 4.1 `Dockerfile` (raíz del repositorio)

```dockerfile
# ---------- Etapa 1: compilacion ----------
FROM maven:3.9-eclipse-temurin-8 AS compilacion
WORKDIR /fuente
COPY . .
# -pl <modulo-ear> -am: compila solo el EAR y los modulos de los que depende (no el escritorio ni las pruebas)
RUN mvn -B -q -pl <modulo-ear> -am package -DskipTests

# ---------- Etapa 2: servidor ----------
FROM tomee:8.0.16-jre8-Temurin-plume

ENV TZ=America/Bogota
WORKDIR /usr/local/tomee

# Driver de MySQL, sin las aplicaciones de ejemplo de TomEE, y carpeta apps/ para el EAR
RUN rm -rf webapps/* \
 && mkdir -p apps webapps/ROOT \
 && curl -fsSL -o lib/mysql-connector-j-8.0.33.jar \
      https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar

# (Solo si hay cliente de escritorio con EJB remoto) EJB por HTTP y lista blanca de serializacion
COPY despliegue/tomee/system.properties.extra /tmp/system.properties.extra
RUN sed -i '/^tomee.serialization.class.blacklist/d' conf/system.properties \
 && cat /tmp/system.properties.extra >> conf/system.properties \
 && rm /tmp/system.properties.extra

COPY despliegue/tomee/context.xml conf/context.xml
COPY despliegue/tomee/ROOT/index.html webapps/ROOT/index.html
COPY despliegue/tomee/iniciar.sh /usr/local/bin/iniciar.sh
# Quita los CR por si el archivo se edito en Windows
RUN sed -i 's/\r$//' /usr/local/bin/iniciar.sh && chmod +x /usr/local/bin/iniciar.sh

# Un EAR se despliega desde apps/ (NO desde webapps/). Un WAR si puede ir en webapps/
COPY --from=compilacion /fuente/<modulo-ear>/target/<artefacto>.ear apps/<nombre>.ear

EXPOSE 8080
CMD ["iniciar.sh"]
```

### 4.2 `despliegue/tomee/iniciar.sh`

Genera `conf/tomee.xml` al arrancar, a partir de variables de entorno. Así las contraseñas no quedan en la imagen ni
en el repositorio. Los `id` de los `Resource` deben coincidir con los nombres JNDI que usa la aplicación (en uniMotor,
`jdbc/prueba` y `mail/unimotor`).

```sh
#!/bin/sh
set -e
TOMEE=/usr/local/tomee

# Escapa &, < y > para poder escribir el valor dentro del XML
xml() {
	printf '%s' "$1" | sed -e 's/&/\&amp;/g' -e 's/</\&lt;/g' -e 's/>/\&gt;/g'
}

if [ -z "$DB_URL" ]; then
	: "${DB_HOST:?Falta la variable DB_HOST}"
	DB_URL="jdbc:mysql://${DB_HOST}:${DB_PORT:-3306}/${DB_NAME:-<base>}?sslMode=${DB_SSL_MODE:-REQUIRED}&allowPublicKeyRetrieval=true&serverTimezone=America/Bogota&characterEncoding=UTF-8"
fi
: "${DB_USER:?Falta la variable DB_USER}"
: "${DB_PASSWORD:?Falta la variable DB_PASSWORD}"

{
	echo '<?xml version="1.0" encoding="UTF-8"?>'
	echo '<tomee>'
	# El timeout por defecto de TomEE es 10 minutes (ver problema 7.1)
	echo '  <TransactionManager id="TransactionManager" type="TransactionManager">'
	echo "    defaultTransactionTimeout = ${TX_TIMEOUT:-1 hour}"
	echo '  </TransactionManager>'
	echo '  <Resource id="<jndi-datasource>" type="javax.sql.DataSource">'
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
		echo '  <Resource id="<jndi-mail>" type="javax.mail.Session">'
		echo "    mail.transport.protocol = smtp"
		echo "    mail.smtp.host = $(xml "$MAIL_HOST")"
		echo "    mail.smtp.port = ${MAIL_PORT:-587}"
		echo "    mail.smtp.starttls.enable = ${MAIL_STARTTLS:-true}"
		echo "    mail.smtp.connectiontimeout = 10000"
		echo "    mail.smtp.timeout = 10000"
		echo "    mail.from = $(xml "${MAIL_FROM:-no-responder@ejemplo.local}")"
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

# Render indica en PORT el puerto en el que debe escuchar el servicio
sed -i "s/<Connector port=\"[0-9]*\" protocol=\"HTTP\/1.1\"/<Connector port=\"${PORT:-8080}\" protocol=\"HTTP\/1.1\"/" "$TOMEE/conf/server.xml"

# Memoria para 512 MB: heap limitado, metaspace acotado y recolector serial
export CATALINA_OPTS="$CATALINA_OPTS -Xms128m -Xmx${JAVA_XMX:-320m} -XX:MaxMetaspaceSize=192m -XX:ReservedCodeCacheSize=48m -Xss512k -XX:+UseSerialGC -Duser.timezone=America/Bogota -Duser.language=es -Duser.country=CO -Dfile.encoding=UTF-8"

exec catalina.sh run
```

### 4.3 `despliegue/tomee/context.xml` (reemplaza `conf/context.xml`)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Context>
    <WatchedResource>WEB-INF/web.xml</WatchedResource>
    <WatchedResource>WEB-INF/tomcat-web.xml</WatchedResource>
    <WatchedResource>${catalina.base}/conf/web.xml</WatchedResource>
    <!-- Sin persistencia de sesiones entre reinicios (el disco del contenedor no se conserva) -->
    <Manager pathname="" />
    <!-- JSF en Production solo en la imagen: override="false" gana sobre el Development del web.xml -->
    <Parameter name="javax.faces.PROJECT_STAGE" value="Production" override="false" />
</Context>
```

### 4.4 `despliegue/tomee/ROOT/index.html` (redirige `/` al contexto de la aplicación)

```html
<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="UTF-8">
<meta http-equiv="refresh" content="0; url=/<contexto-web>/">
<title><Proyecto></title>
</head>
<body><p>Redirigiendo a <a href="/<contexto-web>/"><Proyecto></a>...</p></body>
</html>
```

### 4.5 `despliegue/tomee/system.properties.extra` (solo si hay cliente con EJB remoto)

```properties
# EJB remotos por HTTP en /tomee/ejb
tomee.remote.support = true
# Lista blanca de clases que se deserializan en las llamadas remotas; todo lo demas se rechaza
tomee.serialization.class.blacklist = -
tomee.serialization.class.whitelist = org.apache.openejb.,<paquete.base>.,java.lang.,java.util.,java.math.,java.sql.,java.net.URI,[Ljava.lang.,[Ljava.net.URI,[L<paquete.base>.,org.eclipse.persistence.indirection.,org.eclipse.persistence.internal.indirection.
```

Si al llamar desde el cliente aparece `... is not whitelisted as deserialisable`, agregar a la lista el prefijo de la
clase que dice el mensaje.

### 4.6 `render.yaml` (Blueprint, en la raíz)

```yaml
services:
  - type: web
    name: <servicio>              # da la URL https://<servicio>.onrender.com
    runtime: docker
    plan: free
    region: virginia              # la misma region que la base (AWS us-east-1)
    dockerfilePath: ./Dockerfile
    dockerContext: .
    autoDeploy: true
    healthCheckPath: /<contexto-web>/<ruta-que-responda-200-sin-login>
    envVars:
      - key: DB_HOST
        sync: false               # Render la pide al crear el Blueprint; no queda en el repositorio
      - key: DB_PORT
        value: "4000"
      - key: DB_NAME
        value: <base>
      - key: DB_USER
        sync: false
      - key: DB_PASSWORD
        sync: false
      - key: DB_SSL_MODE
        value: REQUIRED
      - key: JAVA_XMX
        value: 320m
```

### 4.7 `.dockerignore` y `.gitattributes`

```
# .dockerignore: lo que no hace falta para compilar dentro de Docker
.git
.idea
.run
.settings
**/target
notas
docs
scripts
*.md
!README.md
```

```
# .gitattributes: los scripts de shell deben ir con LF o fallan en Linux
*.sh text eol=lf
Dockerfile text eol=lf
```

---

## 5. Cambios de código para que el EAR funcione en TomEE

Todos estos cambios siguieron siendo compatibles con Payara.

| Síntoma en TomEE | Causa | Solución |
|---|---|---|
| El correo cae en "modo simulado" aunque `mail/...` está configurado | TomEE publica el recurso como `openejb:Resource/mail/<nombre>`, no como `mail/<nombre>` | Hacer el lookup con los dos nombres (ver abajo) |
| Tildes rotas en los formularios | En Payara lo resolvía `glassfish-web.xml`, que TomEE ignora | En `web.xml` (Servlet 4.0): `<request-character-encoding>UTF-8</request-character-encoding>` y `<response-character-encoding>UTF-8</response-character-encoding>` |
| El cliente de escritorio no conecta | Usaba IIOP (`localhost:3700`), que es exclusivo de Payara | Cliente `org.apache.tomee:openejb-client:8.0.16`, `RemoteInitialContextFactory` y `PROVIDER_URL=https://<servidor>/tomee/ejb`; nombre JNDI sin `java:` (ver abajo) |
| El JSON del API cambia de formato (`9.8E7` en lugar de `98000000.0`) | TomEE usa Johnzon y Payara usa Yasson | Diferencia conocida; no se corrigió |

Lookup del correo que funciona en los dos servidores:
```java
private Session buscarSesion() {
    String[] nombres = { "mail/<nombre>", "openejb:Resource/mail/<nombre>" };
    for (String nombre : nombres) {
        try {
            Object recurso = new InitialContext().lookup(nombre);
            if (recurso instanceof Session) { return (Session) recurso; }
        } catch (NamingException e) { /* se prueba el siguiente */ }
    }
    return null;   // sin sesion: simular el envio (escribirlo en el log)
}
```

Cliente remoto contra TomEE:
```java
Properties p = new Properties();
p.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.RemoteInitialContextFactory");
p.put(Context.PROVIDER_URL, "https://<servicio>.onrender.com/tomee/ejb");
Object ejb = new InitialContext(p).lookup(
    "global/<ear>/<modulo-ejb>/<Bean>!<paquete>.<InterfazRemota>");   // el java:global sin "java:"
```
En uniMotor, el servidor se elige con `-Dunimotor.servidor=<url>|iiop`, para usar el mismo cliente con Payara y con TomEE.

---

## 6. Probar en local antes de subir

```bash
docker build -t <imagen> .
# Pasar las variables con un archivo SIN retornos de carro (CR). En Git Bash no funciona --env-file <(...)
tr -d '\r' < notas/.env > /tmp/demo.env
docker run -d --name demo --memory=512m --memory-swap=512m -p 10000:10000 -e PORT=10000 \
  --env-file /tmp/demo.env <imagen>
docker logs -f demo                 # esperar "Server startup in [...] milliseconds"
docker stats --no-stream demo       # memoria: debe quedar bastante por debajo de 512 MB
```

- Para un MySQL local sin SSL: `-e DB_HOST=host.docker.internal -e DB_SSL_MODE=DISABLED`.
- Para ver correos en local: Mailpit (`-e MAIL_HOST=host.docker.internal -e MAIL_PORT=1025 -e MAIL_STARTTLS=false`).
- uniMotor en 512 MB: arranca en ~60 s con la base local, usa ~330 MB tras el arranque y ~380 MB tras recorrer toda la demo.
- Si la aplicación está colgada, sacar un volcado de hilos sin `jcmd`, que no viene en la imagen JRE:
  `docker exec demo sh -c 'kill -3 $(pgrep java)'` y mirar `docker logs demo`.

---

## 7. Problemas encontrados y soluciones

### 7.1 La primera carga de datos supera el timeout de transacción
Contra TiDB en Virginia, desde Colombia, cada inserción tarda ~200 ms. Los datos de demostración (unas 5000 filas en
una sola transacción del `@Startup` `@Singleton`) tardaron **~18 minutos**. TomEE corta las transacciones a los
**10 minutos**, y el log mostró `Transaction has timed out`, `SetupEJB` falló y el EAR no se desplegó. TomEE igual
imprime `Server startup`, así que hay que buscar `GRAVE`/`SEVERE` en el log.
**Solución:** `<TransactionManager>` con `defaultTransactionTimeout` configurable (`TX_TIMEOUT`, 1 hora por
defecto). Además, poner la base y Render en la misma región. Otra opción: dejar que la carga inicial ocurra en local
contra la base de la nube, como pasó en uniMotor. Render encontró la base llena y arrancó en segundos.

### 7.2 El EAR no se despliega
TomEE no despliega EAR desde `webapps/`, solo WAR. **Solución:** `apps/` + `<Deployments dir="apps"/>` en `tomee.xml`.
Si queda una copia explotada en `webapps/`, sale `DuplicateDeploymentIdException`: borrarla.

### 7.3 El correo se cuelga ~2 minutos y falla en Render
El plan gratuito bloquea el SMTP saliente. Con los timeouts de 10 s y los reintentos, cada envío tardaba ~135 s y la
página mostraba "No se pudo enviar el correo". **Solución en uniMotor:** no definir `MAIL_HOST` en Render; la
aplicación simula el envío y lo escribe en el log. Alternativas: SMTP en el puerto 2525 (Mailtrap), una API HTTP de
correo o un plan de pago.

### 7.4 Errores de deserialización en el EJB remoto
Salió `[Ljava.net.URI; is not whitelisted` y luego `BeanContext$BusinessRemoteHome is not whitelisted`.
**Solución:** la lista blanca de la sección 4.5, con `org.apache.openejb.` y los arreglos (`[L...`).

### 7.5 Detalles de Windows y Git Bash
- `docker exec` con rutas o `sed` se rompe por la conversión de rutas de MSYS: anteponer `MSYS_NO_PATHCONV=1`.
- `--env-file <(...)` falla ("cannot find the path"): usar un archivo temporal.
- Los `.sh` con CRLF fallan dentro del contenedor: `.gitattributes` con `eol=lf` y `sed -i 's/\r$//'` en el Dockerfile.

### 7.6 El servicio dormido
Tras 15 minutos sin visitas, el cliente de escritorio no conecta hasta que el servicio despierta. Abrir antes la web
y esperar ~1 minuto.

---

## 8. Lista de verificación final

- [ ] `https://<servicio>.onrender.com/` redirige a la aplicación.
- [ ] Las páginas principales cargan, el inicio de sesión funciona y una URL inexistente da la página 404 propia.
- [ ] El API responde (`curl https://<servicio>.onrender.com/<contexto>/api/...`).
- [ ] El cliente de escritorio conecta a `https://<servicio>.onrender.com/tomee/ejb`, si lo hay.
- [ ] Los usuarios de prueba del README entran. Si se probó la recuperación de contraseña contra la base de la nube,
      la contraseña quedó cambiada: restaurarla.
- [ ] No quedaron datos de prueba en la base de la nube.
- [ ] El enlace está en el README de `main` y en *About → Website*.
- [ ] Las variables que se quitaron del `render.yaml` también se borraron en Render → *Environment*.
