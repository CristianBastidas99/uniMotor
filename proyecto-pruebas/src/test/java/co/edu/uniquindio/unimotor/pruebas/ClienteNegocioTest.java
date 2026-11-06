package co.edu.uniquindio.unimotor.pruebas;

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

import co.edu.uniquindio.unimotor.ejb.AdministradorEJB;
import co.edu.uniquindio.unimotor.ejb.AdministradorEJBRemote;
import co.edu.uniquindio.unimotor.ejb.CatalogoEJB;
import co.edu.uniquindio.unimotor.ejb.CiudadEJB;
import co.edu.uniquindio.unimotor.ejb.ClienteEJB;
import co.edu.uniquindio.unimotor.ejb.CorreoEJB;
import co.edu.uniquindio.unimotor.ejb.PlantillasCorreo;
import co.edu.uniquindio.unimotor.ejb.PreguntaEJB;
import co.edu.uniquindio.unimotor.ejb.VehiculoEJB;
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
 * Pruebas in-container de la capa de negocio de la Fase 2.3: {@link ClienteEJB},
 * {@link VehiculoEJB}, {@link PreguntaEJB} y {@link CatalogoEJB}, verificadas contra el
 * contrato de la API y no contra la implementacion.
 * <p>
 * Cada prueba corre en una transaccion con rollback sobre los 14 datasets JSON de
 * src/test/resources. El correo funciona en modo simulado porque el GlassFish embebido no
 * tiene el recurso {@code mail/unimotor}. No se despliega {@code SetupEJB} (es
 * {@code @Startup} y llenaria la base).
 * <p>
 * Datos de los JSON que se usan (resumen):
 * <ul>
 * <li>Clientes: usuario 4 (usuario1@servidor.com), 5 (usuario2@servidor.com) y 6
 * (usuario3@servidor.com), todos con contrasena 12345. Administrador:
 * cristia@proyecto.com con contrasena 123.</li>
 * <li>Vehiculos activos: 1 (NUEVO, dueno 4, Mazda, 26000000, 2018, 57 busquedas, caracteristica 1),
 * 3 (USADO, dueno 5, Mazda, 6500000, 2006, 33 busquedas, caracteristica 1), 4 (USADO, dueno 4,
 * Audi, 5000000, 2015, 8 busquedas) y 5 (NUEVO, dueno 4, Audi, 64000000, 2020, 95 busquedas).
 * Inactivos: 2 (dueno 6, vendido al 5), 6 (dueno 5, vendido al 6) y 7 (dueno 6, vendido al 4).</li>
 * <li>Favoritos: vehiculo 1 de los clientes 6 y 5; vehiculo 2 de los clientes 4 y 5.</li>
 * <li>Preguntas: 1 (cliente 5, vehiculo 2, respondida), 2 (cliente 5, vehiculo 1), 3 (cliente 4,
 * vehiculo 2) y 4 (cliente 4, vehiculo 3); solo la 1 tiene respuesta.</li>
 * <li>Catalogos: marcas Audi, Mazda y Renoult; modelos 1 (Audi A1), 2 (Mazda 3) y 3 (Renoult 4);
 * ciudades Armenia, Buenavista y Pijao; caracteristicas Aire Acondicionado, Luces Led y
 * Asientos de Cuero.</li>
 * </ul>
 */
@RunWith(Arquillian.class)
@UsingDataSet({ Datasets.MODELO, Datasets.ADMINISTRADOR, Datasets.CARACTERISTICA, Datasets.CARACTERISTICA_VEHICULO,
		Datasets.CIUDAD, Datasets.CLIENTE, Datasets.FAVORITO, Datasets.FOTO_VEHICULO, Datasets.PREGUNTA,
		Datasets.TELEFONO_USUARIO, Datasets.USUARIO, Datasets.VEHICULO, Datasets.VENTA, Datasets.CALIFICACION })
public class ClienteNegocioTest {

	private static final String EMAIL1 = "usuario1@servidor.com";
	private static final String EMAIL2 = "usuario2@servidor.com";
	private static final String EMAIL3 = "usuario3@servidor.com";
	private static final String PASSWORD_JSON = "12345";
	private static final String EMAIL_ADMIN = "cristia@proyecto.com";
	private static final String PASSWORD_ADMIN = "123";

	@EJB
	private ClienteEJB clienteEJB;

	@EJB
	private VehiculoEJB vehiculoEJB;

	@EJB
	private PreguntaEJB preguntaEJB;

	@EJB
	private CatalogoEJB catalogoEJB;

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
				.addPackage(AutenticacionException.class.getPackage());
	}

	// ------------------------------------------------------------------
	// Utilidades
	// ------------------------------------------------------------------

	/**
	 * Construye un cliente valido sin persistir, en la ciudad 1 (Armenia).
	 *
	 * @param email correo del cliente
	 * @return cliente nuevo
	 */
	private Cliente clienteNuevo(String email) {
		Cliente c = new Cliente();
		c.setEmail(email);
		c.setPassword("clave123");
		c.setNombre_completo("Cliente de Prueba");
		c.setDireccion("Calle 1 # 2-3");
		Map<String, String> telefonos = new HashMap<>();
		telefonos.put("celular", "3001234567");
		c.setTelefono(telefonos);
		c.setCodigo_ciudad(entityManager.find(Ciudad.class, 1L));
		return c;
	}

	/**
	 * Construye un vehiculo valido sin persistir (dueno cliente 4, modelo 1, ciudad 1,
	 * caracteristicas 1 y 2). A proposito se dan valores que el negocio debe forzar
	 * (activo falso y 50 busquedas).
	 *
	 * @param placa placa del vehiculo
	 * @return vehiculo nuevo
	 */
	private Vehiculo vehiculoNuevo(String placa) {
		Vehiculo v = new Vehiculo();
		v.setPlaca(placa);
		v.setDescripcion("Vehiculo de prueba de negocio");
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
		v.setNumero_busquedas(50);
		v.setUrl_foto(new ArrayList<>(Arrays.asList("/imagenes/nueva1.jpg")));
		v.setCaracteristicas(new ArrayList<>(Arrays.asList(entityManager.find(Caracteristica.class, 1L),
				entityManager.find(Caracteristica.class, 2L))));
		return v;
	}

	/**
	 * Publica un vehiculo con el negocio y sincroniza para que tenga identificador.
	 *
	 * @param v vehiculo a publicar
	 * @return vehiculo publicado
	 * @throws Exception si el negocio rechaza el vehiculo
	 */
	private Vehiculo publicar(Vehiculo v) throws Exception {
		Vehiculo publicado = vehiculoEJB.publicarVehiculo(v);
		entityManager.flush();
		return publicado;
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
	 * Obtiene la lista ordenada de codigos de una lista de vehiculos.
	 *
	 * @param lista vehiculos
	 * @return codigos en el mismo orden
	 */
	private List<Long> codigosEnOrden(List<Vehiculo> lista) {
		List<Long> r = new ArrayList<>();
		for (Vehiculo v : lista) {
			r.add(v.getCodigo_vehiculo());
		}
		return r;
	}

	/**
	 * Indica si una lista de vehiculos contiene el codigo dado.
	 *
	 * @param lista vehiculos
	 * @param codigo codigo buscado
	 * @return verdadero si esta
	 */
	private boolean contiene(List<Vehiculo> lista, long codigo) {
		return codigos(lista).contains(codigo);
	}

	/**
	 * Indica si una lista de preguntas contiene alguna con la descripcion dada.
	 *
	 * @param lista preguntas
	 * @param texto descripcion buscada
	 * @return verdadero si esta
	 */
	private boolean contienePregunta(List<Pregunta> lista, String texto) {
		for (Pregunta p : lista) {
			if (texto.equals(p.getDescripcion())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Obtiene el conjunto de codigos de una lista de preguntas.
	 *
	 * @param lista preguntas
	 * @return conjunto de codigos
	 */
	private Set<Long> codigosPreguntas(List<Pregunta> lista) {
		Set<Long> r = new HashSet<>();
		for (Pregunta p : lista) {
			r.add(p.getCodigo_pregunta());
		}
		return r;
	}

	// ------------------------------------------------------------------
	// ClienteEJB
	// ------------------------------------------------------------------

	/**
	 * Registra un cliente valido y luego lo encuentra por email. Esperado: se le asigna un
	 * identificador y conserva nombre y ciudad.
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void registrarClienteTest() throws Exception {
		Cliente registrado = clienteEJB.registrarCliente(clienteNuevo("nuevo@servidor.com"));
		entityManager.flush();
		Assert.assertNotNull(registrado.getCodigo_usuario());
		Cliente buscado = clienteEJB.buscarCliente("nuevo@servidor.com");
		Assert.assertEquals(registrado.getCodigo_usuario(), buscado.getCodigo_usuario());
		Assert.assertEquals("Cliente de Prueba", buscado.getNombre_completo());
		Assert.assertEquals(Long.valueOf(1), buscado.getCodigo_ciudad().getCodigo_ciudad());
	}

	/**
	 * Registrar un cliente con el email de un usuario del JSON (usuario1@servidor.com).
	 * Esperado: ElementoRepetidoException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = ElementoRepetidoException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void registrarClienteEmailRepetidoTest() throws Exception {
		clienteEJB.registrarCliente(clienteNuevo(EMAIL1));
	}

	/**
	 * Registrar un cliente con un email sin formato valido. Esperado: InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void registrarClienteEmailInvalidoTest() throws Exception {
		clienteEJB.registrarCliente(clienteNuevo("esto no es un correo"));
	}

	/**
	 * Autentica al cliente 4 con su email y contrasena del JSON. Esperado: devuelve a Hector Rodriguez.
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void autenticarClienteTest() throws Exception {
		Cliente c = clienteEJB.autenticarCliente(EMAIL1, PASSWORD_JSON);
		Assert.assertEquals(Long.valueOf(4), c.getCodigo_usuario());
		Assert.assertEquals("Hector Rodriguez", c.getNombre_completo());
	}

	/**
	 * Autentica con contrasena incorrecta. Esperado: AutenticacionException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = AutenticacionException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void autenticarClientePasswordMalaTest() throws Exception {
		clienteEJB.autenticarCliente(EMAIL1, "incorrecta");
	}

	/**
	 * Autentica con las credenciales correctas de un administrador del JSON (el usuario no es
	 * un Cliente). Esperado: AutenticacionException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = AutenticacionException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void autenticarClienteConAdministradorTest() throws Exception {
		clienteEJB.autenticarCliente(EMAIL_ADMIN, PASSWORD_ADMIN);
	}

	/**
	 * Busca un cliente inexistente. Esperado: ElementoNoEncontradoException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = ElementoNoEncontradoException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void buscarClienteInexistenteTest() throws Exception {
		clienteEJB.buscarCliente("nadie@servidor.com");
	}

	/**
	 * Modifica nombre, direccion y ciudad del cliente 4 enviando una copia desprendida. Esperado:
	 * los datos cambian y la contrasena sigue siendo la del JSON.
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void modificarClienteTest() throws Exception {
		Cliente cambios = new Cliente();
		cambios.setCodigo_usuario(4L);
		cambios.setEmail(EMAIL1);
		cambios.setPassword(PASSWORD_JSON);
		cambios.setNombre_completo("Hector Modificado");
		cambios.setDireccion("Carrera nueva 10-20");
		Map<String, String> telefonos = new HashMap<>();
		telefonos.put("celular", "3109998877");
		cambios.setTelefono(telefonos);
		cambios.setCodigo_ciudad(entityManager.find(Ciudad.class, 2L));
		Cliente r = clienteEJB.modificarCliente(cambios);
		Assert.assertEquals("Hector Modificado", r.getNombre_completo());
		Assert.assertEquals("Carrera nueva 10-20", r.getDireccion());
		Assert.assertEquals(Long.valueOf(2), r.getCodigo_ciudad().getCodigo_ciudad());
		Cliente buscado = clienteEJB.buscarCliente(EMAIL1);
		Assert.assertEquals("Hector Modificado", buscado.getNombre_completo());
		Assert.assertEquals(PASSWORD_JSON, buscado.getPassword());
	}

	/**
	 * Modifica el email del cliente 4 a uno libre. Esperado: se puede buscar con el email nuevo y
	 * el anterior deja de existir.
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void modificarClienteEmailNuevoTest() throws Exception {
		Cliente cambios = new Cliente();
		cambios.setCodigo_usuario(4L);
		cambios.setEmail("cambiado@servidor.com");
		cambios.setPassword(PASSWORD_JSON);
		cambios.setNombre_completo("Hector Rodriguez");
		cambios.setDireccion("B/ la clarita Mz F #45");
		cambios.setCodigo_ciudad(entityManager.find(Ciudad.class, 1L));
		clienteEJB.modificarCliente(cambios);
		Assert.assertEquals(Long.valueOf(4), clienteEJB.buscarCliente("cambiado@servidor.com").getCodigo_usuario());
		try {
			clienteEJB.buscarCliente(EMAIL1);
			Assert.fail("El email anterior ya no deberia existir");
		} catch (ElementoNoEncontradoException e) {
			// esperado
		}
	}

	/**
	 * Modifica el cliente 4 con el email de otro usuario (usuario2@servidor.com). Esperado:
	 * ElementoRepetidoException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = ElementoRepetidoException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void modificarClienteEmailDeOtroTest() throws Exception {
		Cliente cambios = new Cliente();
		cambios.setCodigo_usuario(4L);
		cambios.setEmail(EMAIL2);
		cambios.setPassword(PASSWORD_JSON);
		cambios.setNombre_completo("Hector Rodriguez");
		cambios.setDireccion("B/ la clarita Mz F #45");
		cambios.setCodigo_ciudad(entityManager.find(Ciudad.class, 1L));
		clienteEJB.modificarCliente(cambios);
	}

	/**
	 * Modifica un cliente cuyo identificador no existe. Esperado: ElementoNoEncontradoException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = ElementoNoEncontradoException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void modificarClienteInexistenteTest() throws Exception {
		Cliente cambios = clienteNuevo("fantasma@servidor.com");
		cambios.setCodigo_usuario(9999L);
		clienteEJB.modificarCliente(cambios);
	}

	/**
	 * Cambia la contrasena del cliente 4 con la actual correcta (12345). Esperado: autentica con
	 * la nueva y ya no con la anterior.
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void cambiarPasswordTest() throws Exception {
		clienteEJB.cambiarPassword(EMAIL1, PASSWORD_JSON, "nueva9876");
		Cliente c = clienteEJB.autenticarCliente(EMAIL1, "nueva9876");
		Assert.assertEquals(Long.valueOf(4), c.getCodigo_usuario());
		try {
			clienteEJB.autenticarCliente(EMAIL1, PASSWORD_JSON);
			Assert.fail("La contrasena anterior ya no deberia servir");
		} catch (AutenticacionException e) {
			// esperado
		}
	}

	/**
	 * Cambia la contrasena con una actual incorrecta. Esperado: AutenticacionException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = AutenticacionException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void cambiarPasswordActualIncorrectaTest() throws Exception {
		clienteEJB.cambiarPassword(EMAIL1, "equivocada", "nueva9876");
	}

	/**
	 * Cambia la contrasena por una de menos de 4 caracteres. Esperado: InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void cambiarPasswordNuevaCortaTest() throws Exception {
		clienteEJB.cambiarPassword(EMAIL1, PASSWORD_JSON, "ab");
	}

	/**
	 * Recupera la contrasena del cliente 4 (el correo es simulado). Esperado: la contrasena deja
	 * de ser 12345.
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void recuperarPasswordTest() throws Exception {
		clienteEJB.recuperarPassword(EMAIL1);
		Cliente c = clienteEJB.buscarCliente(EMAIL1);
		Assert.assertNotNull(c.getPassword());
		Assert.assertFalse(PASSWORD_JSON.equals(c.getPassword()));
	}

	/**
	 * Recupera la contrasena de un email que no existe. Esperado: ElementoNoEncontradoException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = ElementoNoEncontradoException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void recuperarPasswordEmailInexistenteTest() throws Exception {
		clienteEJB.recuperarPassword("nadie@servidor.com");
	}

	/**
	 * Elimina un cliente sin ventas ni compras. Como los clientes del JSON tienen todos ventas o
	 * compras, se crea uno con registrarCliente, se le publica un vehiculo, hace una pregunta,
	 * marca un favorito y recibe una pregunta de otro cliente. Esperado: tras eliminarlo ya no se
	 * encuentra el cliente ni su vehiculo, y sus preguntas y favoritos desaparecen sin afectar los datos del JSON.
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void eliminarClienteSinVentasTest() throws Exception {
		String email = "borrar@servidor.com";
		Cliente nuevo = clienteEJB.registrarCliente(clienteNuevo(email));
		entityManager.flush();
		Vehiculo v = vehiculoNuevo("DEL123");
		v.setCodigo_dueno(nuevo);
		Long codigoPropio = publicar(v).getCodigo_vehiculo();
		preguntaEJB.hacerPregunta(email, 1L, "Pregunta del cliente a borrar");
		preguntaEJB.hacerPregunta(EMAIL2, codigoPropio, "Pregunta recibida por el cliente a borrar");
		clienteEJB.agregarFavorito(email, 3L);
		entityManager.flush();
		Assert.assertTrue(clienteEJB.esFavorito(email, 3L));

		clienteEJB.eliminarCliente(email);
		entityManager.flush();

		try {
			clienteEJB.buscarCliente(email);
			Assert.fail("El cliente eliminado no deberia existir");
		} catch (ElementoNoEncontradoException e) {
			// esperado
		}
		try {
			vehiculoEJB.buscarVehiculo(codigoPropio);
			Assert.fail("El vehiculo del cliente eliminado no deberia existir");
		} catch (ElementoNoEncontradoException e) {
			// esperado
		}
		// El vehiculo 1 conserva solo su pregunta del JSON (la 2).
		Assert.assertEquals(1, preguntaEJB.listarPreguntasVehiculo(1L).size());
		Assert.assertFalse(clienteEJB.esFavorito(email, 3L));
		// El resto de datos del JSON siguen: cliente 5 conserva sus 2 preguntas hechas.
		Assert.assertEquals(2, preguntaEJB.listarPreguntasHechas(EMAIL2).size());
	}

	/**
	 * Elimina al cliente 4 (usuario1), que tiene una compra (venta 3, vehiculo 7). Esperado:
	 * InformacionInvalidaException y el cliente sigue existiendo.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void eliminarClienteConComprasTest() throws Exception {
		try {
			clienteEJB.eliminarCliente(EMAIL1);
			Assert.fail("Un cliente con compras no se puede eliminar");
		} catch (InformacionInvalidaException e) {
			// esperado
		}
		Assert.assertNotNull(clienteEJB.buscarCliente(EMAIL1));
	}

	/**
	 * Elimina al cliente 6 (usuario3), dueno de los vehiculos 2 y 7, ambos con venta. Esperado:
	 * InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void eliminarClienteConVentasTest() throws Exception {
		clienteEJB.eliminarCliente(EMAIL3);
	}

	/**
	 * Elimina un cliente inexistente. Esperado: ElementoNoEncontradoException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = ElementoNoEncontradoException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void eliminarClienteInexistenteTest() throws Exception {
		clienteEJB.eliminarCliente("nadie@servidor.com");
	}

	/**
	 * El cliente 4 (usuario1) agrega como favorito el vehiculo 3 (dueno 5). En el JSON solo tenia
	 * el vehiculo 2. Esperado: esFavorito es verdadero y listarFavoritos incluye los vehiculos 2 y 3.
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void agregarFavoritoTest() throws Exception {
		Assert.assertFalse(clienteEJB.esFavorito(EMAIL1, 3L));
		clienteEJB.agregarFavorito(EMAIL1, 3L);
		Assert.assertTrue(clienteEJB.esFavorito(EMAIL1, 3L));
		List<Vehiculo> favoritos = clienteEJB.listarFavoritos(EMAIL1);
		Assert.assertEquals(2, favoritos.size());
		Assert.assertTrue(contiene(favoritos, 2L));
		Assert.assertTrue(contiene(favoritos, 3L));
	}

	/**
	 * Agrega como favorito un vehiculo que ya lo es (cliente 4 y vehiculo 2 en favorito.json).
	 * Esperado: ElementoRepetidoException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = ElementoRepetidoException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void agregarFavoritoRepetidoTest() throws Exception {
		clienteEJB.agregarFavorito(EMAIL1, 2L);
	}

	/**
	 * El cliente 4 intenta marcar como favorito su propio vehiculo 4. Esperado:
	 * InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void agregarFavoritoPropioTest() throws Exception {
		clienteEJB.agregarFavorito(EMAIL1, 4L);
	}

	/**
	 * Agrega como favorito un vehiculo inexistente. Esperado: ElementoNoEncontradoException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = ElementoNoEncontradoException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void agregarFavoritoVehiculoInexistenteTest() throws Exception {
		clienteEJB.agregarFavorito(EMAIL1, 9999L);
	}

	/**
	 * Quita el favorito vehiculo 1 del cliente 5 (usuario2). Esperado: deja de ser favorito, y
	 * el cliente 6 lo conserva.
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void quitarFavoritoTest() throws Exception {
		Assert.assertTrue(clienteEJB.esFavorito(EMAIL2, 1L));
		clienteEJB.quitarFavorito(EMAIL2, 1L);
		Assert.assertFalse(clienteEJB.esFavorito(EMAIL2, 1L));
		Assert.assertFalse(contiene(clienteEJB.listarFavoritos(EMAIL2), 1L));
		Assert.assertTrue(clienteEJB.esFavorito(EMAIL3, 1L));
	}

	/**
	 * Quita un vehiculo que el cliente 6 no tiene como favorito (vehiculo 3). Esperado:
	 * ElementoNoEncontradoException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = ElementoNoEncontradoException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void quitarFavoritoNoFavoritoTest() throws Exception {
		clienteEJB.quitarFavorito(EMAIL3, 3L);
	}

	/**
	 * Lista los favoritos de los clientes del JSON. Esperado: cliente 5 tiene los vehiculos 1 y 2,
	 * cliente 6 solo el 1, y un email inexistente produce ElementoNoEncontradoException.
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void listarFavoritosTest() throws Exception {
		Assert.assertEquals(new HashSet<Long>(Arrays.asList(1L, 2L)), codigos(clienteEJB.listarFavoritos(EMAIL2)));
		Assert.assertEquals(new HashSet<Long>(Arrays.asList(1L)), codigos(clienteEJB.listarFavoritos(EMAIL3)));
		Assert.assertFalse(clienteEJB.esFavorito("nadie@servidor.com", 1L));
		Assert.assertFalse(clienteEJB.esFavorito(EMAIL1, 9999L));
		try {
			clienteEJB.listarFavoritos("nadie@servidor.com");
			Assert.fail("Un cliente inexistente no tiene favoritos");
		} catch (ElementoNoEncontradoException e) {
			// esperado
		}
	}

	// ------------------------------------------------------------------
	// VehiculoEJB
	// ------------------------------------------------------------------

	/**
	 * Publica un vehiculo valido con la placa en minusculas. Esperado: queda activo, con 0
	 * busquedas, placa en mayusculas, dos caracteristicas, el dueno indicado y sin venta, aunque la
	 * entrada traia activo falso y 50 busquedas.
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void publicarVehiculoTest() throws Exception {
		Vehiculo p = publicar(vehiculoNuevo("  abc123 "));
		Assert.assertNotNull(p.getCodigo_vehiculo());
		Assert.assertEquals("ABC123", p.getPlaca());
		Assert.assertTrue(p.isActivo());
		Assert.assertEquals(0, p.getNumero_busquedas());
		Assert.assertNull(p.getCodigo_venta());
		Assert.assertNotNull(p.getFecha_publicacion());
		Assert.assertEquals(2, p.getCaracteristicas().size());
		Assert.assertEquals(Long.valueOf(4), p.getCodigo_dueno().getCodigo_usuario());
		Vehiculo buscado = vehiculoEJB.buscarVehiculo(p.getCodigo_vehiculo());
		Assert.assertEquals("ABC123", buscado.getPlaca());
		Assert.assertTrue(contiene(vehiculoEJB.listarVehiculosActivos(), p.getCodigo_vehiculo()));
	}

	/**
	 * Publica un vehiculo con la placa de uno del JSON (VEV592, vehiculo 1) escrita en minusculas.
	 * Esperado: ElementoRepetidoException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = ElementoRepetidoException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void publicarVehiculoPlacaRepetidaTest() throws Exception {
		vehiculoEJB.publicarVehiculo(vehiculoNuevo("vev592"));
	}

	/**
	 * Publica un vehiculo con precio negativo. Esperado: InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void publicarVehiculoPrecioInvalidoTest() throws Exception {
		Vehiculo v = vehiculoNuevo("PRC001");
		v.setPrecio(-5d);
		vehiculoEJB.publicarVehiculo(v);
	}

	/**
	 * Publica un vehiculo con precio cero. Esperado: InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void publicarVehiculoPrecioCeroTest() throws Exception {
		Vehiculo v = vehiculoNuevo("PRC002");
		v.setPrecio(0d);
		vehiculoEJB.publicarVehiculo(v);
	}

	/**
	 * Publica un vehiculo con un modelo cuyo identificador (999) no existe. Esperado:
	 * ElementoNoEncontradoException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = ElementoNoEncontradoException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void publicarVehiculoModeloInexistenteTest() throws Exception {
		Vehiculo v = vehiculoNuevo("MOD001");
		Modelo fantasma = new Modelo();
		fantasma.setCodigo_modelo(999L);
		v.setCodigo_modelo(fantasma);
		vehiculoEJB.publicarVehiculo(v);
	}

	/**
	 * Modifica el vehiculo 1 (dueno usuario1) enviando una copia desprendida con otro precio y una
	 * sola caracteristica (la 3). Esperado: cambian precio y caracteristicas; no cambian el dueno,
	 * el estado activo ni las 57 busquedas.
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void modificarVehiculoTest() throws Exception {
		Vehiculo cambios = vehiculoNuevo("VEV592");
		cambios.setCodigo_vehiculo(1L);
		cambios.setPrecio(25000000d);
		cambios.setCaracteristicas(new ArrayList<>(Arrays.asList(entityManager.find(Caracteristica.class, 3L))));
		Vehiculo r = vehiculoEJB.modificarVehiculo(cambios, EMAIL1);
		Assert.assertEquals(25000000d, r.getPrecio(), 0.001);
		Assert.assertEquals(1, r.getCaracteristicas().size());
		Assert.assertEquals(Long.valueOf(3), r.getCaracteristicas().get(0).getCodigo_caracteristica());
		Vehiculo buscado = vehiculoEJB.buscarVehiculo(1L);
		Assert.assertEquals(25000000d, buscado.getPrecio(), 0.001);
		Assert.assertEquals("VEV592", buscado.getPlaca());
		Assert.assertEquals(Long.valueOf(4), buscado.getCodigo_dueno().getCodigo_usuario());
		Assert.assertTrue(buscado.isActivo());
		Assert.assertEquals(57, buscado.getNumero_busquedas());
	}

	/**
	 * Modifica el vehiculo 1 como si fuera de usuario2 (que no es el dueno). Esperado:
	 * InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void modificarVehiculoNoDuenoTest() throws Exception {
		Vehiculo cambios = vehiculoNuevo("VEV592");
		cambios.setCodigo_vehiculo(1L);
		cambios.setPrecio(1000000d);
		vehiculoEJB.modificarVehiculo(cambios, EMAIL2);
	}

	/**
	 * Invalida el vehiculo 4 (dueno usuario1, activo en el JSON). Esperado: queda inactivo, ya no
	 * aparece en listarVehiculosActivos ni en buscarVehiculos, pero sigue en listarVehiculosDeCliente.
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void invalidarVehiculoTest() throws Exception {
		Assert.assertTrue(contiene(vehiculoEJB.listarVehiculosActivos(), 4L));
		vehiculoEJB.invalidarVehiculo(4L, EMAIL1);
		Assert.assertFalse(vehiculoEJB.buscarVehiculo(4L).isActivo());
		Assert.assertFalse(contiene(vehiculoEJB.listarVehiculosActivos(), 4L));
		Assert.assertFalse(contiene(vehiculoEJB.buscarVehiculos(null, null, null, null, null, null), 4L));
		Assert.assertTrue(contiene(vehiculoEJB.listarVehiculosDeCliente(EMAIL1), 4L));
	}

	/**
	 * Elimina el vehiculo 3 (dueno usuario2, sin venta, con la pregunta 4 y la caracteristica 1).
	 * Esperado: buscarVehiculo lanza ElementoNoEncontradoException y su pregunta desaparece de las
	 * preguntas hechas por el cliente 4.
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void eliminarVehiculoSinVentaTest() throws Exception {
		Assert.assertEquals(2, preguntaEJB.listarPreguntasHechas(EMAIL1).size());
		vehiculoEJB.eliminarVehiculo(3L, EMAIL2);
		entityManager.flush();
		try {
			vehiculoEJB.buscarVehiculo(3L);
			Assert.fail("El vehiculo eliminado no deberia existir");
		} catch (ElementoNoEncontradoException e) {
			// esperado
		}
		Assert.assertFalse(contiene(vehiculoEJB.listarVehiculosDeCliente(EMAIL2), 3L));
		Assert.assertEquals(1, preguntaEJB.listarPreguntasHechas(EMAIL1).size());
	}

	/**
	 * Elimina el vehiculo 1 (dueno usuario1, sin venta), que es favorito de los clientes 5 y 6 y
	 * tiene la pregunta 2. Esperado: el vehiculo desaparece y los clientes 5 y 6 ya no lo tienen
	 * como favorito (el 5 conserva el vehiculo 2).
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void eliminarVehiculoConFavoritosTest() throws Exception {
		vehiculoEJB.eliminarVehiculo(1L, EMAIL1);
		entityManager.flush();
		Assert.assertTrue(clienteEJB.listarFavoritos(EMAIL3).isEmpty());
		Assert.assertEquals(new HashSet<Long>(Arrays.asList(2L)), codigos(clienteEJB.listarFavoritos(EMAIL2)));
		Assert.assertFalse(clienteEJB.esFavorito(EMAIL2, 1L));
		Assert.assertEquals(1, preguntaEJB.listarPreguntasHechas(EMAIL2).size());
	}

	/**
	 * Elimina el vehiculo 7 (dueno usuario3), vendido segun la venta 3. Esperado:
	 * InformacionInvalidaException y el vehiculo sigue existiendo.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void eliminarVehiculoVendidoTest() throws Exception {
		try {
			vehiculoEJB.eliminarVehiculo(7L, EMAIL3);
			Assert.fail("Un vehiculo vendido no se puede eliminar");
		} catch (InformacionInvalidaException e) {
			// esperado
		}
		Assert.assertNotNull(vehiculoEJB.buscarVehiculo(7L));
	}

	/**
	 * Busca un vehiculo inexistente. Esperado: ElementoNoEncontradoException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = ElementoNoEncontradoException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void buscarVehiculoInexistenteTest() throws Exception {
		vehiculoEJB.buscarVehiculo(9999L);
	}

	/**
	 * Busca el vehiculo 1 por codigo. Esperado: placa VEV592 y sigue con 57 busquedas, porque
	 * buscarVehiculo no suma.
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void buscarVehiculoTest() throws Exception {
		Vehiculo v = vehiculoEJB.buscarVehiculo(1L);
		Assert.assertEquals("VEV592", v.getPlaca());
		Assert.assertEquals(57, v.getNumero_busquedas());
	}

	/**
	 * Registra una visita al vehiculo 1, que tenia 57 busquedas. Esperado: 58.
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void registrarVisitaTest() throws Exception {
		Vehiculo v = vehiculoEJB.registrarVisita(1L);
		Assert.assertEquals(58, v.getNumero_busquedas());
		Assert.assertEquals(58, vehiculoEJB.buscarVehiculo(1L).getNumero_busquedas());
	}

	/**
	 * Registra una visita a un vehiculo inexistente. Esperado: ElementoNoEncontradoException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = ElementoNoEncontradoException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void registrarVisitaInexistenteTest() throws Exception {
		vehiculoEJB.registrarVisita(9999L);
	}

	/**
	 * Lista los vehiculos por dueno. Esperado: usuario1 tiene los vehiculos 1, 4 y 5; usuario3 los
	 * vehiculos 2 y 7 (activos o no); un email sin vehiculos da lista vacia.
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void listarVehiculosDeClienteTest() {
		Assert.assertEquals(new HashSet<Long>(Arrays.asList(1L, 4L, 5L)),
				codigos(vehiculoEJB.listarVehiculosDeCliente(EMAIL1)));
		Assert.assertEquals(new HashSet<Long>(Arrays.asList(2L, 7L)),
				codigos(vehiculoEJB.listarVehiculosDeCliente(EMAIL3)));
		Assert.assertTrue(vehiculoEJB.listarVehiculosDeCliente("nadie@servidor.com").isEmpty());
	}

	/**
	 * Lista los vehiculos activos. Esperado: los vehiculos 1, 3, 4 y 5 (el 2, 6 y 7 estan inactivos).
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void listarVehiculosActivosTest() {
		List<Vehiculo> activos = vehiculoEJB.listarVehiculosActivos();
		Assert.assertEquals(4, activos.size());
		Assert.assertEquals(new HashSet<Long>(Arrays.asList(1L, 3L, 4L, 5L)), codigos(activos));
	}

	/**
	 * Busca sin ningun filtro. Esperado: equivale a los vehiculos activos (1, 3, 4 y 5).
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void buscarVehiculosSinFiltrosTest() throws Exception {
		List<Vehiculo> r = vehiculoEJB.buscarVehiculos(null, null, null, null, null, null);
		Assert.assertEquals(new HashSet<Long>(Arrays.asList(1L, 3L, 4L, 5L)), codigos(r));
		List<Vehiculo> conMarcaVacia = vehiculoEJB.buscarVehiculos(null, "", null, null, null, null);
		Assert.assertEquals(4, conMarcaVacia.size());
	}

	/**
	 * Busca por tipo. Esperado: CARRO devuelve los 4 activos; MOTOCICLETA ninguno (la moto 6 esta
	 * inactiva).
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void buscarVehiculosPorTipoTest() throws Exception {
		Assert.assertEquals(4, vehiculoEJB.buscarVehiculos(TipoVehiculo.CARRO, null, null, null, null, null).size());
		Assert.assertTrue(
				vehiculoEJB.buscarVehiculos(TipoVehiculo.MOTOCICLETA, null, null, null, null, null).isEmpty());
	}

	/**
	 * Busca por marca. Esperado: Mazda devuelve los vehiculos 1 y 3 (modelo 2); Audi los 4 y 5
	 * (modelo 1); Renoult ninguno activo (modelo 3 solo lo tienen el 2 y el 6, inactivos).
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void buscarVehiculosPorMarcaTest() throws Exception {
		Assert.assertEquals(new HashSet<Long>(Arrays.asList(1L, 3L)),
				codigos(vehiculoEJB.buscarVehiculos(null, "Mazda", null, null, null, null)));
		Assert.assertEquals(new HashSet<Long>(Arrays.asList(4L, 5L)),
				codigos(vehiculoEJB.buscarVehiculos(null, "Audi", null, null, null, null)));
		Assert.assertTrue(vehiculoEJB.buscarVehiculos(null, "Renoult", null, null, null, null).isEmpty());
	}

	/**
	 * Busca por rango de precio. Esperado: entre 5000000 y 26000000 (inclusivo) salen los
	 * vehiculos 1, 3 y 4; solo con minimo 30000000 sale el 5.
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void buscarVehiculosPorPrecioTest() throws Exception {
		Assert.assertEquals(new HashSet<Long>(Arrays.asList(1L, 3L, 4L)),
				codigos(vehiculoEJB.buscarVehiculos(null, null, 5000000d, 26000000d, null, null)));
		Assert.assertEquals(new HashSet<Long>(Arrays.asList(5L)),
				codigos(vehiculoEJB.buscarVehiculos(null, null, 30000000d, null, null, null)));
	}

	/**
	 * Busca por rango de anio. Esperado: entre 2015 y 2018 salen el 1 (2018) y el 4 (2015); hasta
	 * 2010 sale solo el 3 (2006).
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void buscarVehiculosPorAnioTest() throws Exception {
		Assert.assertEquals(new HashSet<Long>(Arrays.asList(1L, 4L)),
				codigos(vehiculoEJB.buscarVehiculos(null, null, null, null, 2015, 2018)));
		Assert.assertEquals(new HashSet<Long>(Arrays.asList(3L)),
				codigos(vehiculoEJB.buscarVehiculos(null, null, null, null, null, 2010)));
	}

	/**
	 * Busca con un rango de precio invertido (minimo mayor que maximo). Esperado:
	 * InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void buscarVehiculosPrecioInvertidoTest() throws Exception {
		vehiculoEJB.buscarVehiculos(null, null, 30000000d, 1000000d, null, null);
	}

	/**
	 * Busca con un rango de anio invertido. Esperado: InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void buscarVehiculosAnioInvertidoTest() throws Exception {
		vehiculoEJB.buscarVehiculos(null, null, null, null, 2020, 2000);
	}

	/**
	 * Verifica que buscarVehiculos suma 1 a numero_busquedas de los devueltos. Esperado: buscando
	 * Mazda, el vehiculo 1 pasa de 57 a 58 y el 3 de 33 a 34; el 4 (Audi) sigue en 8.
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void buscarVehiculosSumaBusquedasTest() throws Exception {
		List<Vehiculo> r = vehiculoEJB.buscarVehiculos(null, "Mazda", null, null, null, null);
		Assert.assertEquals(2, r.size());
		for (Vehiculo v : r) {
			if (v.getCodigo_vehiculo() == 1L) {
				Assert.assertEquals(58, v.getNumero_busquedas());
			} else {
				Assert.assertEquals(3L, v.getCodigo_vehiculo().longValue());
				Assert.assertEquals(34, v.getNumero_busquedas());
			}
		}
		Assert.assertEquals(58, vehiculoEJB.buscarVehiculo(1L).getNumero_busquedas());
		Assert.assertEquals(34, vehiculoEJB.buscarVehiculo(3L).getNumero_busquedas());
		Assert.assertEquals(8, vehiculoEJB.buscarVehiculo(4L).getNumero_busquedas());
	}

	/**
	 * Filtra los vehiculos activos por una caracteristica. Esperado: con la caracteristica 1
	 * (Aire Acondicionado) quedan los vehiculos 1 y 3 (caracteristica_vehiculo.json); y no suma busquedas.
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void filtrarPorUnaCaracteristicaTest() throws Exception {
		List<Vehiculo> activos = vehiculoEJB.listarVehiculosActivos();
		List<Vehiculo> r = vehiculoEJB.filtrarPorCaracteristicas(activos, Arrays.asList(1L));
		Assert.assertEquals(new HashSet<Long>(Arrays.asList(1L, 3L)), codigos(r));
		Assert.assertEquals(57, vehiculoEJB.buscarVehiculo(1L).getNumero_busquedas());
	}

	/**
	 * Filtra por dos caracteristicas (1 y 2). Esperado: en los JSON ningun vehiculo activo tiene
	 * ambas, asi que queda vacia; tras publicar un vehiculo con las dos, queda solo ese.
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void filtrarPorDosCaracteristicasTest() throws Exception {
		List<Long> ambas = Arrays.asList(1L, 2L);
		Assert.assertTrue(vehiculoEJB.filtrarPorCaracteristicas(vehiculoEJB.listarVehiculosActivos(), ambas).isEmpty());
		Long nuevo = publicar(vehiculoNuevo("CAR123")).getCodigo_vehiculo();
		List<Vehiculo> r = vehiculoEJB.filtrarPorCaracteristicas(vehiculoEJB.listarVehiculosActivos(), ambas);
		Assert.assertEquals(1, r.size());
		Assert.assertEquals(nuevo, r.get(0).getCodigo_vehiculo());
	}

	/**
	 * Filtra con lista de caracteristicas vacia o nula. Esperado: devuelve los mismos vehiculos.
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void filtrarPorCaracteristicasVaciasTest() throws Exception {
		List<Vehiculo> activos = vehiculoEJB.listarVehiculosActivos();
		List<Vehiculo> r = vehiculoEJB.filtrarPorCaracteristicas(activos, new ArrayList<Long>());
		Assert.assertEquals(codigosEnOrden(activos), codigosEnOrden(r));
		List<Vehiculo> rNula = vehiculoEJB.filtrarPorCaracteristicas(activos, null);
		Assert.assertEquals(codigosEnOrden(activos), codigosEnOrden(rNula));
	}

	/**
	 * Top de los mas buscados NUEVOS. Esperado, solo activos y por busquedas descendentes:
	 * vehiculo 5 (95) y luego vehiculo 1 (57).
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void top10NuevosTest() throws Exception {
		List<Vehiculo> r = vehiculoEJB.top10MasBuscados(Condicion.NUEVO);
		Assert.assertEquals(Arrays.asList(5L, 1L), codigosEnOrden(r));
	}

	/**
	 * Top de los mas buscados USADOS. Esperado: solo los activos 3 (33 busquedas) y 4 (8); los
	 * usados inactivos 2 (120), 6 y 7 (70) no cuentan.
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void top10UsadosTest() throws Exception {
		List<Vehiculo> r = vehiculoEJB.top10MasBuscados(Condicion.USADO);
		Assert.assertEquals(Arrays.asList(3L, 4L), codigosEnOrden(r));
	}

	/**
	 * Top con mas de 10 candidatos. Se publican 11 vehiculos USADOS (0 busquedas cada uno), con lo
	 * que hay 13 usados activos. Esperado: a lo sumo 10 resultados, encabezados por el 3 y el 4, en
	 * orden descendente de busquedas.
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void top10LimiteTest() throws Exception {
		for (int i = 0; i < 11; i++) {
			publicar(vehiculoNuevo("TOP10" + i));
		}
		List<Vehiculo> r = vehiculoEJB.top10MasBuscados(Condicion.USADO);
		Assert.assertEquals(10, r.size());
		Assert.assertEquals(3L, r.get(0).getCodigo_vehiculo().longValue());
		Assert.assertEquals(4L, r.get(1).getCodigo_vehiculo().longValue());
		for (int i = 1; i < r.size(); i++) {
			Assert.assertTrue(r.get(i - 1).getNumero_busquedas() >= r.get(i).getNumero_busquedas());
			Assert.assertTrue(r.get(i).isActivo());
		}
	}

	/**
	 * Top con condicion nula. Esperado: InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void top10CondicionNulaTest() throws Exception {
		vehiculoEJB.top10MasBuscados(null);
	}

	// ------------------------------------------------------------------
	// PreguntaEJB
	// ------------------------------------------------------------------

	/**
	 * El cliente 4 (usuario1) pregunta por el vehiculo 3 (dueno usuario2). Esperado: la pregunta
	 * aparece en las del vehiculo (que tenia 1, ahora 2), en las recibidas por el dueno (tenia 1,
	 * ahora 2) y en las hechas por el cliente; no tiene respuesta. El correo es simulado y no falla.
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void hacerPreguntaTest() throws Exception {
		String texto = "Acepta cambio por una moto?";
		Assert.assertEquals(1, preguntaEJB.listarPreguntasVehiculo(3L).size());
		Assert.assertEquals(1, preguntaEJB.listarPreguntasRecibidas(EMAIL2).size());
		Pregunta p = preguntaEJB.hacerPregunta(EMAIL1, 3L, texto);
		Assert.assertEquals(texto, p.getDescripcion());
		Assert.assertNotNull(p.getFecha());
		Assert.assertNull(p.getRespuesta());
		Assert.assertEquals(EMAIL1, p.getCliente().getEmail());
		Assert.assertEquals(Long.valueOf(3), p.getVehiculo().getCodigo_vehiculo());
		List<Pregunta> delVehiculo = preguntaEJB.listarPreguntasVehiculo(3L);
		Assert.assertEquals(2, delVehiculo.size());
		Assert.assertTrue(contienePregunta(delVehiculo, texto));
		List<Pregunta> recibidas = preguntaEJB.listarPreguntasRecibidas(EMAIL2);
		Assert.assertEquals(2, recibidas.size());
		Assert.assertTrue(contienePregunta(recibidas, texto));
		Assert.assertTrue(contienePregunta(preguntaEJB.listarPreguntasHechas(EMAIL1), texto));
		Assert.assertTrue(contienePregunta(preguntaEJB.listarPreguntasSinResponder(EMAIL2), texto));
	}

	/**
	 * El cliente 4 pregunta sobre su propio vehiculo 1. Esperado: InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void hacerPreguntaVehiculoPropioTest() throws Exception {
		preguntaEJB.hacerPregunta(EMAIL1, 1L, "Pregunta a mi propio vehiculo");
	}

	/**
	 * El cliente 4 pregunta sobre el vehiculo 2, que esta inactivo en el JSON. Esperado:
	 * InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void hacerPreguntaVehiculoInactivoTest() throws Exception {
		preguntaEJB.hacerPregunta(EMAIL1, 2L, "Aun esta disponible?");
	}

	/**
	 * Pregunta con texto vacio (solo espacios). Esperado: InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void hacerPreguntaTextoVacioTest() throws Exception {
		preguntaEJB.hacerPregunta(EMAIL1, 3L, "   ");
	}

	/**
	 * Pregunta sobre un vehiculo inexistente. Esperado: ElementoNoEncontradoException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = ElementoNoEncontradoException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void hacerPreguntaVehiculoInexistenteTest() throws Exception {
		preguntaEJB.hacerPregunta(EMAIL1, 9999L, "Hay alguien?");
	}

	/**
	 * Pregunta de un cliente inexistente. Esperado: ElementoNoEncontradoException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = ElementoNoEncontradoException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void hacerPreguntaClienteInexistenteTest() throws Exception {
		preguntaEJB.hacerPregunta("nadie@servidor.com", 3L, "Quien soy?");
	}

	/**
	 * El dueno (usuario1) responde la pregunta 2 del vehiculo 1, que no tenia respuesta. Esperado:
	 * queda con la respuesta y fecha de respuesta, y deja de salir en las preguntas sin responder
	 * (que tenia 1 y queda en 0).
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void responderPreguntaTest() throws Exception {
		Assert.assertEquals(conjunto(2L), codigosPreguntas(preguntaEJB.listarPreguntasSinResponder(EMAIL1)));
		Pregunta p = preguntaEJB.responderPregunta(2L, "Si, todos los papeles al dia.", EMAIL1);
		Assert.assertEquals("Si, todos los papeles al dia.", p.getRespuesta());
		Assert.assertNotNull(p.getFecha_respuesta());
		Assert.assertTrue(preguntaEJB.listarPreguntasSinResponder(EMAIL1).isEmpty());
		Assert.assertEquals(1, preguntaEJB.listarPreguntasRecibidas(EMAIL1).size());
	}

	/**
	 * Un cliente que no es el dueno (usuario2) responde la pregunta 2 del vehiculo 1. Esperado:
	 * InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void responderPreguntaNoDuenoTest() throws Exception {
		preguntaEJB.responderPregunta(2L, "Respuesta ajena", EMAIL2);
	}

	/**
	 * El dueno (usuario3) responde la pregunta 1 del vehiculo 2, que ya tiene respuesta en el
	 * JSON. Esperado: InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void responderPreguntaYaRespondidaTest() throws Exception {
		preguntaEJB.responderPregunta(1L, "Otra respuesta", EMAIL3);
	}

	/**
	 * Responde con texto vacio. Esperado: InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void responderPreguntaRespuestaVaciaTest() throws Exception {
		preguntaEJB.responderPregunta(2L, "  ", EMAIL1);
	}

	/**
	 * Responde una pregunta inexistente. Esperado: ElementoNoEncontradoException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = ElementoNoEncontradoException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void responderPreguntaInexistenteTest() throws Exception {
		preguntaEJB.responderPregunta(9999L, "Respuesta", EMAIL1);
	}

	/**
	 * Lista las preguntas hechas, recibidas y por vehiculo con los datos del JSON. Esperado:
	 * hechas por usuario2 son la 1 y la 2; por usuario1 la 3 y la 4; por usuario3 ninguna.
	 * Recibidas: usuario3 las 1 y 3 (vehiculo 2), usuario1 la 2 y usuario2 la 4. Sin responder
	 * de usuario3: la 3. Del vehiculo 2: la 1 y la 3.
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void listarPreguntasTest() {
		Assert.assertEquals(new HashSet<Long>(Arrays.asList(1L, 2L)),
				codigosPreguntas(preguntaEJB.listarPreguntasHechas(EMAIL2)));
		Assert.assertEquals(new HashSet<Long>(Arrays.asList(3L, 4L)),
				codigosPreguntas(preguntaEJB.listarPreguntasHechas(EMAIL1)));
		Assert.assertTrue(preguntaEJB.listarPreguntasHechas(EMAIL3).isEmpty());
		Assert.assertEquals(new HashSet<Long>(Arrays.asList(1L, 3L)),
				codigosPreguntas(preguntaEJB.listarPreguntasRecibidas(EMAIL3)));
		Assert.assertEquals(conjunto(2L), codigosPreguntas(preguntaEJB.listarPreguntasRecibidas(EMAIL1)));
		Assert.assertEquals(conjunto(4L), codigosPreguntas(preguntaEJB.listarPreguntasRecibidas(EMAIL2)));
		Assert.assertEquals(conjunto(3L), codigosPreguntas(preguntaEJB.listarPreguntasSinResponder(EMAIL3)));
		Assert.assertEquals(new HashSet<Long>(Arrays.asList(1L, 3L)),
				codigosPreguntas(preguntaEJB.listarPreguntasVehiculo(2L)));
	}

	/**
	 * Conjunto de un solo codigo, para comparar.
	 *
	 * @param codigo unico elemento
	 * @return conjunto con ese codigo
	 */
	private Set<Long> conjunto(Long codigo) {
		return new HashSet<Long>(Arrays.asList(codigo));
	}

	// ------------------------------------------------------------------
	// CatalogoEJB
	// ------------------------------------------------------------------

	/**
	 * Lista las marcas. Esperado, en orden alfabetico: Audi, Mazda y Renoult (modelo.json).
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void listarMarcasTest() {
		Assert.assertEquals(Arrays.asList("Audi", "Mazda", "Renoult"), catalogoEJB.listarMarcas());
	}

	/**
	 * Lista los modelos. Esperado: 3 modelos, ordenados por marca y nombre, el primero Audi A1.
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void listarModelosTest() {
		List<Modelo> modelos = catalogoEJB.listarModelos();
		Assert.assertEquals(3, modelos.size());
		Assert.assertEquals("Audi", modelos.get(0).getMarca());
		Assert.assertEquals("A1", modelos.get(0).getNombre());
	}

	/**
	 * Lista los modelos por marca. Esperado: Mazda tiene un modelo (el 2, llamado 3), Audi el
	 * modelo 1 (A1) y una marca desconocida ninguno.
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void listarModelosPorMarcaTest() {
		List<Modelo> mazda = catalogoEJB.listarModelosPorMarca("Mazda");
		Assert.assertEquals(1, mazda.size());
		Assert.assertEquals(Long.valueOf(2), mazda.get(0).getCodigo_modelo());
		Assert.assertEquals("3", mazda.get(0).getNombre());
		List<Modelo> audi = catalogoEJB.listarModelosPorMarca("Audi");
		Assert.assertEquals(1, audi.size());
		Assert.assertEquals("A1", audi.get(0).getNombre());
		Assert.assertTrue(catalogoEJB.listarModelosPorMarca("Inexistente").isEmpty());
	}

	/**
	 * Lista las caracteristicas. Esperado: 3, ordenadas por nombre: Aire Acondicionado, Asientos
	 * de Cuero y Luces Led.
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void listarCaracteristicasTest() {
		List<Caracteristica> lista = catalogoEJB.listarCaracteristicas();
		Assert.assertEquals(3, lista.size());
		Assert.assertEquals("Aire Acondicionado", lista.get(0).getNombre());
		Assert.assertEquals("Asientos de Cuero", lista.get(1).getNombre());
		Assert.assertEquals("Luces Led", lista.get(2).getNombre());
	}

	/**
	 * Lista las ciudades. Esperado: 3 (Armenia, Buenavista y Pijao de ciudad.json).
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void listarCiudadesTest() {
		List<Ciudad> ciudades = catalogoEJB.listarCiudades();
		Assert.assertEquals(3, ciudades.size());
		Set<String> nombres = new HashSet<>();
		for (Ciudad c : ciudades) {
			nombres.add(c.getNombre());
		}
		Assert.assertEquals(new HashSet<String>(Arrays.asList("Armenia", "Buenavista", "Pijao")), nombres);
	}

	/**
	 * Lista los tipos de vehiculo, combustible, transmision y condicion. Esperado: la misma cantidad
	 * que los valores de cada enumeracion, y la condicion contiene NUEVO y USADO.
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void listarEnumeracionesTest() {
		Assert.assertEquals(TipoVehiculo.values().length, catalogoEJB.listarTiposVehiculo().size());
		Assert.assertTrue(catalogoEJB.listarTiposVehiculo().contains(TipoVehiculo.CARRO));
		Assert.assertEquals(TipoCombustible.values().length, catalogoEJB.listarCombustibles().size());
		Assert.assertEquals(TipoTrasmision.values().length, catalogoEJB.listarTransmisiones().size());
		Assert.assertEquals(Condicion.values().length, catalogoEJB.listarCondiciones().size());
		Assert.assertTrue(catalogoEJB.listarCondiciones().contains(Condicion.NUEVO));
		Assert.assertTrue(catalogoEJB.listarCondiciones().contains(Condicion.USADO));
	}
}
