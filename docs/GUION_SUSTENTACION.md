# Guion de sustentación de uniMotor

Duración prevista: 20 a 25 minutos (más preguntas). Los tiempos son una guía; si el jurado
interrumpe, se recorta la demo, no la arquitectura.

| Bloque | Tema | Tiempo |
|---|---|---|
| 1 | Presentación | 1 min |
| 2 | Arquitectura | 4 min |
| 3 | Modelo de datos | 3 min |
| 4 | Requisitos especiales del enunciado | 4 min |
| 5 | Funcionalidades extra | 2 min |
| 6 | Demo en vivo | 8 a 10 min |
| 7 | Calidad | 2 min |
| 8 | Preguntas del jurado | el resto |
| 9 | Checklist previa | antes de empezar |

---

## 1. Presentación (1 min)

**Qué decir:** "uniMotor es un portal de compra y venta de vehículos (carros, motos y camionetas).
Resuelve el contacto entre vendedor y comprador: el vendedor publica con fotos, el comprador busca,
filtra, pregunta y guarda favoritos, y el vendedor recibe un aviso por correo. Tiene dos clientes sobre
la misma capa de negocio: una web para compradores y vendedores y una aplicación de escritorio para el
administrador, con estadísticas."

- Curso: Análisis de Algoritmos II, Universidad del Quindío.
- Tres entregas: persistencia, negocio con escritorio, y presentación web.

---

## 2. Arquitectura (4 min)

### 2.1 Módulos Maven

`groupId` único: `co.edu.uniquindio.unimotor` (requisito del enunciado).

| Módulo | Responsabilidad |
|---|---|
| `proyecto-persistencia` | Entidades JPA, enumeraciones, `NamedQuery` y DTO de estadísticas (`EstadisticaDTO`, `PuntoDTO`) |
| `proyecto-negocio` | EJB con las reglas de negocio, correo, ciudades, `SetupEJB` y generador de datos demo; excepciones propias |
| `proyecto-web` | JSF + PrimeFaces (cliente), API REST (`web.rest`), servlet de fotos, filtro de seguridad |
| `proyecto-escritorio` | JavaFX del administrador; habla con el EJB remoto |
| `proyecto-ear` | Empaqueta `proyecto-negocio` (EJB) y `proyecto-web` (WAR) en un solo EAR |
| `proyecto-pruebas` | JUnit 4 + Arquillian + DBUnit con datasets JSON |

### 2.2 Capas y despliegue

```
 Navegador (cliente)                    App de escritorio (administrador)
        |  HTTP :8080                            |  IIOP / JNDI remoto :3700
        v                                        v
+------------------ Payara 5.2022.5 (proyecto-ear.ear) -------------------+
|  proyecto-web.war                      |                                 |
|   JSF + PrimeFaces  (web.beans)        |                                 |
|   API REST JAX-RS + JSON-B (web.rest)  |                                 |
|   FiltroSeguridad, FotoServlet         |                                 |
|        |  @EJB (mismo proceso)         |                                 |
|        v                               v                                 |
|  proyecto-negocio.jar (EJB)                                              |
|   ClienteEJB VehiculoEJB PreguntaEJB VentaEJB CalificacionEJB            |
|   CatalogoEJB CiudadEJB CorreoEJB  AdministradorEJB (@Remote)            |
|        |  JPA / EclipseLink  (proyecto-persistencia)                     |
+--------|-----------------------------------------------------------------+
         |  jdbc/prueba -> mysql_unimotor_pool       |  SMTP mail/unimotor
         v                                           v
   MySQL 8 (Docker): BD unimotor            Mailpit (Docker) :1025 / :8025
                     BD prueba (pruebas)    api-colombia.com (ciudades)
```

### 2.3 Tecnologías

| Capa | Tecnología y versión |
|---|---|
| Lenguaje | Java 8 (JDK Zulu 8 con JavaFX) |
| Servidor | Payara Server 5.2022.5 (Java EE 8) |
| Persistencia | JPA con EclipseLink, MySQL 8.0 (Connector/J 8.0.33) |
| Negocio | EJB `@Stateless` + `@LocalBean`; `AdministradorEJB` con interfaz `@Remote`; `SetupEJB` `@Singleton @Startup` |
| Web | JSF 2.3 con CDI (`ConfiguracionJsf`), PrimeFaces 8.0, tema `nova-light` |
| Escritorio | JavaFX 8, patrón Delegado (`AdministradorDelegado`) con JNDI remoto |
| API | JAX-RS (`ApiAplicacion`, `VehiculoRest`, `CatalogoRest`) con JSON-B |
| Correo | JavaMail, recurso `mail/unimotor` (Mailpit en desarrollo) |
| Pruebas | JUnit 4, Arquillian (GlassFish embebido), DBUnit |

Idea clave para decir: **toda la lógica vive en los EJB**; web, escritorio y REST son solo
presentación. Por eso las reglas (por ejemplo "solo califica quien compró") valen igual en todos lados.

---

## 3. Modelo de datos (3 min)

Diagrama: `docs/diagrama-er.png` (se genera del esquema real con PlantUML, `docs/diagrama-er.puml`).
Mostrarlo en pantalla durante este bloque.

| Entidad | Para qué sirve |
|---|---|
| `Usuario` | Base con herencia JOINED (`@Inheritance(strategy = JOINED)`): email único y contraseña |
| `Administrador`, `Cliente` | Subclases. `Cliente` tiene teléfonos (`@ElementCollection`), ciudad, favoritos, compras y vehículos |
| `Vehiculo` | Entidad central: tipo, condición, combustible, transmisión, precio (`Double`), placa única, fotos, características, `numero_busquedas`, `fecha_publicacion`, `activo` |
| `Ciudad`, `Modelo`, `Caracteristica` | Catálogos. `Ciudad` lleva `departamento` porque hay nombres repetidos |
| `Pregunta` | Id propio, texto, respuesta y fecha de respuesta (nullable) |
| `Venta`, `Calificacion` | Las dos funcionalidades extra |

- **Herencia de usuarios:** un solo login para ambos roles; la separación se hace por tipo
  (`autenticarCliente` rechaza administradores y `autenticarAdministrador` rechaza clientes).
- **Relaciones clave:** `Vehiculo` es el lado dueño de `caracteristica_vehiculo` (muchos a muchos);
  favoritos es otra tabla de unión (`favorito`); `Venta` es uno a uno con `Vehiculo`.
- **NamedQueries:** una por necesidad de negocio, con nombre `<Entidad>_<CONSTANTE>`
  (por ejemplo `Pregunta.PREGUNTAS_POR_VEHICULO`) y siempre usadas por constante. Hay búsqueda
  combinada con filtros opcionales `(:p IS NULL OR ...)`, `TOP_BUSCADOS`, preguntas recibidas,
  y consultas con `JOIN`, `IN`, `DISTINCT`, `GROUP BY`, `COUNT` y `AVG`.
- **DTO de estadísticas:** las consultas de los gráficos devuelven `EstadisticaDTO(etiqueta, [serie], valor)`
  y `PuntoDTO(x, y, [tamaño])` mediante expresiones constructoras JPQL; son serializables y viajan por IIOP al escritorio.

---

## 4. Requisitos especiales del enunciado (4 min)

El enunciado original (PDF del curso) no está en el repositorio; la lista sale de las notas del proyecto.

| Requisito | Dónde está en el código | Cómo funciona |
|---|---|---|
| Correo al vendedor al recibir una pregunta | `PreguntaEJB.hacerPregunta` + `PlantillasCorreo.preguntaRecibida` + `CorreoEJB.enviarEmail` | Persiste la pregunta, hace flush y avisa al dueño con datos del cliente y del vehículo. Si el correo falla, la pregunta se conserva |
| Ciudades desde una API de terceros | `CiudadEJB.consultarCiudadesColombia` y `cargarCiudades` | Consulta api-colombia.com (timeout 10 s) con `HttpURLConnection` y JSON-P; llamado desde `SetupEJB`. Respaldo: `ciudades-colombia.json`. Quedan 1123 municipios |
| API REST propia en JSON | `web.rest`: `ApiAplicacion` (`/api`), `VehiculoRest`, `CatalogoRest` | `GET /vehiculos`, `/vehiculos/{id}`, `/vehiculos/top?condicion=`, `/ciudades`, `/caracteristicas`, `/modelos`, `/marcas`. Solo lectura, con DTO |
| Compartir en Facebook y Twitter | `DetalleVehiculoBean.getUrlFacebook` y `getUrlTwitter`; botones en `detalleVehiculo.xhtml` | Enlaces a `sharer.php?u=` y `intent/tweet?url=&text=`, más etiquetas Open Graph y `twitter:card` en el `head` |
| Recuperar contraseña | `ClienteEJB.recuperarPassword` (web: `RecuperarPasswordBean.recuperar`); administrador: `AdministradorEJB.recuperarPasswordAdministrador` | Genera una contraseña temporal de 8 caracteres y la envía por correo. Si el envío falla, hace rollback |
| Top 10 más buscados (nuevos y usados) | `VehiculoEJB.top10MasBuscados(Condicion)`; `InicioBean.getTopNuevos` y `getTopUsados` | Consulta `TOP_BUSCADOS` con `setMaxResults(10)`. El contador `numero_busquedas` sube con la búsqueda y con el detalle |
| Filtros por características | `VehiculoEJB.buscarVehiculos` y `filtrarPorCaracteristicas`; `ResultadosBean.aplicarFiltros` | Búsqueda por tipo, marca, precio y año; luego se refina por características (deben cumplirse todas), combustible, transmisión, condición y kilometraje |
| Al menos 10 gráficos del administrador | `AdministradorEJBRemote` (17 estadísticas) + `FabricaGraficos` (escritorio) | 17 gráficos de 9 tipos: pastel, barras, barras horizontales, barras apiladas, líneas, área, dispersión y burbujas |
| `groupId` único | `pom.xml` padre y de cada módulo | `co.edu.uniquindio.unimotor` |

---

## 5. Funcionalidades extra (2 min)

| Extra | Clase y método | Regla |
|---|---|---|
| **Venta** | `VentaEJB.registrarVenta(codigoVehiculo, emailVendedor, emailComprador)` | Solo la registra el dueño; el comprador es otro cliente existente; un vehículo se vende una sola vez; el vehículo queda inactivo y sale de búsquedas y top 10; se avisa al comprador por correo |
| **Calificación del vendedor** | `CalificacionEJB.calificarVendedor(...)`, `promedioVendedor`, `puedeCalificar` | 1 a 5 estrellas; **solo califica quien le compró al vendedor**; una vez por vendedor; no se autocalifica |

**Por qué no son triviales:**

- La venta toca varias tablas y reglas a la vez (dueño, comprador, estado, correo). Además la base **no tiene
  UNIQUE** en `VENTA.codigo_vehiculo`, así que la unicidad se valida en el negocio (con `getCodigo_venta()` y con un `count`).
- La calificación depende de la venta: se consulta `Venta.COMPRAS_A_VENDEDOR`. Es una regla que une dos extras.
- La interfaz muestra o esconde el botón "Calificar" con `puedeCalificar` (página "Mis compras", `ComprasBean`).
- El generador de datos demo respeta estas mismas reglas.

---

## 6. Demo en vivo (8 a 10 min)

Antes de empezar: abrir las pestañas de la sección 9. Usuarios de prueba:

| Rol | Usuario | Contraseña |
|---|---|---|
| Cliente | `usuario1@servidor.com` | `12345` |
| Cliente | `usuario2@servidor.com` (ya tiene una compra) | `12345` |
| Cliente | `usuario3@servidor.com` | `12345` |
| Administrador | `cristia@proyecto.com` (solo por la app de escritorio) | `123` |

Mailpit (bandeja de correos): `http://localhost:8025`.

| # | Min | Paso | Qué decir (una línea) | Plan B |
|---|---|---|---|---|
| 1 | 0:45 | Abrir `http://localhost:8080/proyecto-web/`: buscador y los dos top 10 (nuevos y usados) | "El inicio muestra los 10 más buscados de cada condición; es una consulta con `setMaxResults(10)`." | Si las fotos no cargan, revisar la carpeta de fotos y recargar sin caché |
| 2 | 1:00 | Buscar (por ejemplo carro, marca Mazda, rango de precio) y refinar por características, combustible y orden | "La búsqueda suma una búsqueda a cada vehículo; filtrar y ordenar trabajan en memoria y no suman." | Usar la URL directa `resultados.xhtml?tipo=CARRO&marca=Mazda` |
| 3 | 1:00 | Abrir un detalle: galería, ficha técnica, vendedor, botones de compartir (mostrar la URL de Facebook y Twitter) | "Compartir son enlaces a los sharers de cada red, con etiquetas Open Graph para la vista previa." | Si no hay internet, basta mostrar el enlace; no hace falta abrirlo |
| 4 | 1:00 | Registrar un cliente nuevo: nombre, correo, contraseña con confirmación, ciudad con autocompletado, teléfonos | "Las ciudades vienen de api-colombia.com y se guardaron al desplegar; al registrarse la sesión queda iniciada." | Con internet caído no afecta: las ciudades ya están en la base (respaldo `ciudades-colombia.json`) |
| 5 | 1:30 | Publicar un vehículo con 2 o 3 fotos y características | "Las fotos se validan en el servidor (tipo, tamaño, firma del archivo) y se guardan con nombre UUID." | Tener 2 fotos JPG pequeñas en el escritorio; si falla la subida, editar un vehículo existente |
| 6 | 1:30 | Cerrar sesión, entrar como `usuario1@servidor.com`, abrir el vehículo publicado y preguntar. Abrir Mailpit y mostrar el correo al vendedor | "`hacerPregunta` guarda la pregunta y el correo llega al vendedor con los datos del cliente y del vehículo." | Si el correo no llega, mostrar Mailpit con correos anteriores o el log de Payara (modo simulado) |
| 7 | 1:00 | Entrar como el dueño: Preguntas recibidas, responder; en el detalle se ve la respuesta | "Solo el dueño puede responder y una sola vez." | n/a |
| 8 | 0:30 | Con el otro usuario, marcar el vehículo como favorito y verlo en Favoritos | "Favoritos es una relación muchos a muchos; no se puede marcar el vehículo propio." | n/a |
| 9 | 1:00 | Como dueño, en Mis vehículos pulsar "Marcar como vendido" con el correo del comprador; mostrar el correo al comprador en Mailpit | "`registrarVenta` deja el vehículo inactivo y avisa al comprador con la invitación a calificar." | Si el correo falla, la venta queda registrada igual: mostrar Mailpit o el log |
| 10 | 0:45 | Como comprador, ir a Mis compras y calificar al vendedor | "Solo puede calificar quien compró, y una sola vez; el botón sale según `puedeCalificar`." | n/a |
| 11 | 1:00 | En otra pestaña: `http://localhost:8080/proyecto-web/api/vehiculos?limite=5`, `/api/vehiculos/top?condicion=NUEVO` y `/api/ciudades?nombre=armenia` | "La API es de solo lectura, en JSON, con DTO: no expone entidades ni datos personales." | Con `curl` desde la terminal si el navegador falla |
| 12 | 1:30 | App de escritorio (`co.edu.uniquindio.unimotor.escritorio.app.Aplicacion`): iniciar sesión como administrador, recorrer varios gráficos y pulsar Refrescar | "El escritorio llama al EJB remoto por JNDI; los gráficos reflejan la actividad que acabamos de crear (vehículos por tipo, ventas por mes, top buscados)." | Si el escritorio no conecta, tener capturas de los gráficos listas |
| 13 | 0:45 | Cerrar sesión en la web y usar "Recuperar contraseña" con un cliente; mostrar la contraseña temporal en Mailpit | "Se genera una temporal de 8 caracteres y se envía por correo; la respuesta de la web es siempre la misma para no revelar qué correos existen." | Usar un cliente de prueba que no se vaya a usar después (la contraseña cambia) |

Sugerencias de ritmo:

- Preparar de antemano el vehículo, las fotos y el correo del comprador para no escribir de más.
- Si el tiempo se acaba, saltar el paso 8 (favoritos) y el 13 (recuperar), que se pueden contar sin mostrar.
- Tras la demo, la base queda modificada (la recuperación cambia una contraseña, la venta y las preguntas
  se guardan). Para repetir la demo desde cero hay que recrear la base `unimotor` (ver sección 9).

---

## 7. Calidad (2 min)

- **Pruebas Arquillian: 367, 0 fallos** (aprox. 11 minutos). Ejecutan en un GlassFish embebido con puertos
  propios, sobre la base `prueba`; cada prueba carga datasets JSON (14 archivos) con DBUnit y hace rollback.

| Clase | Pruebas | Qué cubre |
|---|---|---|
| `EntidadTest` | 14 | CRUD de `Administrador` y `Caracteristica` y consultas |
| `UsuarioCatalogoTest` | 12 | CRUD de `Cliente`, `Ciudad` y `Modelo` |
| `VehiculoTest` | 17 | CRUD de `Vehiculo`, `Pregunta`, `Venta` y `Calificacion` y sus restricciones |
| `ConsultasTest` | 48 | Una prueba por `NamedQuery` (incluidas las 17 estadísticas) |
| `ClienteNegocioTest` | 77 | API de negocio del cliente |
| `ExtrasNegocioTest` | 38 | Venta y calificación |
| `AdministradorNegocioTest` | 32 | Login, recuperar contraseña y las 17 estadísticas |
| `ClienteValidacionTest` | 49 | Validaciones de `ClienteEJB` |
| `VehiculoPreguntaValidacionTest` | 44 | Validaciones de vehículos y preguntas, límites |
| `ServiciosNegocioTest` | 36 | Correo, plantillas, ciudades y casos límite |

- En las pruebas el correo funciona en modo simulado (el GlassFish embebido no tiene `mail/unimotor`).
- Se comprobó que las pruebas detectan errores alterando el código (mutantes: kilómetros negativos,
  contraseña de 3 caracteres, estrellas negativas, destinatario de correo sin validar).
- **Javadoc:** entidades, DTO y pruebas documentadas; se genera con `mvn javadoc:javadoc`.
- **Validaciones:** en el negocio (excepciones propias `InformacionInvalidaException`, `ElementoRepetidoException`,
  `ElementoNoEncontradoException`, `AutenticacionException`) y en la web (`required`, formato de correo,
  validador de placa, tamaño y tipo de las fotos).
- **Seguridad:**
  - `FiltroSeguridad` (`@WebFilter("/usuario/*")`) manda al login si no hay sesión.
  - Tras el login solo se vuelve a rutas seguras (`SeguridadBean.esRutaSegura`), para evitar redirecciones abiertas.
  - `changeSessionId()` al iniciar sesión y al registrarse (fijación de sesión).
  - La API REST no expone datos personales (vendedor como "Nombre I.", de la placa solo la terminación).

---

## 8. Preguntas probables del jurado

| Pregunta | Respuesta corta |
|---|---|
| ¿Por qué EJB `@Stateless` y `@LocalBean`? | Las operaciones no guardan estado entre llamadas; el contenedor las agrupa en un pool y les da transacciones. `@LocalBean` evita crear una interfaz local por cada EJB, porque la web está en el mismo EAR. Solo `AdministradorEJB` tiene interfaz `@Remote`, porque el escritorio es un proceso aparte. |
| ¿Cómo se evita el doble conteo de búsquedas? | `ResultadosBean` llama a `buscarVehiculos` una sola vez, en `f:viewAction` con `onPostback="false"`. Filtrar, ordenar y paginar trabajan en memoria sobre esa lista. `DetalleVehiculoBean.inicializar` usa el mismo mecanismo con `registrarVisita`, así que las acciones AJAX del detalle (preguntar, favorito) no suman. |
| ¿Por qué la web JSF no consume la API REST? | Los beans llaman a los EJB dentro del mismo proceso: es más simple, más rápido y transaccional. La API existe para clientes externos (otra app, un script, un tercero). |
| ¿Cómo se protege `/usuario/*`? | `FiltroSeguridad`, un `@WebFilter("/usuario/*")`, revisa el cliente en `SeguridadBean` y redirige al login con la ruta pedida. Esa ruta solo se acepta de vuelta si pasa `esRutaSegura`. El administrador no entra a la web: va por el escritorio. |
| ¿Cómo se suben y sirven las fotos? | `p:fileUpload` múltiple; el servidor valida tipo, extensión, tamaño (5 MB) y primeros bytes, y guarda con nombre UUID en una carpeta del servidor (`ConfiguracionFotos`, por defecto `${user.home}/unimotor/fotos`, configurable). `FotoServlet` las sirve en `/fotos/*` con caché. En la base solo se guarda el nombre del archivo. |
| ¿Por qué DTO en la API? | Para no serializar entidades JPA: evita ciclos y `LazyInitializationException`, y permite ocultar datos personales (`ConversorDTO` lee las relaciones perezosas dentro de la petición y devuelve DTO planos). |
| ¿Cómo manejan transacciones y correo? | `CorreoEJB` es `@TransactionAttribute(NOT_SUPPORTED)`: si el correo falla, no deshace la transacción del llamador y la pregunta o la venta se conservan. En cambio `recuperarPassword` sí debe fallar si no se puede enviar: `EnvioCorreoException` es `@ApplicationException(rollback = true)` y la contraseña no cambia. |
| ¿Cómo se generan los datos de demostración? | `SetupEJB` (`@Singleton @Startup`) llama a `GeneradorDatosDemo` si la base no tiene usuarios. Usa un `Random` con semilla fija (2026) y listas reales (`CatalogoDemo`), así que son los mismos en cada despliegue y cumplen las reglas del negocio. Crea 60 clientes, 400 vehículos, 800 preguntas, 100 ventas, 60 calificaciones y 150 favoritos. |
| ¿Cómo se recuperan las contraseñas sin guardarlas en claro por correo? | Hoy se envía una temporal de 8 caracteres (la contraseña anterior no se revela). Aun así las contraseñas están guardadas en texto plano; ver la última pregunta. |
| ¿Qué pasa si la API de ciudades no responde? | `CiudadEJB` tiene un timeout de 10 s y cae al respaldo `ciudades-colombia.json`. Además solo carga si la tabla está vacía. |
| ¿Por qué el escritorio tarda en fallar si Payara está apagado? | Se resolvió: `AdministradorDelegado` comprueba primero el puerto IIOP (3700, 3 s) y lanza `ErrorConexionException`; la ventana muestra un aviso. |
| ¿Qué cambiarían en producción? | Contraseñas con hash (por ejemplo bcrypt o PBKDF2) en vez de texto plano; HTTPS; correo real (Gmail u otro SMTP, con contraseña de aplicación) en vez de Mailpit; `PROJECT_STAGE=Production`; credenciales de base y pool fuera del código; fotos en almacenamiento externo o CDN; `schema-generation` con migraciones (Flyway) en lugar de `create`; paginación en la API siempre. |

---

## 9. Checklist previa a la sustentación

Hacerla al menos 30 minutos antes.

- [ ] Docker Desktop abierto; contenedores `unimotor-mysql` (puerto 3306) y `unimotor-mailpit` (puertos 1025 y 8025) arriba.
- [ ] Payara iniciado (`scripts\iniciar-entorno.bat`) con el EAR desplegado (`scripts\desplegar.bat`); `asadmin list-applications` muestra `proyecto-ear`.
- [ ] Base `unimotor` recreada con datos demo, para partir limpio (borra lo que dejó el ensayo):
  `docker exec unimotor-mysql mysql -uroot -p12345 -e "DROP DATABASE unimotor; CREATE DATABASE unimotor CHARACTER SET utf8mb4;"`
  y redesplegar (el arranque tarda unos segundos más porque genera los datos).
- [ ] Carpeta de fotos en su sitio (`%USERPROFILE%\unimotor\fotos` con las 50 fotos demo); quitar las fotos subidas en ensayos.
- [ ] Recurso `mail/unimotor` creado en Payara (si no existe, el correo funciona en modo simulado: solo se ve en el log).
- [ ] Mailpit vacío (recrear el contenedor o borrar los mensajes) para que los correos de la demo se distingan.
- [ ] App de escritorio compilada (`mvn clean install -DskipTests` hecho tras el último cambio) y lista para ejecutar la configuración **Escritorio (Administrador)** de IntelliJ con el JDK `jdk8-fx`.
- [ ] Navegador con pestañas preparadas: inicio, Mailpit (`http://localhost:8025`), API (`/proyecto-web/api/vehiculos`), `docs/diagrama-er.png`.
- [ ] Aviso de contraseña filtrada de Chrome desactivado (Configuración, Autocompletar y contraseñas, Gestor de contraseñas), porque los usuarios demo usan `12345` y el diálogo estorba al iniciar sesión.
- [ ] Dos o tres fotos JPG pequeñas en el escritorio para publicar el vehículo.
- [ ] Probar una vez el recorrido completo, sabiendo que cambia datos (ver recrear la base arriba).
- [ ] Tener a mano capturas de los gráficos y de Mailpit por si algo falla en vivo.
