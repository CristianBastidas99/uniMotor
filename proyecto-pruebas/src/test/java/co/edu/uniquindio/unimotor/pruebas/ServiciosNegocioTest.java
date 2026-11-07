package co.edu.uniquindio.unimotor.pruebas;

import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.EJB;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import co.edu.uniquindio.unimotor.ejb.AdministradorEJB;
import co.edu.uniquindio.unimotor.ejb.AdministradorEJBRemote;
import co.edu.uniquindio.unimotor.ejb.CalificacionEJB;
import co.edu.uniquindio.unimotor.ejb.CatalogoEJB;
import co.edu.uniquindio.unimotor.ejb.CiudadEJB;
import co.edu.uniquindio.unimotor.ejb.ClienteEJB;
import co.edu.uniquindio.unimotor.ejb.CorreoEJB;
import co.edu.uniquindio.unimotor.ejb.PlantillasCorreo;
import co.edu.uniquindio.unimotor.ejb.PreguntaEJB;
import co.edu.uniquindio.unimotor.ejb.VehiculoEJB;
import co.edu.uniquindio.unimotor.ejb.VentaEJB;
import co.edu.uniquindio.unimotor.entidades.Calificacion;
import co.edu.uniquindio.unimotor.entidades.Ciudad;
import co.edu.uniquindio.unimotor.entidades.Cliente;
import co.edu.uniquindio.unimotor.entidades.Modelo;
import co.edu.uniquindio.unimotor.entidades.Pregunta;
import co.edu.uniquindio.unimotor.entidades.Usuario;
import co.edu.uniquindio.unimotor.entidades.Vehiculo;
import co.edu.uniquindio.unimotor.entidades.Venta;
import co.edu.uniquindio.unimotor.excepciones.AutenticacionException;
import co.edu.uniquindio.unimotor.excepciones.ElementoNoEncontradoException;
import co.edu.uniquindio.unimotor.excepciones.InformacionInvalidaException;

/**
 * Pruebas in-container de la Fase 2.7 (cierre de huecos de cobertura) para los servicios de apoyo:
 * {@link CorreoEJB}, {@link PlantillasCorreo}, {@link CiudadEJB}, y los caminos menores de
 * {@link VentaEJB} y {@link CalificacionEJB}.
 * <p>
 * Cada prueba corre en una transaccion con rollback sobre los 14 datasets JSON de
 * src/test/resources. El GlassFish embebido no tiene el recurso JNDI {@code mail/unimotor}, por lo
 * que {@code enviarEmail} funciona siempre en modo simulado y devuelve {@code false}. Ninguna prueba
 * llama a la API externa de ciudades: {@code cargarCiudades} se prueba con la tabla ya poblada y el
 * archivo de respaldo se lee por reflexion. No se despliega {@code SetupEJB}.
 * <p>
 * Datos de los JSON que se usan: clientes 4 (usuario1@servidor.com, Hector Rodriguez, telefonos
 * Personal 3135638905 y Personal 2 3155679045), 5 (usuario2@servidor.com) y 6
 * (usuario3@servidor.com, Ruben zuluaga); vehiculo 1 (VEV592, Mazda 3, 2018, 26000000) y vehiculo
 * 2 (NCV903, Renoult 4, 1990, 6000000, dueno 6, vendido en la venta 1); ciudades 1 Armenia,
 * 2 Buenavista y 3 Pijao (todas de Quindio).
 */
@RunWith(Arquillian.class)
@UsingDataSet({ Datasets.MODELO, Datasets.ADMINISTRADOR, Datasets.CARACTERISTICA, Datasets.CARACTERISTICA_VEHICULO,
		Datasets.CIUDAD, Datasets.CLIENTE, Datasets.FAVORITO, Datasets.FOTO_VEHICULO, Datasets.PREGUNTA,
		Datasets.TELEFONO_USUARIO, Datasets.USUARIO, Datasets.VEHICULO, Datasets.VENTA, Datasets.CALIFICACION })
public class ServiciosNegocioTest {

	private static final String EMAIL1 = "usuario1@servidor.com";
	private static final String EMAIL2 = "usuario2@servidor.com";
	private static final String EMAIL3 = "usuario3@servidor.com";
	private static final String QUINDIO = "Quindío";

	@EJB
	private CorreoEJB correoEJB;

	@EJB
	private CiudadEJB ciudadEJB;

	@EJB
	private VentaEJB ventaEJB;

	@EJB
	private CalificacionEJB calificacionEJB;

	@PersistenceContext
	private EntityManager entityManager;

	/**
	 * Arma el despliegue a partir de {@link Despliegue#crear()} agregando los EJB de negocio, el
	 * paquete de excepciones y el archivo de respaldo de ciudades (que vive en el jar de
	 * proyecto-negocio, presente en el classpath de las pruebas). No incluye {@code SetupEJB}.
	 *
	 * @return archivo WAR de prueba
	 */
	@Deployment
	public static Archive<?> d() {
		return Despliegue.crear().addClass(ClienteEJB.class).addClass(VehiculoEJB.class).addClass(PreguntaEJB.class)
				.addClass(CatalogoEJB.class).addClass(CorreoEJB.class).addClass(PlantillasCorreo.class)
				.addClass(CiudadEJB.class).addClass(AdministradorEJB.class).addClass(AdministradorEJBRemote.class)
				.addClass(VentaEJB.class).addClass(CalificacionEJB.class)
				.addPackage(AutenticacionException.class.getPackage()).addAsResource("ciudades-colombia.json");
	}

	// ------------------------------------------------------------------
	// Utilidades
	// ------------------------------------------------------------------

	/**
	 * Prepara una compra aun no calificada: el usuario2 (dueno del vehiculo 3) le vende el
	 * vehiculo 3 al usuario1.
	 *
	 * @return la venta registrada
	 * @throws Exception si el negocio rechaza la venta
	 */
	private Venta ventaDeUsuario2AUsuario1() throws Exception {
		Venta v = ventaEJB.registrarVenta(3L, EMAIL2, EMAIL1);
		entityManager.flush();
		return v;
	}

	/**
	 * Intenta enviar un correo y comprueba que se rechace con InformacionInvalidaException.
	 *
	 * @param asunto asunto
	 * @param mensaje mensaje
	 * @param destinatario destinatario
	 * @throws Exception si lanza una excepcion distinta
	 */
	private void esperarRechazo(String asunto, String mensaje, String destinatario) throws Exception {
		try {
			correoEJB.enviarEmail(asunto, mensaje, destinatario);
			Assert.fail("Se esperaba InformacionInvalidaException con destinatario: " + destinatario);
		} catch (InformacionInvalidaException e) {
			Assert.assertNotNull(e.getMessage());
		}
	}

	// ------------------------------------------------------------------
	// CorreoEJB.enviarEmail
	// ------------------------------------------------------------------

	/**
	 * Asunto nulo. Esperado: InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void enviarEmailAsuntoNuloTest() throws Exception {
		correoEJB.enviarEmail(null, "Mensaje", EMAIL1);
	}

	/**
	 * Asunto en blanco. Esperado: InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void enviarEmailAsuntoVacioTest() throws Exception {
		correoEJB.enviarEmail("   ", "Mensaje", EMAIL1);
	}

	/**
	 * Mensaje nulo. Esperado: InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void enviarEmailMensajeNuloTest() throws Exception {
		correoEJB.enviarEmail("Asunto", null, EMAIL1);
	}

	/**
	 * Mensaje vacio. Esperado: InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void enviarEmailMensajeVacioTest() throws Exception {
		correoEJB.enviarEmail("Asunto", "", EMAIL1);
	}

	/**
	 * Destinatario nulo. Esperado: InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void enviarEmailDestinatarioNuloTest() throws Exception {
		correoEJB.enviarEmail("Asunto", "Mensaje", null);
	}

	/**
	 * Destinatario en blanco. Esperado: InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void enviarEmailDestinatarioVacioTest() throws Exception {
		correoEJB.enviarEmail("Asunto", "Mensaje", "  ");
	}

	/**
	 * Destinatarios con formato invalido (sin arroba, sin parte local, con dos direcciones o con
	 * doble arroba). Esperado: InformacionInvalidaException en todos (el analisis es estricto).
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void enviarEmailDestinatarioFormatoInvalidoTest() throws Exception {
		esperarRechazo("Asunto", "Mensaje", "sin-arroba");
		esperarRechazo("Asunto", "Mensaje", "@servidor.com");
		esperarRechazo("Asunto", "Mensaje", "uno@servidor.com,dos@servidor.com");
		esperarRechazo("Asunto", "Mensaje", "a@@servidor.com");
	}

	/**
	 * Envio con datos validos. El GlassFish embebido no tiene el recurso {@code mail/unimotor}, asi
	 * que el envio se simula: no lanza excepcion y devuelve {@code false}. Se acepta el destinatario
	 * con espacios alrededor porque el codigo lo recorta.
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void enviarEmailModoSimuladoTest() throws Exception {
		Assert.assertFalse(correoEJB.enviarEmail("Asunto de prueba", "Cuerpo de prueba", EMAIL1));
		Assert.assertFalse(correoEJB.enviarEmail("Asunto de prueba", "Cuerpo de prueba", "  " + EMAIL2 + "  "));
	}

	// ------------------------------------------------------------------
	// PlantillasCorreo
	// ------------------------------------------------------------------

	/**
	 * Asunto y cuerpo de pregunta recibida con datos cargados de la base: pregunta del cliente 5
	 * sobre el vehiculo 1 (Mazda 3, 2018, VEV592, 26000000). Esperado: el asunto y el cuerpo traen
	 * la marca, modelo, anio, placa, precio con puntos de miles, el nombre y email de quien
	 * pregunta, el texto y la fecha formateada.
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void preguntaRecibidaConDatosTest() {
		Vehiculo vehiculo = entityManager.find(Vehiculo.class, 1L);
		Cliente cliente = entityManager.find(Cliente.class, 4L);
		Calendar c = new GregorianCalendar(2020, Calendar.OCTOBER, 24, 15, 30);
		Pregunta pregunta = new Pregunta(vehiculo, cliente, c.getTime(), "Aceptan permuta?");

		Assert.assertEquals("uniMotor: nueva pregunta sobre tu Mazda 3 2018",
				PlantillasCorreo.asuntoPreguntaRecibida(pregunta));

		String cuerpo = PlantillasCorreo.preguntaRecibida(pregunta);
		Assert.assertTrue(cuerpo.contains("Nombre: Hector Rodriguez"));
		Assert.assertTrue(cuerpo.contains("Email: " + EMAIL1));
		Assert.assertTrue(cuerpo.contains("(Personal): 3135638905"));
		Assert.assertTrue(cuerpo.contains("(Personal 2): 3155679045"));
		Assert.assertTrue(cuerpo.contains("Marca: Mazda"));
		Assert.assertTrue(cuerpo.contains("Modelo: 3"));
		Assert.assertTrue(cuerpo.contains("Año: 2018"));
		Assert.assertTrue(cuerpo.contains("Placa: VEV592"));
		Assert.assertTrue(cuerpo.contains("Precio: $ 26.000.000"));
		Assert.assertTrue(cuerpo.contains("Aceptan permuta?"));
		Assert.assertTrue(cuerpo.contains("Fecha: 24/10/2020 15:30"));
		Assert.assertFalse(cuerpo.contains("no disponible"));
	}

	/**
	 * Pregunta nula: el asunto usa la palabra generica de vehiculo y el cuerpo marca como no
	 * disponibles al cliente y al vehiculo, con el texto y la fecha vacios representados por un
	 * guion. Esperado: no lanza excepcion.
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void preguntaRecibidaNulaTest() {
		String asunto = PlantillasCorreo.asuntoPreguntaRecibida(null);
		Assert.assertTrue(asunto.startsWith("uniMotor: nueva pregunta sobre tu veh"));
		Assert.assertFalse(asunto.contains("null"));

		String cuerpo = PlantillasCorreo.preguntaRecibida(null);
		Assert.assertTrue(cuerpo.contains("Quien pregunta:\n  (no disponible)"));
		Assert.assertTrue(cuerpo.contains(":\n  (no disponible)\n\nPregunta:\n  -"));
		Assert.assertTrue(cuerpo.contains("Fecha: -"));
		Assert.assertFalse(cuerpo.contains("null"));
	}

	/**
	 * Pregunta con campos nulos: vehiculo sin modelo, sin precio y placa vacia; cliente sin nombre
	 * ni telefonos. Esperado: se usan guiones y nunca aparece la palabra null.
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void preguntaRecibidaCamposNulosTest() {
		Vehiculo vehiculo = new Vehiculo();
		vehiculo.setPlaca("  ");
		vehiculo.setPrecio(null);
		vehiculo.setYear(2015);
		Cliente cliente = new Cliente();
		cliente.setEmail(null);
		cliente.setNombre_completo(null);
		cliente.setTelefono(null);
		Pregunta pregunta = new Pregunta(vehiculo, cliente, null, null);

		Assert.assertTrue(PlantillasCorreo.asuntoPreguntaRecibida(pregunta).endsWith("culo"));

		String cuerpo = PlantillasCorreo.preguntaRecibida(pregunta);
		Assert.assertTrue(cuerpo.contains("Nombre: -"));
		Assert.assertTrue(cuerpo.contains("Email: -"));
		Assert.assertTrue(cuerpo.contains("Marca: -"));
		Assert.assertTrue(cuerpo.contains("Modelo: -"));
		Assert.assertTrue(cuerpo.contains("Placa: -"));
		Assert.assertTrue(cuerpo.contains("Precio: -"));
		Assert.assertTrue(cuerpo.contains("Pregunta:\n  -"));
		Assert.assertTrue(cuerpo.contains("Fecha: -"));
		Assert.assertFalse(cuerpo.contains("null"));
	}

	/**
	 * Un telefono con valor vacio se imprime con guion y la clave se conserva.
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void preguntaRecibidaTelefonoVacioTest() {
		Cliente cliente = new Cliente();
		cliente.setEmail(EMAIL1);
		Map<String, String> telefonos = new HashMap<>();
		telefonos.put("Casa", "");
		cliente.setTelefono(telefonos);
		String cuerpo = PlantillasCorreo.preguntaRecibida(new Pregunta(null, cliente, null, "Hola"));
		Assert.assertTrue(cuerpo.contains("(Casa): -"));
		Assert.assertTrue(cuerpo.contains("Vehículo:\n  (no disponible)"));
	}

	/**
	 * Asunto y cuerpo de recuperacion de contrasena para un cliente con nombre. Esperado: saluda
	 * por el nombre, trae la contrasena temporal y recomienda cambiarla.
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void recuperarPasswordClienteTest() {
		Assert.assertTrue(PlantillasCorreo.asuntoRecuperarPassword().startsWith("uniMotor: recuperaci"));
		Cliente cliente = entityManager.find(Cliente.class, 4L);
		String cuerpo = PlantillasCorreo.recuperarPassword(cliente, "Tmp-1234");
		Assert.assertTrue(cuerpo.startsWith("Hola Hector Rodriguez,"));
		Assert.assertTrue(cuerpo.contains("Tu contraseña temporal es: Tmp-1234"));
		Assert.assertTrue(cuerpo.contains("cambiarla"));
	}

	/**
	 * Un usuario que no es cliente (administrador 1) se saluda por su email, igual que un cliente
	 * sin nombre. Un usuario nulo deja el saludo sin nombre.
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void recuperarPasswordSinNombreTest() {
		Usuario admin = entityManager.find(Usuario.class, 1L);
		Assert.assertTrue(PlantillasCorreo.recuperarPassword(admin, "abc").startsWith("Hola cristia@proyecto.com,"));

		Cliente sinNombre = new Cliente();
		sinNombre.setEmail(EMAIL2);
		sinNombre.setNombre_completo(null);
		Assert.assertTrue(PlantillasCorreo.recuperarPassword(sinNombre, "abc").startsWith("Hola " + EMAIL2 + ","));

		String nulo = PlantillasCorreo.recuperarPassword(null, "abc");
		Assert.assertTrue(nulo.startsWith("Hola ,"));
		Assert.assertTrue(nulo.contains("temporal es: abc"));
	}

	/**
	 * Asunto y cuerpo de venta registrada con datos de la base: la venta 1 es el vehiculo 2
	 * (NCV903, Renoult 4, 1990, 6000000) del vendedor 6 (Ruben zuluaga, usuario3) con fecha
	 * 2020-10-24. Esperado: trae los datos del vehiculo, del vendedor, la fecha y la invitacion a
	 * calificar.
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void ventaRegistradaConDatosTest() {
		Venta venta = entityManager.find(Venta.class, 1L);
		Assert.assertEquals("uniMotor: registramos la compra de tu Renoult 4 1990",
				PlantillasCorreo.asuntoVentaRegistrada(venta));

		String cuerpo = PlantillasCorreo.ventaRegistrada(venta);
		Assert.assertTrue(cuerpo.contains("Marca: Renoult"));
		Assert.assertTrue(cuerpo.contains("Año: 1990"));
		Assert.assertTrue(cuerpo.contains("Placa: NCV903"));
		Assert.assertTrue(cuerpo.contains("Precio: $ 6.000.000"));
		Assert.assertTrue(cuerpo.contains("Nombre: Ruben zuluaga"));
		Assert.assertTrue(cuerpo.contains("Email: " + EMAIL3));
		Assert.assertTrue(cuerpo.contains("Fecha: 24/10/"));
		Assert.assertTrue(cuerpo.contains("calificar al vendedor"));
		Assert.assertFalse(cuerpo.contains("no disponible"));
	}

	/**
	 * Venta nula o vacia (sin vehiculo, sin fecha): el asunto usa la palabra generica y el cuerpo
	 * marca el vehiculo y el vendedor como no disponibles. Esperado: no lanza excepcion ni imprime
	 * null.
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void ventaRegistradaNulaTest() {
		Assert.assertTrue(PlantillasCorreo.asuntoVentaRegistrada(null).endsWith("tu vehículo"));
		Assert.assertTrue(PlantillasCorreo.asuntoVentaRegistrada(new Venta()).endsWith("tu vehículo"));

		String cuerpo = PlantillasCorreo.ventaRegistrada(null);
		Assert.assertTrue(cuerpo.contains("Vehículo:\n  (no disponible)"));
		Assert.assertTrue(cuerpo.contains("Vendedor:\n  (no disponible)"));
		Assert.assertTrue(cuerpo.contains("Fecha: -"));
		Assert.assertFalse(cuerpo.contains("null"));
	}

	/**
	 * Venta con un vehiculo sin modelo, sin precio y sin dueno: usa guiones y marca al vendedor no
	 * disponible.
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void ventaRegistradaVehiculoIncompletoTest() {
		Vehiculo vehiculo = new Vehiculo();
		vehiculo.setPlaca("ABC123");
		vehiculo.setPrecio(null);
		vehiculo.setYear(2000);
		Venta venta = new Venta(null, vehiculo, null);

		Assert.assertTrue(PlantillasCorreo.asuntoVentaRegistrada(venta).endsWith("tu vehículo"));
		String cuerpo = PlantillasCorreo.ventaRegistrada(venta);
		Assert.assertTrue(cuerpo.contains("Marca: -"));
		Assert.assertTrue(cuerpo.contains("Modelo: -"));
		Assert.assertTrue(cuerpo.contains("Placa: ABC123"));
		Assert.assertTrue(cuerpo.contains("Precio: -"));
		Assert.assertTrue(cuerpo.contains("Vendedor:\n  (no disponible)"));
		Assert.assertFalse(cuerpo.contains("null"));
	}

	/**
	 * Las plantillas de venta incluyen los telefonos del vendedor cuando los tiene: el vendedor
	 * del vehiculo 1 es el cliente 4, con dos telefonos en los JSON. Se arma una venta en memoria
	 * sobre ese vehiculo.
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void ventaRegistradaConTelefonosTest() {
		Vehiculo vehiculo = entityManager.find(Vehiculo.class, 1L);
		Cliente comprador = entityManager.find(Cliente.class, 5L);
		Venta venta = new Venta(new java.util.Date(), vehiculo, comprador);
		String cuerpo = PlantillasCorreo.ventaRegistrada(venta);
		Assert.assertTrue(cuerpo.contains("Nombre: Hector Rodriguez"));
		Assert.assertTrue(cuerpo.contains("(Personal): 3135638905"));
		Assert.assertTrue(cuerpo.contains("(Personal 2): 3155679045"));
		Assert.assertTrue(cuerpo.contains("Placa: VEV592"));
		Assert.assertTrue(cuerpo.contains("Precio: $ 26.000.000"));
	}

	/**
	 * Un modelo con marca y nombre vacios se representa con guiones en la plantilla.
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void ventaRegistradaModeloSinNombreTest() {
		Modelo modelo = new Modelo();
		modelo.setMarca(" ");
		modelo.setNombre(null);
		Vehiculo vehiculo = new Vehiculo();
		vehiculo.setCodigo_modelo(modelo);
		vehiculo.setYear(1999);
		String cuerpo = PlantillasCorreo.ventaRegistrada(new Venta(null, vehiculo, null));
		Assert.assertTrue(cuerpo.contains("Marca: -"));
		Assert.assertTrue(cuerpo.contains("Modelo: -"));
		Assert.assertEquals("uniMotor: registramos la compra de tu - - 1999",
				PlantillasCorreo.asuntoVentaRegistrada(new Venta(null, vehiculo, null)));
	}

	// ------------------------------------------------------------------
	// CiudadEJB
	// ------------------------------------------------------------------

	/**
	 * Listar las ciudades de los JSON. Esperado: tres ciudades ordenadas por nombre (Armenia,
	 * Buenavista, Pijao).
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void listarCiudadesTest() {
		List<Ciudad> ciudades = ciudadEJB.listarCiudades();
		Assert.assertEquals(3, ciudades.size());
		Assert.assertEquals("Armenia", ciudades.get(0).getNombre());
		Assert.assertEquals("Buenavista", ciudades.get(1).getNombre());
		Assert.assertEquals("Pijao", ciudades.get(2).getNombre());
	}

	/**
	 * Buscar una ciudad que existe con su departamento. Esperado: la ciudad 1 de los JSON.
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void buscarCiudadExistenteTest() throws Exception {
		Ciudad ciudad = ciudadEJB.buscarCiudad("Pijao", QUINDIO);
		Assert.assertEquals(Long.valueOf(3), ciudad.getCodigo_ciudad());
		Assert.assertEquals(QUINDIO, ciudad.getDepartamento());
	}

	/**
	 * Buscar una ciudad inexistente. Esperado: ElementoNoEncontradoException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = ElementoNoEncontradoException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void buscarCiudadInexistenteTest() throws Exception {
		ciudadEJB.buscarCiudad("Atlantis", QUINDIO);
	}

	/**
	 * Buscar una ciudad que existe pero con otro departamento. Esperado:
	 * ElementoNoEncontradoException (la busqueda es por nombre y departamento).
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = ElementoNoEncontradoException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void buscarCiudadOtroDepartamentoTest() throws Exception {
		ciudadEJB.buscarCiudad("Armenia", "Antioquia");
	}

	/**
	 * Buscar con nombre y departamento nulos. Esperado: ElementoNoEncontradoException (no hay
	 * ninguna ciudad con esos valores).
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = ElementoNoEncontradoException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void buscarCiudadNulosTest() throws Exception {
		ciudadEJB.buscarCiudad(null, null);
	}

	/**
	 * Cargar ciudades con la tabla ya poblada (3 ciudades). Esperado: devuelve 0, no consulta la
	 * API y la tabla sigue con 3 filas.
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void cargarCiudadesTablaPobladaTest() {
		Assert.assertEquals(0, ciudadEJB.cargarCiudades());
		Assert.assertEquals(3, ciudadEJB.listarCiudades().size());
	}

	/**
	 * El archivo de respaldo esta en el despliegue y se lee bien. El metodo que lo lee es privado
	 * y {@code consultarCiudadesColombia} llama primero a la API externa, asi que se invoca por
	 * reflexion. Esperado: mas de 500 ciudades, sin pares repetidos, y existe Armenia de Quindio.
	 *
	 * @throws Exception si falla la reflexion o la lectura
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void leerRespaldoCiudadesTest() throws Exception {
		Assert.assertNotNull("Falta " + CiudadEJB.RESPALDO + " en el despliegue",
				CiudadEJB.class.getResourceAsStream(CiudadEJB.RESPALDO));
		Method leer = CiudadEJB.class.getDeclaredMethod("leerRespaldo");
		leer.setAccessible(true);
		@SuppressWarnings("unchecked")
		List<Ciudad> ciudades = (List<Ciudad>) leer.invoke(new CiudadEJB());

		Assert.assertTrue("Se esperaban mas de 500 ciudades y hay " + ciudades.size(), ciudades.size() > 500);
		Set<String> pares = new HashSet<>();
		boolean armenia = false;
		for (Ciudad c : ciudades) {
			Assert.assertTrue("Par repetido: " + c.getNombre() + " - " + c.getDepartamento(),
					pares.add(c.getNombre() + "|" + c.getDepartamento()));
			if ("Armenia".equals(c.getNombre()) && QUINDIO.equals(c.getDepartamento())) {
				armenia = true;
			}
		}
		Assert.assertTrue(armenia);
	}

	// ------------------------------------------------------------------
	// VentaEJB: emails en blanco
	// ------------------------------------------------------------------

	/**
	 * Email del vendedor con solo espacios. Esperado: InformacionInvalidaException y el vehiculo
	 * sigue activo y sin venta.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void registrarVentaVendedorEnBlancoTest() throws Exception {
		try {
			ventaEJB.registrarVenta(1L, "  ", EMAIL2);
			Assert.fail("Se esperaba InformacionInvalidaException");
		} catch (InformacionInvalidaException e) {
			Assert.assertNotNull(e.getMessage());
		}
		Vehiculo v = entityManager.find(Vehiculo.class, 1L);
		Assert.assertTrue(v.isActivo());
		Assert.assertNull(v.getCodigo_venta());
	}

	/**
	 * Email del comprador con solo espacios. Esperado: InformacionInvalidaException y no se crea
	 * ninguna venta nueva (siguen las 3 de los JSON).
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void registrarVentaCompradorEnBlancoTest() throws Exception {
		try {
			ventaEJB.registrarVenta(1L, EMAIL1, "   ");
			Assert.fail("Se esperaba InformacionInvalidaException");
		} catch (InformacionInvalidaException e) {
			Assert.assertNotNull(e.getMessage());
		}
		Long ventas = entityManager.createQuery("select count(v) from Venta v", Long.class).getSingleResult();
		Assert.assertEquals(Long.valueOf(3), ventas);
	}

	/**
	 * Los emails con espacios alrededor se recortan: vender el vehiculo 5 (dueno usuario1) al
	 * usuario3 con espacios. Esperado: la venta se registra.
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void registrarVentaEmailsConEspaciosTest() throws Exception {
		Venta venta = ventaEJB.registrarVenta(5L, " " + EMAIL1 + " ", " " + EMAIL3 + " ");
		Assert.assertNotNull(venta.getCodigo_venta());
		Assert.assertEquals(EMAIL3, venta.getCodigo_usuario().getEmail());
	}

	// ------------------------------------------------------------------
	// CalificacionEJB: menores
	// ------------------------------------------------------------------

	/**
	 * Estrellas negativas. Esperado: InformacionInvalidaException y no se crea calificacion.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void calificarVendedorEstrellasNegativasTest() throws Exception {
		ventaDeUsuario2AUsuario1();
		try {
			calificacionEJB.calificarVendedor(EMAIL1, EMAIL2, -1, "Negativa");
			Assert.fail("Se esperaba InformacionInvalidaException");
		} catch (InformacionInvalidaException e) {
			Assert.assertNotNull(e.getMessage());
		}
		Assert.assertEquals(Long.valueOf(3),
				entityManager.createQuery("select count(c) from Calificacion c", Long.class).getSingleResult());
		Assert.assertTrue(calificacionEJB.puedeCalificar(EMAIL1, EMAIL2));
	}

	/**
	 * Una estrella es el limite inferior valido: tras comprar al usuario2, el usuario1 lo califica
	 * con 1 estrella. Esperado: se guarda y el promedio del usuario2 pasa de 3.0 a 2.0.
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void calificarVendedorUnaEstrellaTest() throws Exception {
		ventaDeUsuario2AUsuario1();
		Calificacion c = calificacionEJB.calificarVendedor(EMAIL1, EMAIL2, 1, "Mala experiencia");
		entityManager.flush();
		Assert.assertNotNull(c.getCodigo_calificacion());
		Assert.assertEquals(1, c.getEstrellas());
		Assert.assertEquals(2.0, calificacionEJB.promedioVendedor(EMAIL2), 0.001);
		Assert.assertFalse(calificacionEJB.puedeCalificar(EMAIL1, EMAIL2));
	}

	/**
	 * Cinco estrellas es el limite superior valido (con los emails con espacios, que se recortan).
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void calificarVendedorCincoEstrellasTest() throws Exception {
		ventaDeUsuario2AUsuario1();
		Calificacion c = calificacionEJB.calificarVendedor(" " + EMAIL1 + " ", " " + EMAIL2 + " ", 5, null);
		Assert.assertEquals(5, c.getEstrellas());
		Assert.assertNull(c.getComentario());
	}

	/**
	 * Email del calificador con solo espacios. Esperado: InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void calificarCalificadorEnBlancoTest() throws Exception {
		calificacionEJB.calificarVendedor("  ", EMAIL2, 3, "x");
	}

	/**
	 * Email del vendedor con solo espacios. Esperado: InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void calificarVendedorEnBlancoTest() throws Exception {
		calificacionEJB.calificarVendedor(EMAIL1, "  ", 3, "x");
	}

	/**
	 * {@code puedeCalificar} con emails en blanco. Esperado: {@code false} sin lanzar excepcion,
	 * aunque el otro email sea valido.
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void puedeCalificarEmailVacioTest() {
		Assert.assertFalse(calificacionEJB.puedeCalificar("  ", EMAIL2));
		Assert.assertFalse(calificacionEJB.puedeCalificar(EMAIL1, "  "));
		Assert.assertFalse(calificacionEJB.puedeCalificar("", ""));
	}

	/**
	 * {@code puedeCalificar} recorta los espacios: tras la compra del usuario1 al usuario2, con
	 * espacios alrededor sigue siendo verdadero.
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void puedeCalificarEmailsConEspaciosTest() throws Exception {
		ventaDeUsuario2AUsuario1();
		Assert.assertTrue(calificacionEJB.puedeCalificar("  " + EMAIL1, EMAIL2 + "  "));
	}
}
