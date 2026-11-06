package co.edu.uniquindio.unimotor.pruebas;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
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

import co.edu.uniquindio.unimotor.dto.EstadisticaDTO;
import co.edu.uniquindio.unimotor.dto.PuntoDTO;
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
import co.edu.uniquindio.unimotor.entidades.Administrador;
import co.edu.uniquindio.unimotor.entidades.Usuario;
import co.edu.uniquindio.unimotor.excepciones.AutenticacionException;
import co.edu.uniquindio.unimotor.excepciones.ElementoNoEncontradoException;
import co.edu.uniquindio.unimotor.excepciones.InformacionInvalidaException;

/**
 * Pruebas in-container de la Fase 2.5: {@link AdministradorEJB}, usado a traves de la interfaz
 * {@link AdministradorEJBRemote}. Autenticacion y recuperacion de contrasena de administradores
 * y las 17 estadisticas para los graficos del escritorio.
 * <p>
 * Cada prueba corre en una transaccion con rollback sobre los 14 datasets JSON de
 * src/test/resources. El correo funciona en modo simulado. No se despliega {@code SetupEJB}.
 * <p>
 * Datos usados: administradores 1 (cristia@proyecto.com, clave 123), 2 (Jhon@proyecto.com, 1234)
 * y 3 (profesor@proyecto.com, 444); clientes 4, 5 y 6 (usuarioN@servidor.com, clave 12345).
 * Los valores esperados de las estadisticas son los mismos de {@link ConsultasTest}; en cada
 * prueba se cita el metodo de origen.
 */
@RunWith(Arquillian.class)
@UsingDataSet({ Datasets.MODELO, Datasets.ADMINISTRADOR, Datasets.CARACTERISTICA, Datasets.CARACTERISTICA_VEHICULO,
		Datasets.CIUDAD, Datasets.CLIENTE, Datasets.FAVORITO, Datasets.FOTO_VEHICULO, Datasets.PREGUNTA,
		Datasets.TELEFONO_USUARIO, Datasets.USUARIO, Datasets.VEHICULO, Datasets.VENTA, Datasets.CALIFICACION })
public class AdministradorNegocioTest {

	private static final String EMAIL_ADMIN = "cristia@proyecto.com";
	private static final String PASSWORD_ADMIN = "123";
	private static final String EMAIL_CLIENTE = "usuario1@servidor.com";
	private static final String PASSWORD_CLIENTE = "12345";
	private static final double DELTA = 0.001;

	@EJB
	private AdministradorEJBRemote administradorEJB;

	@PersistenceContext
	private EntityManager entityManager;

	/**
	 * Arma el despliegue a partir de {@link Despliegue#crear()} agregando los EJB de negocio y
	 * el paquete de excepciones. No incluye {@code SetupEJB}.
	 *
	 * @return archivo WAR de prueba
	 */
	@Deployment
	public static Archive<?> d() {
		return Despliegue.crear().addClass(ClienteEJB.class).addClass(VehiculoEJB.class).addClass(PreguntaEJB.class)
				.addClass(CatalogoEJB.class).addClass(CorreoEJB.class).addClass(PlantillasCorreo.class)
				.addClass(CiudadEJB.class).addClass(AdministradorEJB.class).addClass(AdministradorEJBRemote.class)
				.addClass(VentaEJB.class).addClass(CalificacionEJB.class)
				.addPackage(AutenticacionException.class.getPackage());
	}

	// ------------------------------------------------------------------
	// Utilidades
	// ------------------------------------------------------------------

	/** Lee la contrasena actual de un usuario directamente de la base. */
	private String passwordDe(String email) {
		entityManager.flush();
		return entityManager.createNamedQuery(Usuario.AUTENTIFICAR_EMAIL, Usuario.class)
				.setParameter("email", email).getSingleResult().getPassword();
	}

	/** Convierte la lista en mapa etiqueta a valor (long), verificando que no haya etiquetas repetidas. */
	private Map<String, Long> aMapaLong(List<EstadisticaDTO> lista) {
		Map<String, Long> mapa = new HashMap<>();
		for (EstadisticaDTO e : lista) {
			Assert.assertNull("Etiqueta repetida: " + e.getEtiqueta(),
					mapa.put(e.getEtiqueta(), e.getValor().longValue()));
		}
		return mapa;
	}

	private void assertEstadistica(String etiqueta, long valor, EstadisticaDTO e) {
		Assert.assertEquals(etiqueta, e.getEtiqueta());
		Assert.assertEquals(valor, e.getValor().longValue());
	}

	private void assertEstadistica(String etiqueta, double valor, EstadisticaDTO e) {
		Assert.assertEquals(etiqueta, e.getEtiqueta());
		Assert.assertEquals(valor, e.getValor().doubleValue(), DELTA);
	}

	/** Serializa el objeto con ObjectOutputStream; falla si no es serializable. */
	private void serializar(Object o) throws Exception {
		try (ObjectOutputStream oos = new ObjectOutputStream(new ByteArrayOutputStream())) {
			oos.writeObject(o);
		}
	}

	// ------------------------------------------------------------------
	// autenticarAdministrador
	// ------------------------------------------------------------------

	/**
	 * Autentica al administrador 1 con su clave. Esperado: devuelve el Administrador con su email.
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void autenticarAdministradorTest() throws Exception {
		Administrador a = administradorEJB.autenticarAdministrador(EMAIL_ADMIN, PASSWORD_ADMIN);
		Assert.assertNotNull(a);
		Assert.assertEquals(EMAIL_ADMIN, a.getEmail());
		Assert.assertEquals(Long.valueOf(1L), a.getCodigo_usuario());
	}

	/**
	 * Autentica a otro administrador (profesor, clave 444). Esperado: devuelve ese administrador.
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void autenticarOtroAdministradorTest() throws Exception {
		Administrador a = administradorEJB.autenticarAdministrador("profesor@proyecto.com", "444");
		Assert.assertEquals("profesor@proyecto.com", a.getEmail());
	}

	/**
	 * Contrasena incorrecta. Esperado: AutenticacionException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = AutenticacionException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void autenticarAdministradorPasswordIncorrectoTest() throws Exception {
		administradorEJB.autenticarAdministrador(EMAIL_ADMIN, "incorrecta");
	}

	/**
	 * Email inexistente. Esperado: AutenticacionException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = AutenticacionException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void autenticarAdministradorEmailInexistenteTest() throws Exception {
		administradorEJB.autenticarAdministrador("nadie@proyecto.com", PASSWORD_ADMIN);
	}

	/**
	 * Un cliente con su contrasena correcta no puede entrar como administrador. Esperado:
	 * AutenticacionException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = AutenticacionException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void autenticarAdministradorConClienteTest() throws Exception {
		administradorEJB.autenticarAdministrador(EMAIL_CLIENTE, PASSWORD_CLIENTE);
	}

	/**
	 * Email null. Esperado: AutenticacionException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = AutenticacionException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void autenticarAdministradorEmailNuloTest() throws Exception {
		administradorEJB.autenticarAdministrador(null, PASSWORD_ADMIN);
	}

	/**
	 * Contrasena null. Esperado: AutenticacionException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = AutenticacionException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void autenticarAdministradorPasswordNuloTest() throws Exception {
		administradorEJB.autenticarAdministrador(EMAIL_ADMIN, null);
	}

	/**
	 * Email vacio. Esperado: AutenticacionException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = AutenticacionException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void autenticarAdministradorEmailVacioTest() throws Exception {
		administradorEJB.autenticarAdministrador("  ", PASSWORD_ADMIN);
	}

	/**
	 * Contrasena vacia. Esperado: AutenticacionException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = AutenticacionException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void autenticarAdministradorPasswordVaciaTest() throws Exception {
		administradorEJB.autenticarAdministrador(EMAIL_ADMIN, "");
	}

	// ------------------------------------------------------------------
	// recuperarPasswordAdministrador
	// ------------------------------------------------------------------

	/**
	 * Recupera la contrasena del administrador 1. Esperado: la contrasena en la base cambia y la
	 * temporal tiene 8 caracteres.
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void recuperarPasswordAdministradorTest() throws Exception {
		administradorEJB.recuperarPasswordAdministrador(EMAIL_ADMIN);
		String nueva = passwordDe(EMAIL_ADMIN);
		Assert.assertNotNull(nueva);
		Assert.assertFalse(PASSWORD_ADMIN.equals(nueva));
		Assert.assertEquals(8, nueva.length());
	}

	/**
	 * Email de un cliente. Esperado: ElementoNoEncontradoException y la contrasena del cliente no cambia.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void recuperarPasswordAdministradorConClienteTest() throws Exception {
		try {
			administradorEJB.recuperarPasswordAdministrador(EMAIL_CLIENTE);
			Assert.fail("Debia lanzar ElementoNoEncontradoException");
		} catch (ElementoNoEncontradoException e) {
			// esperado
		}
		Assert.assertEquals(PASSWORD_CLIENTE, passwordDe(EMAIL_CLIENTE));
	}

	/**
	 * Email inexistente. Esperado: ElementoNoEncontradoException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = ElementoNoEncontradoException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void recuperarPasswordAdministradorInexistenteTest() throws Exception {
		administradorEJB.recuperarPasswordAdministrador("nadie@proyecto.com");
	}

	/**
	 * Email null. Esperado: InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void recuperarPasswordAdministradorNuloTest() throws Exception {
		administradorEJB.recuperarPasswordAdministrador(null);
	}

	/**
	 * Email vacio. Esperado: InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void recuperarPasswordAdministradorVacioTest() throws Exception {
		administradorEJB.recuperarPasswordAdministrador("   ");
	}

	// ------------------------------------------------------------------
	// Estadisticas (valores de ConsultasTest)
	// ------------------------------------------------------------------

	/** Valores de ConsultasTest.estadisticaPorTipoTest: CARRO 5 (primero), MOTOCICLETA 1, CAMIONETA 1. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void vehiculosPorTipoTest() {
		List<EstadisticaDTO> r = administradorEJB.vehiculosPorTipo();
		Assert.assertEquals(3, r.size());
		assertEstadistica("CARRO", 5L, r.get(0));
		Map<String, Long> m = aMapaLong(r);
		Assert.assertEquals(Long.valueOf(1), m.get("MOTOCICLETA"));
		Assert.assertEquals(Long.valueOf(1), m.get("CAMIONETA"));
	}

	/** Valores de ConsultasTest.estadisticaPorCondicionTest: USADO 5 y NUEVO 2. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void vehiculosPorCondicionTest() {
		List<EstadisticaDTO> r = administradorEJB.vehiculosPorCondicion();
		Assert.assertEquals(2, r.size());
		assertEstadistica("USADO", 5L, r.get(0));
		assertEstadistica("NUEVO", 2L, r.get(1));
	}

	/** Valores de ConsultasTest.estadisticaPorTransmisionTest: MECANICO 5 y AUTOMATICO 2. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void vehiculosPorTransmisionTest() {
		List<EstadisticaDTO> r = administradorEJB.vehiculosPorTransmision();
		Assert.assertEquals(2, r.size());
		assertEstadistica("MECANICO", 5L, r.get(0));
		assertEstadistica("AUTOMATICO", 2L, r.get(1));
	}

	/** Valores de ConsultasTest.estadisticaPorCombustibleTest: GASOLINA 6 y DISEL 1. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void vehiculosPorCombustibleTest() {
		List<EstadisticaDTO> r = administradorEJB.vehiculosPorCombustible();
		Assert.assertEquals(2, r.size());
		assertEstadistica("GASOLINA", 6L, r.get(0));
		assertEstadistica("DISEL", 1L, r.get(1));
	}

	/** Valores de ConsultasTest.estadisticaPorMarcaTest: Mazda 3 (primero), Audi 2, Renoult 2. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void vehiculosPorMarcaTest() {
		List<EstadisticaDTO> r = administradorEJB.vehiculosPorMarca();
		Assert.assertEquals(3, r.size());
		assertEstadistica("Mazda", 3L, r.get(0));
		Map<String, Long> m = aMapaLong(r);
		Assert.assertEquals(Long.valueOf(2), m.get("Audi"));
		Assert.assertEquals(Long.valueOf(2), m.get("Renoult"));
	}

	/** Valores de ConsultasTest.estadisticaPrecioPromedioPorMarcaTest: Audi 34.5M, Mazda 23.5M, Renoult 5.25M. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void precioPromedioPorMarcaTest() {
		List<EstadisticaDTO> r = administradorEJB.precioPromedioPorMarca();
		Assert.assertEquals(3, r.size());
		assertEstadistica("Audi", 34500000d, r.get(0));
		assertEstadistica("Mazda", 23500000d, r.get(1));
		assertEstadistica("Renoult", 5250000d, r.get(2));
	}

	/** Valores de ConsultasTest.estadisticaPorCiudadTest: Armenia 4, Buenavista 2, Pijao 1. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void vehiculosPorCiudadTest() {
		List<EstadisticaDTO> r = administradorEJB.vehiculosPorCiudad();
		Assert.assertEquals(3, r.size());
		assertEstadistica("Armenia", 4L, r.get(0));
		assertEstadistica("Buenavista", 2L, r.get(1));
		assertEstadistica("Pijao", 1L, r.get(2));
	}

	/** Valores de ConsultasTest.estadisticaPorAnioTest: siete anios ascendentes con 1 vehiculo cada uno. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void vehiculosPorAnioTest() {
		List<EstadisticaDTO> r = administradorEJB.vehiculosPorAnio();
		String[] anios = { "1990", "2006", "2015", "2017", "2018", "2019", "2020" };
		Assert.assertEquals(anios.length, r.size());
		for (int i = 0; i < anios.length; i++) {
			assertEstadistica(anios[i], 1L, r.get(i));
		}
	}

	/**
	 * Valores de ConsultasTest.estadisticaTopBuscadosTest (los 7 vehiculos, menos de 10): orden
	 * descendente por busquedas y a lo sumo 10 elementos.
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void topVehiculosBuscadosTest() {
		List<EstadisticaDTO> r = administradorEJB.topVehiculosBuscados();
		String[] etiquetas = { "Renoult 4 NCV903", "Audi A1 DKR478", "Mazda 3 CMN781", "Mazda 3 VEV592",
				"Mazda 3 BJR464", "Renoult 4 MTR45A", "Audi A1 BSK237" };
		long[] valores = { 120, 95, 70, 57, 33, 21, 8 };
		Assert.assertEquals(etiquetas.length, r.size());
		Assert.assertTrue(r.size() <= 10);
		for (int i = 0; i < etiquetas.length; i++) {
			assertEstadistica(etiquetas[i], valores[i], r.get(i));
		}
		for (int i = 1; i < r.size(); i++) {
			Assert.assertTrue(r.get(i - 1).getValor().longValue() >= r.get(i).getValor().longValue());
		}
	}

	/** Valores de ConsultasTest.estadisticaClientesPorCiudadTest: Armenia, Pijao y Buenavista con 1 cada una. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void clientesPorCiudadTest() {
		List<EstadisticaDTO> r = administradorEJB.clientesPorCiudad();
		Assert.assertEquals(3, r.size());
		Map<String, Long> m = aMapaLong(r);
		Assert.assertEquals(Long.valueOf(1), m.get("Armenia"));
		Assert.assertEquals(Long.valueOf(1), m.get("Pijao"));
		Assert.assertEquals(Long.valueOf(1), m.get("Buenavista"));
	}

	/**
	 * Valores de ConsultasTest.estadisticaPreguntasPorVehiculoTest: "Renoult 4 - NCV903" 2 (primero),
	 * "Mazda 3 - VEV592" 1 y "Mazda 3 - BJR464" 1.
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void preguntasPorVehiculoTest() {
		List<EstadisticaDTO> r = administradorEJB.preguntasPorVehiculo();
		Assert.assertEquals(3, r.size());
		assertEstadistica("Renoult 4 - NCV903", 2L, r.get(0));
		Map<String, Long> m = aMapaLong(r);
		Assert.assertEquals(Long.valueOf(1), m.get("Mazda 3 - VEV592"));
		Assert.assertEquals(Long.valueOf(1), m.get("Mazda 3 - BJR464"));
	}

	/** Valores de ConsultasTest.estadisticaCaracteristicasFrecuentesTest: Aire Acondicionado 2 y Luces Led 1. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void caracteristicasFrecuentesTest() {
		List<EstadisticaDTO> r = administradorEJB.caracteristicasFrecuentes();
		Assert.assertEquals(2, r.size());
		assertEstadistica("Aire Acondicionado", 2L, r.get(0));
		assertEstadistica("Luces Led", 1L, r.get(1));
	}

	/** Valores de ConsultasTest.estadisticaVentasPorMesTest: 2020-08, 2020-09 y 2020-10 con una venta cada una. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void ventasPorMesTest() {
		List<EstadisticaDTO> r = administradorEJB.ventasPorMes();
		Assert.assertEquals(3, r.size());
		assertEstadistica("2020-08", 1L, r.get(0));
		assertEstadistica("2020-09", 1L, r.get(1));
		assertEstadistica("2020-10", 1L, r.get(2));
	}

	/** Valores de ConsultasTest.estadisticaPromedioPorVendedorTest: Ruben zuluaga 4.5 y Rodolfo el Grande 3.0. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void promedioCalificacionPorVendedorTest() {
		List<EstadisticaDTO> r = administradorEJB.promedioCalificacionPorVendedor();
		Assert.assertEquals(2, r.size());
		assertEstadistica("Ruben zuluaga", 4.5, r.get(0));
		assertEstadistica("Rodolfo el Grande", 3.0, r.get(1));
	}

	/**
	 * Valores de ConsultasTest.estadisticaCombustiblePorTipoTest: la serie es el combustible;
	 * CAMIONETA/DISEL 1, CARRO/GASOLINA 5 y MOTOCICLETA/GASOLINA 1.
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void combustiblePorTipoTest() {
		List<EstadisticaDTO> r = administradorEJB.combustiblePorTipo();
		Assert.assertEquals(3, r.size());
		Map<String, Long> m = new HashMap<>();
		for (EstadisticaDTO e : r) {
			Assert.assertNotNull(e.getSerie());
			m.put(e.getEtiqueta() + "/" + e.getSerie(), e.getValor().longValue());
		}
		Assert.assertEquals(Long.valueOf(1), m.get("CAMIONETA/DISEL"));
		Assert.assertEquals(Long.valueOf(5), m.get("CARRO/GASOLINA"));
		Assert.assertEquals(Long.valueOf(1), m.get("MOTOCICLETA/GASOLINA"));
	}

	/** Valores de ConsultasTest.puntosPrecioKilometrajeTest: pares kilometros:precio de los 7 vehiculos. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void puntosPrecioKilometrajeTest() {
		List<PuntoDTO> r = administradorEJB.puntosPrecioKilometraje();
		Set<String> obtenidos = new HashSet<>();
		for (PuntoDTO p : r) {
			obtenidos.add(p.getX().intValue() + ":" + Math.round(p.getY().doubleValue()));
		}
		Set<String> esperados = new HashSet<>(Arrays.asList("10000:26000000", "100000:6000000", "30000:6500000",
				"10000:5000000", "0:64000000", "15000:4500000", "45000:38000000"));
		Assert.assertEquals(7, r.size());
		Assert.assertEquals(esperados, obtenidos);
	}

	/** Valores de ConsultasTest.puntosPrecioAnioBusquedasTest: ternas anio:precio:busquedas de los 7 vehiculos. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void puntosPrecioAnioBusquedasTest() {
		List<PuntoDTO> r = administradorEJB.puntosPrecioAnioBusquedas();
		Set<String> obtenidos = new HashSet<>();
		for (PuntoDTO p : r) {
			obtenidos.add(p.getX().intValue() + ":" + Math.round(p.getY().doubleValue()) + ":"
					+ p.getTamano().intValue());
		}
		Set<String> esperados = new HashSet<>(Arrays.asList("2018:26000000:57", "1990:6000000:120",
				"2006:6500000:33", "2015:5000000:8", "2020:64000000:95", "2019:4500000:21", "2017:38000000:70"));
		Assert.assertEquals(7, r.size());
		Assert.assertEquals(esperados, obtenidos);
	}

	// ------------------------------------------------------------------
	// Serializacion (paso por IIOP al escritorio)
	// ------------------------------------------------------------------

	/**
	 * Serializa cada lista devuelta con ObjectOutputStream. Esperado: ninguna lanza excepcion,
	 * porque las listas viajan por IIOP al escritorio.
	 *
	 * @throws Exception si alguna lista no es serializable
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void listasSerializablesTest() throws Exception {
		List<List<EstadisticaDTO>> listas = new ArrayList<>();
		listas.add(administradorEJB.vehiculosPorTipo());
		listas.add(administradorEJB.vehiculosPorCondicion());
		listas.add(administradorEJB.vehiculosPorTransmision());
		listas.add(administradorEJB.vehiculosPorCombustible());
		listas.add(administradorEJB.vehiculosPorMarca());
		listas.add(administradorEJB.precioPromedioPorMarca());
		listas.add(administradorEJB.vehiculosPorCiudad());
		listas.add(administradorEJB.vehiculosPorAnio());
		listas.add(administradorEJB.topVehiculosBuscados());
		listas.add(administradorEJB.clientesPorCiudad());
		listas.add(administradorEJB.preguntasPorVehiculo());
		listas.add(administradorEJB.caracteristicasFrecuentes());
		listas.add(administradorEJB.ventasPorMes());
		listas.add(administradorEJB.promedioCalificacionPorVendedor());
		listas.add(administradorEJB.combustiblePorTipo());
		Assert.assertEquals(15, listas.size());
		for (List<EstadisticaDTO> l : listas) {
			Assert.assertFalse(l.isEmpty());
			serializar(l);
		}
		serializar(administradorEJB.puntosPrecioKilometraje());
		serializar(administradorEJB.puntosPrecioAnioBusquedas());
	}
}
