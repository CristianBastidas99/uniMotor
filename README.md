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

### 4. Aplicación de escritorio

Con Payara arriba y el EAR desplegado, ejecute la clase
`co.edu.uniquindio.unimotor.escritorio.app.Aplicacion` del módulo `proyecto-escritorio`. En IntelliJ
use la configuración **Escritorio (Administrador)** (`.run/`), que incluye las dependencias `provided`
necesarias para el cliente remoto.

## Pruebas

```bat
cd proyecto-pruebas
mvn test
```

- La suite tiene 85 pruebas: CRUD de las 10 entidades y una prueba por cada `NamedQuery`
  (`NegocioTest`, de la capa de negocio, está marcada `@Ignore` hasta la entrega 2).
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
- **Entrega 2 (negocio y escritorio)**: en curso.
- **Entrega 3 (presentación web)**: en curso.
