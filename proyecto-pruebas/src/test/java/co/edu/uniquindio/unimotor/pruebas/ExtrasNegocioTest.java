package co.edu.uniquindio.unimotor.pruebas;

import java.util.ArrayList;
import java.util.Arrays;
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
import co.edu.uniquindio.unimotor.entidades.Calificacion;
import co.edu.uniquindio.unimotor.entidades.Caracteristica;
import co.edu.uniquindio.unimotor.entidades.Ciudad;
import co.edu.uniquindio.unimotor.entidades.Cliente;
import co.edu.uniquindio.unimotor.entidades.Condicion;
import co.edu.uniquindio.unimotor.entidades.Modelo;
import co.edu.uniquindio.unimotor.entidades.TipoCombustible;
import co.edu.uniquindio.unimotor.entidades.TipoTrasmision;
import co.edu.uniquindio.unimotor.entidades.TipoVehiculo;
import co.edu.uniquindio.unimotor.entidades.Vehiculo;
import co.edu.uniquindio.unimotor.entidades.Venta;
import co.edu.uniquindio.unimotor.excepciones.AutenticacionException;
import co.edu.uniquindio.unimotor.excepciones.ElementoNoEncontradoException;
import co.edu.uniquindio.unimotor.excepciones.ElementoRepetidoException;
import co.edu.uniquindio.unimotor.excepciones.InformacionInvalidaException;

/**
 * Pruebas in-container de la Fase 2.4: {@link VentaEJB} y {@link CalificacionEJB},
 * verificadas contra el contrato de la API y no contra la implementacion.
 * <p>
 * Cada prueba corre en una transaccion con rollback sobre los 14 datasets JSON de
 * src/test/resources. El correo funciona en modo simulado porque el GlassFish embebido no
 * tiene el recurso {@code mail/unimotor}. No se despliega {@code SetupEJB}.
 * <p>
 * Datos de los JSON que se usan (resumen):
 * <ul>
 * <li>Clientes: usuario 4 (usuario1@servidor.com), 5 (usuario2@servidor.com) y 6
 * (usuario3@servidor.com).</li>
 * <li>Vehiculos activos: 1 (NUEVO, dueno 4), 3 (USADO, dueno 5), 4 (USADO, dueno 4) y
 * 5 (NUEVO, dueno 4). Inactivos y vendidos: 2 (dueno 6), 6 (dueno 5) y 7 (dueno 6).</li>
 * <li>Ventas (comprador): la 1 es el vehiculo 2 comprado por el 5 (fecha 2020-10-24), la 2 es el
 * vehiculo 6 comprado por el 6 (2020-08-30) y la 3 es el vehiculo 7 comprado por el 4
 * (2020-09-12). Por tanto el vendedor 6 vendio dos vehiculos, el 5 uno y el 4 ninguno.</li>
 * <li>Calificaciones: 1 (calificador 5, vendedor 6, 5 estrellas), 2 (calificador 4, vendedor 6,
 * 4 estrellas) y 3 (calificador 6, vendedor 5, 3 estrellas). Promedios: usuario3 = 4.5,
 * usuario2 = 3.0 y usuario1 sin calificaciones (null).</li>
 * <li>Todas las compras de los JSON ya fueron calificadas, asi que los casos de calificacion
 * nueva crean la venta con {@code registrarVenta} dentro de la prueba.</li>
 * </ul>
 */
@RunWith(Arquillian.class)
@UsingDataSet({ Datasets.MODELO, Datasets.ADMINISTRADOR, Datasets.CARACTERISTICA, Datasets.CARACTERISTICA_VEHICULO,
		Datasets.CIUDAD, Datasets.CLIENTE, Datasets.FAVORITO, Datasets.FOTO_VEHICULO, Datasets.PREGUNTA,
		Datasets.TELEFONO_USUARIO, Datasets.USUARIO, Datasets.VEHICULO, Datasets.VENTA, Datasets.CALIFICACION })
public class ExtrasNegocioTest {

	private static final String EMAIL1 = "usuario1@servidor.com";
	private static final String EMAIL2 = "usuario2@servidor.com";
	private static final String EMAIL3 = "usuario3@servidor.com";
	private static final String EMAIL_INEXISTENTE = "nadie@servidor.com";
	private static final double DELTA = 0.001;

	@EJB
	private VentaEJB ventaEJB;

	@EJB
	private CalificacionEJB calificacionEJB;

	@EJB
	private VehiculoEJB vehiculoEJB;

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
		v.setDescripcion("Vehiculo de prueba de extras");
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
		v.setUrl_foto(new ArrayList<>(Arrays.asList("/imagenes/extra1.jpg")));
		v.setCaracteristicas(new ArrayList<>(Arrays.asList(entityManager.find(Caracteristica.class, 1L))));
		return v;
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
	 * Obtiene los codigos de venta de una lista, en el mismo orden.
	 *
	 * @param lista ventas
	 * @return codigos de venta
	 */
	private List<Long> codigosVenta(List<Venta> lista) {
		List<Long> r = new ArrayList<>();
		for (Venta v : lista) {
			r.add(v.getCodigo_venta());
		}
		return r;
	}

	/**
	 * Obtiene los codigos de calificacion de una lista como conjunto.
	 *
	 * @param lista calificaciones
	 * @return conjunto de codigos
	 */
	private Set<Long> codigosCalificacion(List<Calificacion> lista) {
		Set<Long> r = new HashSet<>();
		for (Calificacion c : lista) {
			r.add(c.getCodigo_calificacion());
		}
		return r;
	}

	/**
	 * Crea un comentario de la longitud dada.
	 *
	 * @param longitud numero de caracteres
	 * @return texto de esa longitud
	 */
	private String comentarioDe(int longitud) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < longitud; i++) {
			sb.append('a');
		}
		return sb.toString();
	}

	/**
	 * Prepara una compra que aun no esta calificada: el usuario2 (dueno del vehiculo 3) le vende
	 * el vehiculo 3 al usuario1.
	 *
	 * @return la venta registrada
	 * @throws Exception si el negocio rechaza la venta
	 */
	private Venta ventaDeUsuario2AUsuario1() throws Exception {
		Venta v = ventaEJB.registrarVenta(3L, EMAIL2, EMAIL1);
		entityManager.flush();
		return v;
	}

	// ------------------------------------------------------------------
	// VentaEJB.registrarVenta
	// ------------------------------------------------------------------

	/**
	 * Vender el vehiculo 5 (activo, NUEVO, dueno usuario1) al usuario3. Esperado: la venta tiene
	 * identificador, fecha y comprador; el vehiculo queda inactivo con su venta, ya no sale en
	 * las busquedas ni en el top 10 de nuevos, y la venta aparece en listarVentas del vendedor,
	 * listarCompras del comprador y se encuentra con buscarVenta.
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void registrarVentaTest() throws Exception {
		// Antes de vender, el vehiculo 5 sale en las busquedas y en el top 10 de nuevos (JSON: activo).
		Assert.assertTrue(codigos(vehiculoEJB.buscarVehiculos(null, null, null, null, null, null)).contains(5L));
		Assert.assertTrue(codigos(vehiculoEJB.top10MasBuscados(Condicion.NUEVO)).contains(5L));

		Venta venta = ventaEJB.registrarVenta(5L, EMAIL1, EMAIL3);
		entityManager.flush();

		Assert.assertNotNull(venta);
		Assert.assertNotNull(venta.getCodigo_venta());
		Assert.assertNotNull(venta.getFecha());
		Assert.assertEquals(Long.valueOf(5), venta.getCodigo_vehiculo().getCodigo_vehiculo());
		Assert.assertEquals(EMAIL3, venta.getCodigo_usuario().getEmail());
		Assert.assertFalse(venta.getCodigo_vehiculo().isActivo());
		Assert.assertNotNull(venta.getCodigo_vehiculo().getCodigo_venta());

		Assert.assertFalse(codigos(vehiculoEJB.buscarVehiculos(null, null, null, null, null, null)).contains(5L));
		Assert.assertFalse(codigos(vehiculoEJB.top10MasBuscados(Condicion.NUEVO)).contains(5L));

		Assert.assertTrue(codigosVenta(ventaEJB.listarVentas(EMAIL1)).contains(venta.getCodigo_venta()));
		Assert.assertTrue(codigosVenta(ventaEJB.listarCompras(EMAIL3)).contains(venta.getCodigo_venta()));

		Venta buscada = ventaEJB.buscarVenta(venta.getCodigo_venta());
		Assert.assertEquals(venta.getCodigo_venta(), buscada.getCodigo_venta());
		Assert.assertEquals(Long.valueOf(5), buscada.getCodigo_vehiculo().getCodigo_vehiculo());
	}

	/**
	 * Vender un vehiculo invalidado (inactivo) que no tiene venta: se invalida el vehiculo 4
	 * (dueno usuario1) y se vende al usuario2. Esperado: se permite y se registra la venta.
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void registrarVentaVehiculoInvalidadoTest() throws Exception {
		vehiculoEJB.invalidarVehiculo(4L, EMAIL1);
		entityManager.flush();
		Assert.assertFalse(vehiculoEJB.buscarVehiculo(4L).isActivo());

		Venta venta = ventaEJB.registrarVenta(4L, EMAIL1, EMAIL2);
		entityManager.flush();
		Assert.assertNotNull(venta.getCodigo_venta());
		Assert.assertEquals(Long.valueOf(4), venta.getCodigo_vehiculo().getCodigo_vehiculo());
		Assert.assertEquals(EMAIL2, venta.getCodigo_usuario().getEmail());
	}

	/**
	 * Vender el vehiculo 2, que ya tiene la venta 1 en los JSON (comprado por usuario2).
	 * Esperado: InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void registrarVentaVehiculoYaVendidoTest() throws Exception {
		// El vehiculo 2 es del usuario3, por lo que el unico motivo de rechazo es que ya fue vendido.
		ventaEJB.registrarVenta(2L, EMAIL3, EMAIL1);
	}

	/**
	 * Vender dos veces seguidas el mismo vehiculo: la primera venta es valida, la segunda debe
	 * fallar con InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void registrarVentaDosVecesTest() throws Exception {
		ventaEJB.registrarVenta(1L, EMAIL1, EMAIL2);
		entityManager.flush();
		ventaEJB.registrarVenta(1L, EMAIL1, EMAIL3);
	}

	/**
	 * Registrar la venta del vehiculo 1 (dueno usuario1) a nombre del usuario2, que no es el
	 * dueno. Esperado: InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void registrarVentaNoDuenoTest() throws Exception {
		ventaEJB.registrarVenta(1L, EMAIL2, EMAIL3);
	}

	/**
	 * Vender un vehiculo a si mismo (vendedor igual a comprador). Esperado:
	 * InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void registrarVentaCompradorIgualVendedorTest() throws Exception {
		ventaEJB.registrarVenta(1L, EMAIL1, EMAIL1);
	}

	/**
	 * Vender a un comprador que no existe. Esperado: ElementoNoEncontradoException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = ElementoNoEncontradoException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void registrarVentaCompradorInexistenteTest() throws Exception {
		ventaEJB.registrarVenta(1L, EMAIL1, EMAIL_INEXISTENTE);
	}

	/**
	 * Vender un vehiculo que no existe. Esperado: ElementoNoEncontradoException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = ElementoNoEncontradoException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void registrarVentaVehiculoInexistenteTest() throws Exception {
		ventaEJB.registrarVenta(9999L, EMAIL1, EMAIL2);
	}

	/**
	 * Vender con el codigo de vehiculo nulo. Esperado: InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void registrarVentaCodigoNuloTest() throws Exception {
		ventaEJB.registrarVenta(null, EMAIL1, EMAIL2);
	}

	/**
	 * Vender con el email del vendedor nulo. Esperado: InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void registrarVentaVendedorNuloTest() throws Exception {
		ventaEJB.registrarVenta(1L, null, EMAIL2);
	}

	/**
	 * Vender con el email del comprador nulo. Esperado: InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void registrarVentaCompradorNuloTest() throws Exception {
		ventaEJB.registrarVenta(1L, EMAIL1, null);
	}

	// ------------------------------------------------------------------
	// VentaEJB: listados y busqueda
	// ------------------------------------------------------------------

	/**
	 * Compras de los JSON: usuario1 compro la venta 3, usuario2 la venta 1 y usuario3 la venta 2.
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void listarComprasTest() throws Exception {
		Assert.assertEquals(Arrays.asList(3L), codigosVenta(ventaEJB.listarCompras(EMAIL1)));
		Assert.assertEquals(Arrays.asList(1L), codigosVenta(ventaEJB.listarCompras(EMAIL2)));
		Assert.assertEquals(Arrays.asList(2L), codigosVenta(ventaEJB.listarCompras(EMAIL3)));
	}

	/**
	 * Ventas de los JSON: usuario3 vendio los vehiculos 2 y 7 (ventas 1 y 3, ordenadas de la mas
	 * reciente a la mas antigua: 2020-10-24 y 2020-09-12), usuario2 vendio el 6 (venta 2) y
	 * usuario1 no vendio nada.
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void listarVentasTest() throws Exception {
		Assert.assertEquals(Arrays.asList(1L, 3L), codigosVenta(ventaEJB.listarVentas(EMAIL3)));
		Assert.assertEquals(Arrays.asList(2L), codigosVenta(ventaEJB.listarVentas(EMAIL2)));
		Assert.assertTrue(ventaEJB.listarVentas(EMAIL1).isEmpty());
	}

	/**
	 * Un email sin movimientos (no existe en los JSON) da listas vacias.
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void listarSinMovimientosTest() throws Exception {
		Assert.assertTrue(ventaEJB.listarCompras(EMAIL_INEXISTENTE).isEmpty());
		Assert.assertTrue(ventaEJB.listarVentas(EMAIL_INEXISTENTE).isEmpty());
	}

	/**
	 * Buscar la venta 1 de los JSON: vehiculo 2 comprado por usuario2.
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void buscarVentaTest() throws Exception {
		Venta v = ventaEJB.buscarVenta(1L);
		Assert.assertEquals(Long.valueOf(2), v.getCodigo_vehiculo().getCodigo_vehiculo());
		Assert.assertEquals(EMAIL2, v.getCodigo_usuario().getEmail());
	}

	/**
	 * Buscar una venta inexistente. Esperado: ElementoNoEncontradoException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = ElementoNoEncontradoException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void buscarVentaInexistenteTest() throws Exception {
		ventaEJB.buscarVenta(9999L);
	}

	/**
	 * Buscar una venta con codigo nulo. Esperado: ElementoNoEncontradoException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = ElementoNoEncontradoException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void buscarVentaNulaTest() throws Exception {
		ventaEJB.buscarVenta(null);
	}

	// ------------------------------------------------------------------
	// CalificacionEJB.calificarVendedor
	// ------------------------------------------------------------------

	/**
	 * El usuario1 le compra el vehiculo 3 al usuario2 y lo califica con 4 estrellas. Esperado:
	 * la calificacion tiene identificador, el promedio del usuario2 pasa de 3.0 (JSON: una
	 * calificacion de 3) a 3.5 y la calificacion aparece en la lista del vendedor.
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void calificarVendedorTest() throws Exception {
		ventaDeUsuario2AUsuario1();
		Assert.assertEquals(3.0, calificacionEJB.promedioVendedor(EMAIL2), DELTA);

		Calificacion c = calificacionEJB.calificarVendedor(EMAIL1, EMAIL2, 4, "  Buen trato  ");
		entityManager.flush();

		Assert.assertNotNull(c.getCodigo_calificacion());
		Assert.assertEquals(4, c.getEstrellas());
		Assert.assertEquals("Buen trato", c.getComentario());
		Assert.assertNotNull(c.getFecha());
		Assert.assertEquals(EMAIL1, c.getCalificador().getEmail());
		Assert.assertEquals(EMAIL2, c.getVendedor().getEmail());
		Assert.assertEquals(3.5, calificacionEJB.promedioVendedor(EMAIL2), DELTA);

		Set<Long> codigos = codigosCalificacion(calificacionEJB.listarCalificacionesVendedor(EMAIL2));
		Assert.assertTrue(codigos.contains(c.getCodigo_calificacion()));
		Assert.assertTrue(codigos.contains(3L));
		Assert.assertEquals(2, codigos.size());
	}

	/**
	 * Calificar sin comentario: el comentario es opcional, una cadena en blanco queda como nulo.
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void calificarVendedorSinComentarioTest() throws Exception {
		ventaDeUsuario2AUsuario1();
		Calificacion c = calificacionEJB.calificarVendedor(EMAIL1, EMAIL2, 5, "   ");
		Assert.assertNull(c.getComentario());
		Assert.assertEquals(5, c.getEstrellas());
	}

	/**
	 * Un comentario de exactamente 500 caracteres es valido.
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void calificarVendedorComentarioLimiteTest() throws Exception {
		ventaDeUsuario2AUsuario1();
		Calificacion c = calificacionEJB.calificarVendedor(EMAIL1, EMAIL2, 2, comentarioDe(500));
		Assert.assertEquals(500, c.getComentario().length());
	}

	/**
	 * El usuario3 no le ha comprado nada al usuario1 (en los JSON solo compro al usuario2).
	 * Esperado: InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void calificarVendedorSinCompraTest() throws Exception {
		calificacionEJB.calificarVendedor(EMAIL3, EMAIL1, 5, "Sin compra");
	}

	/**
	 * El usuario2 compro al usuario3 (venta 1) y ya lo califico (calificacion 1). Esperado:
	 * ElementoRepetidoException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = ElementoRepetidoException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void calificarVendedorRepetidaTest() throws Exception {
		calificacionEJB.calificarVendedor(EMAIL2, EMAIL3, 3, "Otra vez");
	}

	/**
	 * Calificar con cero estrellas. Esperado: InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void calificarVendedorCeroEstrellasTest() throws Exception {
		ventaDeUsuario2AUsuario1();
		calificacionEJB.calificarVendedor(EMAIL1, EMAIL2, 0, "Cero");
	}

	/**
	 * Calificar con seis estrellas. Esperado: InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void calificarVendedorSeisEstrellasTest() throws Exception {
		ventaDeUsuario2AUsuario1();
		calificacionEJB.calificarVendedor(EMAIL1, EMAIL2, 6, "Seis");
	}

	/**
	 * Un cliente no puede calificarse a si mismo. Esperado: InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void calificarVendedorAutoCalificacionTest() throws Exception {
		calificacionEJB.calificarVendedor(EMAIL1, EMAIL1, 5, "Yo mismo");
	}

	/**
	 * Comentario de 501 caracteres. Esperado: InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void calificarVendedorComentarioLargoTest() throws Exception {
		ventaDeUsuario2AUsuario1();
		calificacionEJB.calificarVendedor(EMAIL1, EMAIL2, 4, comentarioDe(501));
	}

	/**
	 * El vendedor no existe. Esperado: ElementoNoEncontradoException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = ElementoNoEncontradoException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void calificarVendedorInexistenteTest() throws Exception {
		calificacionEJB.calificarVendedor(EMAIL1, EMAIL_INEXISTENTE, 4, "Nadie");
	}

	/**
	 * El calificador no existe. Esperado: ElementoNoEncontradoException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = ElementoNoEncontradoException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void calificarCalificadorInexistenteTest() throws Exception {
		calificacionEJB.calificarVendedor(EMAIL_INEXISTENTE, EMAIL1, 4, "Nadie");
	}

	/**
	 * Email del calificador nulo. Esperado: InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void calificarCalificadorNuloTest() throws Exception {
		calificacionEJB.calificarVendedor(null, EMAIL1, 4, "Nulo");
	}

	/**
	 * Email del vendedor nulo. Esperado: InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void calificarVendedorNuloTest() throws Exception {
		calificacionEJB.calificarVendedor(EMAIL1, null, 4, "Nulo");
	}

	// ------------------------------------------------------------------
	// CalificacionEJB: consultas
	// ------------------------------------------------------------------

	/**
	 * Calificaciones de los JSON: usuario3 recibio la 1 y la 2, usuario2 la 3 y usuario1 ninguna.
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void listarCalificacionesVendedorTest() throws Exception {
		Assert.assertEquals(new HashSet<Long>(Arrays.asList(1L, 2L)),
				codigosCalificacion(calificacionEJB.listarCalificacionesVendedor(EMAIL3)));
		Assert.assertEquals(new HashSet<Long>(Arrays.asList(3L)),
				codigosCalificacion(calificacionEJB.listarCalificacionesVendedor(EMAIL2)));
		Assert.assertTrue(calificacionEJB.listarCalificacionesVendedor(EMAIL1).isEmpty());
		Assert.assertTrue(calificacionEJB.listarCalificacionesVendedor(EMAIL_INEXISTENTE).isEmpty());
	}

	/**
	 * Promedios de los JSON: usuario3 = (5 + 4) / 2 = 4.5, usuario2 = 3.0, usuario1 y un email
	 * inexistente sin calificaciones = null.
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void promedioVendedorTest() throws Exception {
		Assert.assertEquals(4.5, calificacionEJB.promedioVendedor(EMAIL3), DELTA);
		Assert.assertEquals(3.0, calificacionEJB.promedioVendedor(EMAIL2), DELTA);
		Assert.assertNull(calificacionEJB.promedioVendedor(EMAIL1));
		Assert.assertNull(calificacionEJB.promedioVendedor(EMAIL_INEXISTENTE));
	}

	// ------------------------------------------------------------------
	// CalificacionEJB.puedeCalificar
	// ------------------------------------------------------------------

	/**
	 * Con compra y sin calificacion previa es verdadero: tras vender el vehiculo 3 del usuario2 al
	 * usuario1, el usuario1 puede calificar al usuario2.
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void puedeCalificarVerdaderoTest() throws Exception {
		ventaDeUsuario2AUsuario1();
		Assert.assertTrue(calificacionEJB.puedeCalificar(EMAIL1, EMAIL2));
	}

	/**
	 * Sin compra previa es falso. El usuario1 no le ha comprado al usuario2 en los JSON, y el
	 * usuario3 no le ha comprado al usuario1.
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void puedeCalificarSinCompraTest() throws Exception {
		Assert.assertFalse(calificacionEJB.puedeCalificar(EMAIL1, EMAIL2));
		Assert.assertFalse(calificacionEJB.puedeCalificar(EMAIL3, EMAIL1));
	}

	/**
	 * Ya calificado es falso: usuario2 a usuario3, usuario3 a usuario2 y usuario1 a usuario3 ya
	 * tienen compra y calificacion en los JSON.
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void puedeCalificarYaCalificadoTest() throws Exception {
		Assert.assertFalse(calificacionEJB.puedeCalificar(EMAIL2, EMAIL3));
		Assert.assertFalse(calificacionEJB.puedeCalificar(EMAIL3, EMAIL2));
		Assert.assertFalse(calificacionEJB.puedeCalificar(EMAIL1, EMAIL3));
	}

	/**
	 * El mismo usuario nunca puede calificarse. Tampoco lanza excepcion con nulos.
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void puedeCalificarMismoUsuarioTest() throws Exception {
		Assert.assertFalse(calificacionEJB.puedeCalificar(EMAIL1, EMAIL1));
		Assert.assertFalse(calificacionEJB.puedeCalificar(null, EMAIL1));
		Assert.assertFalse(calificacionEJB.puedeCalificar(EMAIL1, null));
	}

	/**
	 * Con un email inexistente es falso, sea el calificador o el vendedor.
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void puedeCalificarInexistenteTest() throws Exception {
		Assert.assertFalse(calificacionEJB.puedeCalificar(EMAIL_INEXISTENTE, EMAIL1));
		Assert.assertFalse(calificacionEJB.puedeCalificar(EMAIL1, EMAIL_INEXISTENTE));
	}

	// ------------------------------------------------------------------
	// Flujo completo
	// ------------------------------------------------------------------

	/**
	 * Flujo completo: el usuario1 publica un vehiculo, se lo vende al usuario3, el usuario3 puede
	 * calificarlo, lo califica con 5 estrellas y ya no puede volver a hacerlo. El promedio del
	 * usuario1 pasa de null a 5.0.
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void flujoCompletoTest() throws Exception {
		Vehiculo publicado = vehiculoEJB.publicarVehiculo(vehiculoNuevo("FLJ123"));
		entityManager.flush();
		Assert.assertTrue(publicado.isActivo());
		Assert.assertNull(calificacionEJB.promedioVendedor(EMAIL1));
		Assert.assertFalse(calificacionEJB.puedeCalificar(EMAIL3, EMAIL1));

		Venta venta = ventaEJB.registrarVenta(publicado.getCodigo_vehiculo(), EMAIL1, EMAIL3);
		entityManager.flush();
		Assert.assertFalse(vehiculoEJB.buscarVehiculo(publicado.getCodigo_vehiculo()).isActivo());
		Assert.assertTrue(codigosVenta(ventaEJB.listarCompras(EMAIL3)).contains(venta.getCodigo_venta()));
		Assert.assertTrue(calificacionEJB.puedeCalificar(EMAIL3, EMAIL1));

		Calificacion c = calificacionEJB.calificarVendedor(EMAIL3, EMAIL1, 5, "Todo perfecto");
		entityManager.flush();
		Assert.assertNotNull(c.getCodigo_calificacion());
		Assert.assertFalse(calificacionEJB.puedeCalificar(EMAIL3, EMAIL1));
		Assert.assertEquals(5.0, calificacionEJB.promedioVendedor(EMAIL1), DELTA);
		Assert.assertEquals(1, calificacionEJB.listarCalificacionesVendedor(EMAIL1).size());
	}
}
