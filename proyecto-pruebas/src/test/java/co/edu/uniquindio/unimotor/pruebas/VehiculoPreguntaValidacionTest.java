package co.edu.uniquindio.unimotor.pruebas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
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
import co.edu.uniquindio.unimotor.entidades.Caracteristica;
import co.edu.uniquindio.unimotor.entidades.Ciudad;
import co.edu.uniquindio.unimotor.entidades.Cliente;
import co.edu.uniquindio.unimotor.entidades.Condicion;
import co.edu.uniquindio.unimotor.entidades.Modelo;
import co.edu.uniquindio.unimotor.entidades.Pregunta;
import co.edu.uniquindio.unimotor.entidades.TipoCombustible;
import co.edu.uniquindio.unimotor.entidades.TipoTrasmision;
import co.edu.uniquindio.unimotor.entidades.TipoVehiculo;
import co.edu.uniquindio.unimotor.entidades.Vehiculo;
import co.edu.uniquindio.unimotor.excepciones.AutenticacionException;
import co.edu.uniquindio.unimotor.excepciones.ElementoNoEncontradoException;
import co.edu.uniquindio.unimotor.excepciones.ElementoRepetidoException;
import co.edu.uniquindio.unimotor.excepciones.InformacionInvalidaException;

/**
 * Pruebas in-container de la Fase 2.7: validaciones de {@link VehiculoEJB} y {@link PreguntaEJB}
 * que no cubren las pruebas anteriores (datos obligatorios, rangos, entidades inexistentes,
 * relaciones con las caracteristicas, busquedas combinadas y limites de texto).
 * <p>
 * Cada prueba corre en una transaccion con rollback sobre los 14 datasets JSON de
 * src/test/resources. No se despliega {@code SetupEJB}. El correo funciona en modo simulado.
 * <p>
 * Datos de los JSON que se usan: clientes 4 (usuario1), 5 (usuario2) y 6 (usuario3);
 * vehiculos activos 1 (CARRO, Mazda, 26000000, 2018, dueno 4, caracteristica 1), 3 (CARRO,
 * Mazda, 6500000, 2006, dueno 5, caracteristica 1), 4 (CARRO, Audi, 5000000, 2015, dueno 4) y
 * 5 (CARRO, Audi, 64000000, 2020, dueno 4); el 6 (MOTOCICLETA) es inactivo. Preguntas: la 2
 * (vehiculo 1) esta sin responder.
 */
@RunWith(Arquillian.class)
@UsingDataSet({ Datasets.MODELO, Datasets.ADMINISTRADOR, Datasets.CARACTERISTICA, Datasets.CARACTERISTICA_VEHICULO,
		Datasets.CIUDAD, Datasets.CLIENTE, Datasets.FAVORITO, Datasets.FOTO_VEHICULO, Datasets.PREGUNTA,
		Datasets.TELEFONO_USUARIO, Datasets.USUARIO, Datasets.VEHICULO, Datasets.VENTA, Datasets.CALIFICACION })
public class VehiculoPreguntaValidacionTest {

	private static final String EMAIL1 = "usuario1@servidor.com";
	private static final String EMAIL2 = "usuario2@servidor.com";

	@EJB
	private VehiculoEJB vehiculoEJB;

	@EJB
	private PreguntaEJB preguntaEJB;

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

	/**
	 * Construye un vehiculo valido sin persistir (dueno cliente 4, modelo 1, ciudad 1,
	 * caracteristica 1).
	 *
	 * @param placa placa del vehiculo
	 * @return vehiculo nuevo
	 */
	private Vehiculo vehiculoNuevo(String placa) {
		Vehiculo v = new Vehiculo();
		v.setPlaca(placa);
		v.setDescripcion("Vehiculo de prueba de validaciones");
		v.setColor("Plata");
		v.setPrecio(30000000d);
		v.setYear(2021);
		v.setCilindraje(1800);
		v.setNumero_puertas(4);
		v.setKilometros(500);
		v.setCodigo_ciudad(entityManager.find(Ciudad.class, 1L));
		v.setCodigo_dueno(entityManager.find(Cliente.class, 4L));
		v.setCodigo_modelo(entityManager.find(Modelo.class, 1L));
		v.setTipo_combustible(TipoCombustible.GASOLINA);
		v.setTipo_vehiculo(TipoVehiculo.CARRO);
		v.setTipo_trasmision(TipoTrasmision.AUTOMATICO);
		v.setCondicion(Condicion.USADO);
		v.setActivo(false);
		v.setNumero_busquedas(0);
		v.setUrl_foto(new ArrayList<>(Arrays.asList("/imagenes/validacion1.jpg")));
		v.setCaracteristicas(new ArrayList<>(Arrays.asList(entityManager.find(Caracteristica.class, 1L))));
		return v;
	}

	/**
	 * Construye los datos de modificacion del vehiculo 1: los mismos de {@link #vehiculoNuevo(String)}
	 * pero con el codigo y la placa del vehiculo 1.
	 *
	 * @return vehiculo desprendido con el codigo 1
	 */
	private Vehiculo datosDeVehiculo1() {
		Vehiculo v = vehiculoNuevo("VEV592");
		v.setCodigo_vehiculo(1L);
		return v;
	}

	/**
	 * Cuenta los vehiculos de la base.
	 *
	 * @return numero de vehiculos
	 */
	private long contarVehiculos() {
		entityManager.flush();
		return entityManager.createQuery("select count(v) from Vehiculo v", Long.class).getSingleResult();
	}

	/**
	 * Cuenta las preguntas de la base.
	 *
	 * @return numero de preguntas
	 */
	private long contarPreguntas() {
		entityManager.flush();
		return entityManager.createQuery("select count(p) from Pregunta p", Long.class).getSingleResult();
	}

	/**
	 * Obtiene el conjunto de codigos de una lista de vehiculos.
	 *
	 * @param lista vehiculos
	 * @return conjunto de codigos
	 */
	private Set<Long> codigos(List<Vehiculo> lista) {
		Set<Long> r = new HashSet<>();
		for (Vehiculo v : lista) {
			r.add(v.getCodigo_vehiculo());
		}
		return r;
	}

	/**
	 * Obtiene el conjunto de codigos de una lista de caracteristicas.
	 *
	 * @param lista caracteristicas
	 * @return conjunto de codigos
	 */
	private Set<Long> codigosCaracteristicas(List<Caracteristica> lista) {
		Set<Long> r = new HashSet<>();
		for (Caracteristica c : lista) {
			r.add(c.getCodigo_caracteristica());
		}
		return r;
	}

	/**
	 * Crea un texto de la longitud dada.
	 *
	 * @param longitud numero de caracteres
	 * @return texto de esa longitud
	 */
	private String textoDe(int longitud) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < longitud; i++) {
			sb.append('a');
		}
		return sb.toString();
	}

	/**
	 * Obtiene el anio maximo permitido por el negocio (anio actual mas uno).
	 *
	 * @return anio maximo
	 */
	private int anioMaximo() {
		return Calendar.getInstance().get(Calendar.YEAR) + 1;
	}

	// ------------------------------------------------------------------
	// VehiculoEJB.publicarVehiculo
	// ------------------------------------------------------------------

	/**
	 * Publicar con el vehiculo nulo. Esperado: InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void publicarVehiculoNuloTest() throws Exception {
		vehiculoEJB.publicarVehiculo(null);
	}

	/**
	 * Publicar con la placa nula. Esperado: InformacionInvalidaException y no se inserta nada.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void publicarVehiculoPlacaNulaTest() throws Exception {
		long antes = contarVehiculos();
		try {
			vehiculoEJB.publicarVehiculo(vehiculoNuevo(null));
			Assert.fail("Debia lanzar InformacionInvalidaException");
		} catch (InformacionInvalidaException e) {
			Assert.assertEquals(antes, contarVehiculos());
		}
	}

	/**
	 * Publicar con la placa vacia (solo espacios). Esperado: InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void publicarVehiculoPlacaVaciaTest() throws Exception {
		vehiculoEJB.publicarVehiculo(vehiculoNuevo("   "));
	}

	/**
	 * Publicar con el anio 1899, por debajo del minimo 1900. Esperado: InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void publicarVehiculoAnioMuyBajoTest() throws Exception {
		Vehiculo v = vehiculoNuevo("ANI001");
		v.setYear(1899);
		vehiculoEJB.publicarVehiculo(v);
	}

	/**
	 * Publicar con un anio superior al maximo (anio actual mas dos). Esperado:
	 * InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void publicarVehiculoAnioFuturoTest() throws Exception {
		Vehiculo v = vehiculoNuevo("ANI002");
		v.setYear(anioMaximo() + 1);
		vehiculoEJB.publicarVehiculo(v);
	}

	/**
	 * Los limites del anio son validos: 1900 y el anio actual mas uno se publican sin error.
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void publicarVehiculoAnioLimitesTest() throws Exception {
		Vehiculo minimo = vehiculoNuevo("ANI003");
		minimo.setYear(1900);
		Assert.assertEquals(1900, vehiculoEJB.publicarVehiculo(minimo).getYear());
		Vehiculo maximo = vehiculoNuevo("ANI004");
		maximo.setYear(anioMaximo());
		Assert.assertEquals(anioMaximo(), vehiculoEJB.publicarVehiculo(maximo).getYear());
	}

	/**
	 * Publicar con kilometros negativos. Esperado: InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void publicarVehiculoKilometrosNegativosTest() throws Exception {
		Vehiculo v = vehiculoNuevo("KMS001");
		v.setKilometros(-1);
		vehiculoEJB.publicarVehiculo(v);
	}

	/**
	 * Publicar con el tipo de vehiculo nulo. Esperado: InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void publicarVehiculoTipoNuloTest() throws Exception {
		Vehiculo v = vehiculoNuevo("TIP001");
		v.setTipo_vehiculo(null);
		vehiculoEJB.publicarVehiculo(v);
	}

	/**
	 * Publicar con el tipo de combustible nulo. Esperado: InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void publicarVehiculoCombustibleNuloTest() throws Exception {
		Vehiculo v = vehiculoNuevo("COM001");
		v.setTipo_combustible(null);
		vehiculoEJB.publicarVehiculo(v);
	}

	/**
	 * Publicar con la transmision nula. Esperado: InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void publicarVehiculoTransmisionNulaTest() throws Exception {
		Vehiculo v = vehiculoNuevo("TRA001");
		v.setTipo_trasmision(null);
		vehiculoEJB.publicarVehiculo(v);
	}

	/**
	 * Publicar con la condicion nula. Esperado: InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void publicarVehiculoCondicionNulaTest() throws Exception {
		Vehiculo v = vehiculoNuevo("CON001");
		v.setCondicion(null);
		vehiculoEJB.publicarVehiculo(v);
	}

	/**
	 * Publicar con el modelo nulo. Esperado: InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void publicarVehiculoModeloNuloTest() throws Exception {
		Vehiculo v = vehiculoNuevo("MOD001");
		v.setCodigo_modelo(null);
		vehiculoEJB.publicarVehiculo(v);
	}

	/**
	 * Publicar con la ciudad nula. Esperado: InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void publicarVehiculoCiudadNulaTest() throws Exception {
		Vehiculo v = vehiculoNuevo("CIU001");
		v.setCodigo_ciudad(null);
		vehiculoEJB.publicarVehiculo(v);
	}

	/**
	 * Publicar con una ciudad inexistente (codigo 9999). Esperado: ElementoNoEncontradoException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = ElementoNoEncontradoException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void publicarVehiculoCiudadInexistenteTest() throws Exception {
		Vehiculo v = vehiculoNuevo("CIU002");
		Ciudad c = new Ciudad();
		c.setCodigo_ciudad(9999L);
		v.setCodigo_ciudad(c);
		vehiculoEJB.publicarVehiculo(v);
	}

	/**
	 * Publicar sin dueno. Esperado: InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void publicarVehiculoDuenoNuloTest() throws Exception {
		Vehiculo v = vehiculoNuevo("DUE001");
		v.setCodigo_dueno(null);
		vehiculoEJB.publicarVehiculo(v);
	}

	/**
	 * Publicar con un dueno inexistente (codigo 9999). Esperado: ElementoNoEncontradoException y
	 * no se inserta nada.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void publicarVehiculoDuenoInexistenteTest() throws Exception {
		long antes = contarVehiculos();
		Vehiculo v = vehiculoNuevo("DUE002");
		Cliente fantasma = new Cliente();
		fantasma.setCodigo_usuario(9999L);
		v.setCodigo_dueno(fantasma);
		try {
			vehiculoEJB.publicarVehiculo(v);
			Assert.fail("Debia lanzar ElementoNoEncontradoException");
		} catch (ElementoNoEncontradoException e) {
			Assert.assertEquals(antes, contarVehiculos());
		}
	}

	/**
	 * Publicar con una caracteristica inexistente (codigo 9999). Esperado:
	 * ElementoNoEncontradoException y no se inserta nada.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void publicarVehiculoCaracteristicaInexistenteTest() throws Exception {
		long antes = contarVehiculos();
		Vehiculo v = vehiculoNuevo("CAR001");
		Caracteristica fantasma = new Caracteristica();
		fantasma.setCodigo_caracteristica(9999L);
		v.setCaracteristicas(new ArrayList<>(Arrays.asList(fantasma)));
		try {
			vehiculoEJB.publicarVehiculo(v);
			Assert.fail("Debia lanzar ElementoNoEncontradoException");
		} catch (ElementoNoEncontradoException e) {
			Assert.assertEquals(antes, contarVehiculos());
		}
	}

	/**
	 * Publicar con las caracteristicas 1 y 3 (placa en minusculas con espacios). Esperado: se
	 * publica activo, con la placa normalizada a mayusculas, cero busquedas, y la relacion con las
	 * caracteristicas queda en ambos sentidos (la 2 no).
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void publicarVehiculoConCaracteristicasTest() throws Exception {
		Vehiculo v = vehiculoNuevo(" car777 ");
		v.setCaracteristicas(new ArrayList<>(Arrays.asList(entityManager.find(Caracteristica.class, 1L),
				entityManager.find(Caracteristica.class, 3L))));
		Vehiculo publicado = vehiculoEJB.publicarVehiculo(v);
		entityManager.flush();

		Assert.assertNotNull(publicado.getCodigo_vehiculo());
		Assert.assertEquals("CAR777", publicado.getPlaca());
		Assert.assertTrue(publicado.isActivo());
		Assert.assertEquals(0, publicado.getNumero_busquedas());
		Assert.assertEquals(new HashSet<Long>(Arrays.asList(1L, 3L)),
				codigosCaracteristicas(publicado.getCaracteristicas()));

		Long codigo = publicado.getCodigo_vehiculo();
		Assert.assertTrue(codigos(entityManager.find(Caracteristica.class, 1L).getVehiculos()).contains(codigo));
		Assert.assertTrue(codigos(entityManager.find(Caracteristica.class, 3L).getVehiculos()).contains(codigo));
		Assert.assertFalse(codigos(entityManager.find(Caracteristica.class, 2L).getVehiculos()).contains(codigo));
		Assert.assertTrue(codigos(entityManager.find(Cliente.class, 4L).getVehiculos_venta()).contains(codigo));
	}

	// ------------------------------------------------------------------
	// VehiculoEJB.modificarVehiculo
	// ------------------------------------------------------------------

	/**
	 * Modificar con el vehiculo nulo. Esperado: ElementoNoEncontradoException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = ElementoNoEncontradoException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void modificarVehiculoNuloTest() throws Exception {
		vehiculoEJB.modificarVehiculo(null, EMAIL1);
	}

	/**
	 * Modificar un vehiculo sin codigo. Esperado: ElementoNoEncontradoException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = ElementoNoEncontradoException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void modificarVehiculoSinCodigoTest() throws Exception {
		vehiculoEJB.modificarVehiculo(vehiculoNuevo("SIN001"), EMAIL1);
	}

	/**
	 * Modificar un vehiculo inexistente (codigo 9999). Esperado: ElementoNoEncontradoException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = ElementoNoEncontradoException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void modificarVehiculoInexistenteTest() throws Exception {
		Vehiculo v = vehiculoNuevo("INE001");
		v.setCodigo_vehiculo(9999L);
		vehiculoEJB.modificarVehiculo(v, EMAIL1);
	}

	/**
	 * Modificar el vehiculo 1 con la placa BSK237 del vehiculo 4. Esperado:
	 * ElementoRepetidoException y la placa del vehiculo 1 no cambia.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void modificarVehiculoPlacaDeOtroTest() throws Exception {
		Vehiculo datos = datosDeVehiculo1();
		datos.setPlaca("BSK237");
		try {
			vehiculoEJB.modificarVehiculo(datos, EMAIL1);
			Assert.fail("Debia lanzar ElementoRepetidoException");
		} catch (ElementoRepetidoException e) {
			Assert.assertEquals("VEV592", vehiculoEJB.buscarVehiculo(1L).getPlaca());
		}
	}

	/**
	 * Modificar con el email del dueno nulo. Esperado: InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void modificarVehiculoEmailDuenoNuloTest() throws Exception {
		vehiculoEJB.modificarVehiculo(datosDeVehiculo1(), null);
	}

	/**
	 * Cambiar las caracteristicas del vehiculo 1 de {1} a {2, 3}. Esperado: el vehiculo queda con
	 * {2, 3}, la caracteristica 1 ya no lo tiene y las caracteristicas 2 y 3 si (altas y bajas en
	 * ambos sentidos).
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void modificarVehiculoCambiaCaracteristicasTest() throws Exception {
		Assert.assertEquals(new HashSet<Long>(Arrays.asList(1L)),
				codigosCaracteristicas(vehiculoEJB.buscarVehiculo(1L).getCaracteristicas()));
		Vehiculo datos = datosDeVehiculo1();
		datos.setCaracteristicas(new ArrayList<>(Arrays.asList(entityManager.find(Caracteristica.class, 2L),
				entityManager.find(Caracteristica.class, 3L))));
		Vehiculo modificado = vehiculoEJB.modificarVehiculo(datos, EMAIL1);
		entityManager.flush();

		Assert.assertEquals(new HashSet<Long>(Arrays.asList(2L, 3L)),
				codigosCaracteristicas(modificado.getCaracteristicas()));
		Assert.assertFalse(codigos(entityManager.find(Caracteristica.class, 1L).getVehiculos()).contains(1L));
		Assert.assertTrue(codigos(entityManager.find(Caracteristica.class, 2L).getVehiculos()).contains(1L));
		Assert.assertTrue(codigos(entityManager.find(Caracteristica.class, 3L).getVehiculos()).contains(1L));
	}

	// ------------------------------------------------------------------
	// VehiculoEJB.invalidarVehiculo y eliminarVehiculo
	// ------------------------------------------------------------------

	/**
	 * Invalidar el vehiculo 1 (dueno usuario1) por el usuario2. Esperado:
	 * InformacionInvalidaException y el vehiculo sigue activo.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void invalidarVehiculoNoDuenoTest() throws Exception {
		try {
			vehiculoEJB.invalidarVehiculo(1L, EMAIL2);
			Assert.fail("Debia lanzar InformacionInvalidaException");
		} catch (InformacionInvalidaException e) {
			Assert.assertTrue(vehiculoEJB.buscarVehiculo(1L).isActivo());
		}
	}

	/**
	 * Invalidar un vehiculo inexistente. Esperado: ElementoNoEncontradoException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = ElementoNoEncontradoException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void invalidarVehiculoInexistenteTest() throws Exception {
		vehiculoEJB.invalidarVehiculo(9999L, EMAIL1);
	}

	/**
	 * Invalidar con el codigo nulo. Esperado: ElementoNoEncontradoException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = ElementoNoEncontradoException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void invalidarVehiculoCodigoNuloTest() throws Exception {
		vehiculoEJB.invalidarVehiculo(null, EMAIL1);
	}

	/**
	 * Eliminar el vehiculo 1 (dueno usuario1) por el usuario2. Esperado:
	 * InformacionInvalidaException y el vehiculo sigue existiendo.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void eliminarVehiculoNoDuenoTest() throws Exception {
		long antes = contarVehiculos();
		try {
			vehiculoEJB.eliminarVehiculo(1L, EMAIL2);
			Assert.fail("Debia lanzar InformacionInvalidaException");
		} catch (InformacionInvalidaException e) {
			Assert.assertEquals(antes, contarVehiculos());
			Assert.assertNotNull(entityManager.find(Vehiculo.class, 1L));
		}
	}

	/**
	 * Eliminar un vehiculo inexistente. Esperado: ElementoNoEncontradoException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = ElementoNoEncontradoException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void eliminarVehiculoInexistenteTest() throws Exception {
		vehiculoEJB.eliminarVehiculo(9999L, EMAIL1);
	}

	// ------------------------------------------------------------------
	// VehiculoEJB.buscarVehiculos con filtros combinados
	// ------------------------------------------------------------------

	/**
	 * Tipo CARRO, marca Mazda y precio maximo 10000000. Esperado: solo el vehiculo 3 (el 1 cuesta
	 * 26000000).
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void buscarVehiculosTipoMarcaPrecioTest() throws Exception {
		Assert.assertEquals(new HashSet<Long>(Arrays.asList(3L)),
				codigos(vehiculoEJB.buscarVehiculos(TipoVehiculo.CARRO, "Mazda", null, 10000000d, null, null)));
	}

	/**
	 * Marca Audi, precio entre 5500000 y 100000000 y anio minimo 2016. Esperado: solo el vehiculo
	 * 5 (el 4 cuesta 5000000 y es de 2015).
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void buscarVehiculosMarcaPrecioAnioTest() throws Exception {
		Assert.assertEquals(new HashSet<Long>(Arrays.asList(5L)),
				codigos(vehiculoEJB.buscarVehiculos(null, "Audi", 5500000d, 100000000d, 2016, null)));
	}

	/**
	 * Marca Mazda, precio minimo 20000000 y anios entre 2010 y 2020. Esperado: solo el vehiculo 1.
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void buscarVehiculosRangosCombinadosTest() throws Exception {
		Assert.assertEquals(new HashSet<Long>(Arrays.asList(1L)),
				codigos(vehiculoEJB.buscarVehiculos(null, "Mazda", 20000000d, null, 2010, 2020)));
	}

	/**
	 * Filtros que ninguna combinacion cumple: marca Audi con anio maximo 2010, y tipo MOTOCICLETA
	 * (el unico, el vehiculo 6, esta inactivo). Esperado: listas vacias.
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void buscarVehiculosSinCoincidenciasTest() throws Exception {
		Assert.assertTrue(vehiculoEJB.buscarVehiculos(null, "Audi", null, null, null, 2010).isEmpty());
		Assert.assertTrue(vehiculoEJB.buscarVehiculos(TipoVehiculo.MOTOCICLETA, null, null, null, null, null).isEmpty());
	}

	/**
	 * Marca en blanco y marca nula equivalen a no filtrar por marca: con tipo CARRO ambas devuelven
	 * los vehiculos activos 1, 3, 4 y 5.
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void buscarVehiculosMarcaEnBlancoTest() throws Exception {
		Set<Long> activos = new HashSet<Long>(Arrays.asList(1L, 3L, 4L, 5L));
		Assert.assertEquals(activos, codigos(vehiculoEJB.buscarVehiculos(TipoVehiculo.CARRO, "  ", null, null, null, null)));
		Assert.assertEquals(activos, codigos(vehiculoEJB.buscarVehiculos(TipoVehiculo.CARRO, null, null, null, null, null)));
	}

	// ------------------------------------------------------------------
	// PreguntaEJB.hacerPregunta
	// ------------------------------------------------------------------

	/**
	 * Pregunta de 501 caracteres. Esperado: InformacionInvalidaException (MAX_PREGUNTA es 500) y
	 * no se inserta nada.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void hacerPreguntaTextoLargoTest() throws Exception {
		long antes = contarPreguntas();
		try {
			preguntaEJB.hacerPregunta(EMAIL2, 1L, textoDe(501));
			Assert.fail("Debia lanzar InformacionInvalidaException");
		} catch (InformacionInvalidaException e) {
			Assert.assertEquals(antes, contarPreguntas());
		}
	}

	/**
	 * Pregunta de exactamente 500 caracteres. Esperado: se registra.
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void hacerPreguntaTextoLimiteTest() throws Exception {
		Pregunta p = preguntaEJB.hacerPregunta(EMAIL2, 1L, textoDe(500));
		entityManager.flush();
		Assert.assertNotNull(p.getCodigo_pregunta());
		Assert.assertEquals(500, p.getDescripcion().length());
	}

	/**
	 * Pregunta con el texto nulo. Esperado: InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void hacerPreguntaTextoNuloTest() throws Exception {
		preguntaEJB.hacerPregunta(EMAIL2, 1L, null);
	}

	/**
	 * Pregunta con el email nulo. Esperado: InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void hacerPreguntaEmailNuloTest() throws Exception {
		preguntaEJB.hacerPregunta(null, 1L, "Hola");
	}

	/**
	 * Pregunta con el email vacio. Esperado: InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void hacerPreguntaEmailVacioTest() throws Exception {
		preguntaEJB.hacerPregunta("   ", 1L, "Hola");
	}

	/**
	 * Pregunta con el codigo de vehiculo nulo. Esperado: InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void hacerPreguntaCodigoNuloTest() throws Exception {
		preguntaEJB.hacerPregunta(EMAIL2, null, "Hola");
	}

	// ------------------------------------------------------------------
	// PreguntaEJB.responderPregunta
	// ------------------------------------------------------------------

	/**
	 * Respuesta de 1001 caracteres a la pregunta 2. Esperado: InformacionInvalidaException
	 * (MAX_RESPUESTA es 1000) y la pregunta sigue sin respuesta.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void responderPreguntaRespuestaLargaTest() throws Exception {
		try {
			preguntaEJB.responderPregunta(2L, textoDe(1001), EMAIL1);
			Assert.fail("Debia lanzar InformacionInvalidaException");
		} catch (InformacionInvalidaException e) {
			Assert.assertNull(entityManager.find(Pregunta.class, 2L).getRespuesta());
		}
	}

	/**
	 * Respuesta de exactamente 1000 caracteres a la pregunta 2 por su dueno. Esperado: se
	 * registra con su fecha.
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void responderPreguntaRespuestaLimiteTest() throws Exception {
		Pregunta p = preguntaEJB.responderPregunta(2L, textoDe(1000), EMAIL1);
		Assert.assertEquals(1000, p.getRespuesta().length());
		Assert.assertNotNull(p.getFecha_respuesta());
	}

	/**
	 * Responder con el codigo de pregunta nulo. Esperado: ElementoNoEncontradoException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = ElementoNoEncontradoException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void responderPreguntaCodigoNuloTest() throws Exception {
		preguntaEJB.responderPregunta(null, "Respuesta", EMAIL1);
	}

	/**
	 * Responder con el email del dueno nulo. Esperado: InformacionInvalidaException y la pregunta
	 * sigue sin respuesta.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void responderPreguntaEmailDuenoNuloTest() throws Exception {
		try {
			preguntaEJB.responderPregunta(2L, "Respuesta", null);
			Assert.fail("Debia lanzar InformacionInvalidaException");
		} catch (InformacionInvalidaException e) {
			Assert.assertNull(entityManager.find(Pregunta.class, 2L).getRespuesta());
		}
	}
}
