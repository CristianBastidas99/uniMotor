# uniMotor

Portal de compra y venta de vehículos desarrollado para el curso **Análisis de Algoritmos II** de la
Universidad del Quindío. Tiene dos clientes sobre la misma capa de negocio:

- un **cliente web** (JSF + PrimeFaces) para compradores y vendedores, y
- una **aplicación de escritorio** (JavaFX) para el administrador, que se conecta de forma remota
  al EJB desplegado en el servidor.

## Arquitectura

Proyecto Maven multimódulo (Java 8, Java EE 8). `groupId`: `co.edu.uniquindio.unimotor`.

| Módulo | Contenido | Paquete base |
|---|---|---|
| `proyecto-persistencia` | Entidades JPA, enumeraciones, `NamedQuery` de negocio y de estadísticas, DTO (`EstadisticaDTO`, `PuntoDTO`) | `co.edu.uniquindio.unimotor.entidades`, `...dto` |
| `proyecto-negocio` | EJB de negocio (`AdministradorEJB` con su interfaz remota, `ClienteEJB`, `VehiculoEJB`), `SetupEJB` (datos iniciales) y excepciones | `co.edu.uniquindio.unimotor.ejb`, `...excepciones` |
| `proyecto-pruebas` | Pruebas de persistencia con JUnit 4, Arquillian (GlassFish embebido) y DBUnit con datasets JSON | `co.edu.uniquindio.unimotor.pruebas` |
| `proyecto-ear` | Empaqueta `proyecto-negocio` y `proyecto-web` en `proyecto-ear-*.ear` | n/a |
| `proyecto-web` | Aplicación web JSF + PrimeFaces (cliente) | n/a |
| `proyecto-escritorio` | Aplicación JavaFX del administrador | `co.edu.uniquindio.unimotor.escritorio` (`app`, `controlador`, `modelo`) |

## Modelo de datos

Entidades (`co.edu.uniquindio.unimotor.entidades`): `Usuario` (herencia JOINED), `Administrador`,
`Cliente`, `Ciudad`, `Modelo`, `Caracteristica`, `Vehiculo`, `Pregunta`, `Venta` y `Calificacion`,
más las enumeraciones `Condicion`, `TipoCombustible`, `TipoTrasmision` y `TipoVehiculo`.

Funcionalidades extra modeladas:

- **Venta**: registra la venta de un vehículo (comprador, fecha).
- **Calificación del vendedor**: un cliente califica de 1 a 5 estrellas (con comentario) a un vendedor;
  no se puede calificar dos veces al mismo vendedor.

![Diagrama E-R](docs/diagrama-er.png)

## Requisitos

| Herramienta | Versión |
|---|---|
| JDK con JavaFX | 8 (por ejemplo Azul Zulu 8 FX) |
| Maven | 3.9 |
| Payara Server | 5.2022.5 (rama 5, compatible con Java 8) |
| MySQL | 8 (se recomienda en Docker) |
| IntelliJ IDEA | opcional (el repositorio incluye configuraciones en `.run/`) |

Variables de entorno recomendadas: `JAVA_HOME` (JDK 8 con JavaFX), `MAVEN_HOME` y `PAYARA_HOME`
(carpeta de Payara), con sus `bin` en el `Path`.

## Puesta en marcha

### 1. Base de datos MySQL

```bat
docker run -d --name unimotor-mysql --restart unless-stopped -p 3306:3306 ^
  -e MYSQL_ROOT_PASSWORD=12345 -e MYSQL_DATABASE=unimotor ^
  -v unimotor-mysql-data:/var/lib/mysql mysql:8.0 ^
  --default-authentication-plugin=mysql_native_password ^
  --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci

docker exec unimotor-mysql mysql -uroot -p12345 -e "CREATE DATABASE IF NOT EXISTS prueba CHARACTER SET utf8mb4;"
```

`unimotor` la usa el servidor y `prueba` las pruebas automáticas. Las credenciales son solo para
desarrollo local.

### 2. Payara

1. Copie `mysql-connector-j-8.0.33.jar` en `%PAYARA_HOME%\glassfish\domains\domain1\lib`.
2. Haga que Payara use el JDK 8: agregue `set AS_JAVA=<ruta del JDK 8>` al final de
   `%PAYARA_HOME%\glassfish\config\asenv.bat`.
3. Inicie el dominio y cree el pool y el recurso JDBC (el nombre JNDI `jdbc/prueba` es el que
   declara el `persistence.xml`):

```bat
asadmin start-domain domain1

asadmin create-jdbc-connection-pool --datasourceclassname com.mysql.cj.jdbc.MysqlDataSource ^
  --restype javax.sql.DataSource ^
  --property "user=root:password=12345:serverName=localhost:portNumber=3306:databaseName=unimotor:useSSL=false:allowPublicKeyRetrieval=true:serverTimezone=America/Bogota:characterEncoding=UTF-8" ^
  mysql_unimotor_pool

asadmin create-jdbc-resource --connectionpoolid mysql_unimotor_pool jdbc/prueba

asadmin ping-connection-pool mysql_unimotor_pool
```

> Los datos de conexión van como propiedades separadas del `MysqlDataSource` (`serverName`, `portNumber`,
> `databaseName`...) en lugar de una URL, porque en `cmd` el `&` de la URL rompe el comando `asadmin`.

Con el contenedor y Payara ya configurados, el día a día se resume en:

```bat
scripts\iniciar-entorno.bat     :: inicia MySQL (Docker) y Payara
scripts\desplegar.bat           :: compila y despliega el EAR
scripts\detener-entorno.bat     :: detiene Payara y MySQL
```

### 3. Compilar y desplegar

Opción rápida: `scripts\desplegar.bat` (ejecuta `mvn -B clean install -DskipTests` y luego
`asadmin deploy --name proyecto-ear --force=true` sobre el `.ear`). Equivale a hacer a mano:

```bat
mvn clean install -DskipTests
asadmin deploy --name proyecto-ear --force=true proyecto-ear\target\proyecto-ear-0.0.1-SNAPSHOT.ear
```

- Aplicación web: <http://localhost:8080/proyecto-web/>
- Consola de administración de Payara: <http://localhost:4848>
- El servidor crea las tablas si no existen (`schema-generation = create`) pero no las actualiza:
  si cambia una entidad, recree la base `unimotor` y vuelva a desplegar. `SetupEJB` crea los usuarios
  iniciales (por ejemplo `cristia@proyecto.com` / `123`).
- **Datos de demostración:** si la base no tiene usuarios, `SetupEJB` llama además a `GeneradorDatosDemo`
  (paquete `co.edu.uniquindio.unimotor.ejb.demo`), que crea unos 60 clientes, 400 vehículos, 800 preguntas,
  100 ventas, 60 calificaciones y 150 favoritos. Los datos salen de listas reales (`CatalogoDemo`: nombres y
  apellidos colombianos, 24 ciudades, unos 60 modelos vendidos en Colombia), respetan las reglas del negocio y,
  como usan una semilla fija, son los mismos en cada despliegue. Todos los clientes tienen la contraseña `12345`.
- **Fotos de demostración:** `fotos-demo/` tiene 50 fotos con licencia libre de Wikimedia Commons (30 carros,
  12 motos y 8 camionetas). Cada vehículo recibe 5 fotos distintas de su tipo, repartidas de forma pareja
  (`RepartidorFotos`). Los autores y las licencias están en `fotos-demo/CREDITOS.md`.
- **Carpeta de fotos del servidor:** la web guarda y sirve las fotos de los vehículos desde una carpeta del
  servidor, por defecto `%USERPROFILE%\unimotor\fotos` (`${user.home}/unimotor/fotos`). Se cambia con la
  propiedad JVM `unimotor.fotos.carpeta` (`asadmin create-jvm-options "-Dunimotor.fotos.carpeta=C\:/ruta"` y
  reiniciar Payara) o con la variable de entorno `UNIMOTOR_FOTOS`. El WAR lleva las fotos de `fotos-demo/` en
  `WEB-INF/fotos-demo` y, al arrancar, las copia a esa carpeta sin sobrescribir las que ya existan. Se sirven en
  `http://localhost:8080/proyecto-web/fotos/<nombre>` (por ejemplo `carro-01.jpg`).

### Correo (JavaMail)

El envío de correos (recuperación de contraseña, avisos de preguntas) usa el recurso JNDI `mail/unimotor`.
En desarrollo se usa [Mailpit](https://github.com/axllent/mailpit) en Docker (bandeja en <http://localhost:8025>):

```bat
docker run -d --name unimotor-mailpit --restart unless-stopped -p 1025:1025 -p 8025:8025 axllent/mailpit
asadmin create-javamail-resource --mailhost localhost --mailuser unimotor --fromaddress no-responder@unimotor.local --property mail.smtp.port=1025 mail/unimotor
```

Para usar Gmail: `--mailhost smtp.gmail.com`, `--mailuser` con la cuenta, y las propiedades `mail.smtp.port=587`,
`mail.smtp.auth=true`, `mail.smtp.starttls.enable=true` y `mail.smtp.password` con una contraseña de aplicación.
En `asadmin`, los caracteres `:` y `=` dentro de los valores de `--property` se escapan con `\`.
Si el recurso no existe, el sistema funciona en modo simulado: el correo solo se registra en el log del servidor.

Las ciudades se cargan al desplegar desde api-colombia.com, con respaldo en `ciudades-colombia.json`.

### 4. Aplicación de escritorio

Con Payara arriba y el EAR desplegado, ejecute la clase
`co.edu.uniquindio.unimotor.escritorio.app.Aplicacion` del módulo `proyecto-escritorio`. En IntelliJ
use la configuración **Escritorio (Administrador)** (`.run/`), que incluye las dependencias `provided`
necesarias para el cliente remoto.

- Inicie sesión con un administrador (por ejemplo `cristia@proyecto.com` / `123`); los clientes no pueden entrar.
- "¿Olvidó su contraseña?" envía una contraseña temporal al correo del administrador.
- La ventana principal muestra 17 gráficos de estadísticas (pastel, barras, barras apiladas, líneas, áreas,
  dispersión y burbujas) con botones para refrescar los datos y cerrar sesión.
- Si Payara no está en ejecución, la aplicación lo indica con un mensaje de error.

## Pruebas

```bat
cd proyecto-pruebas
mvn test
```

- La suite tiene 367 pruebas (~11 min):
  - persistencia: CRUD de las 10 entidades y una prueba por cada `NamedQuery` (`EntidadTest`, `UsuarioCatalogoTest`,
    `VehiculoTest`, `ConsultasTest`);
  - negocio: `ClienteNegocioTest` (cliente: usuarios, vehículos, búsqueda, favoritos, preguntas, catálogos),
    `ExtrasNegocioTest` (venta y calificación del vendedor) y `AdministradorNegocioTest` (login, recuperar contraseña y estadísticas);
  - validaciones y servicios: `ClienteValidacionTest`, `VehiculoPreguntaValidacionTest` y `ServiciosNegocioTest`
    (correo, plantillas de correo, ciudades, casos límite de ventas y calificaciones).
- En las pruebas de negocio el correo funciona en **modo simulado** (el GlassFish embebido no tiene `mail/unimotor`).
- Cada prueba carga los datasets JSON de `src/test/resources` en la base **`prueba`** y hace rollback.
- Requieren el contenedor `unimotor-mysql` en ejecución. **No** hay que apagar Payara: el GlassFish
  embebido de Arquillian usa puertos propios (18080, 18181, 14848, etc., definidos en `domain-test.xml`
  y `arquillian.xml`).
- Configuración de DBUnit necesaria: `schema=prueba` en `arquillian.xml` y `databaseTerm=SCHEMA`
  en la URL JDBC de `sun-resources.xml`; sin ellas DBUnit ve las tablas de todas las bases y falla con
  `AmbiguousTableNameException`.
- Para una sola clase: `mvn test -Dtest=ConsultasTest`. No ejecute dos `mvn test` a la vez
  (comparten puertos y base).

## Documentación

- Javadoc: `mvn javadoc:javadoc` (la configuración está en el `pom.xml` padre).
- Diagrama entidad-relación: [`docs/diagrama-er.png`](docs/diagrama-er.png).

## Estado del proyecto

- **Entrega 1 (persistencia)**: completa: entidades, `NamedQuery`, datasets JSON y pruebas en verde.
- **Entrega 2 (negocio y escritorio)**: completa: EJB con todos los requisitos (incluidas las funcionalidades
  extra), correo, API de ciudades, aplicación de escritorio con 17 gráficos y 367 pruebas en verde.
- **Entrega 3 (presentación web)**: en curso. Hecha la configuración del módulo web (JSF 2.3 con CDI,
  PrimeFaces 8 con el tema `nova-light`, estilos propios y carpeta de fotos servida en `/fotos/*`) y la plantilla
  común con el menú que cambia según haya sesión, la estructura de páginas (públicas y `usuario/`) y las páginas
  de error (404, 500 y sesión expirada). Los clientes ya pueden registrarse (con las ciudades de la API y varios
  teléfonos), iniciar y cerrar sesión y recuperar la contraseña por correo; las páginas de `usuario/` exigen sesión.
  La página principal tiene el buscador (tipo, marca, rango de precio y de año) y los 10 vehículos más buscados
  nuevos y usados; los resultados se pueden refinar por características, combustible, transmisión, condición y
  kilometraje, ordenar y ver en cuadrícula o en lista. El detalle de cada vehículo muestra la galería de fotos, la
  ficha técnica, los datos del vendedor y las preguntas con sus respuestas; con sesión se puede preguntar (el
  vendedor recibe un correo) y guardar en favoritos, y cualquiera puede compartirlo en Facebook o Twitter.
  Para probar: `usuario1@servidor.com` / `12345` (los administradores entran solo por la aplicación de escritorio).
