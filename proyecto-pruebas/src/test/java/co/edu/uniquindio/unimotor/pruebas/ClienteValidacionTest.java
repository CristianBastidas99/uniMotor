package co.edu.uniquindio.unimotor.pruebas;

import java.util.HashMap;
import java.util.Map;

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
import co.edu.uniquindio.unimotor.entidades.Ciudad;
import co.edu.uniquindio.unimotor.entidades.Cliente;
import co.edu.uniquindio.unimotor.entidades.Usuario;
import co.edu.uniquindio.unimotor.excepciones.AutenticacionException;
import co.edu.uniquindio.unimotor.excepciones.ElementoNoEncontradoException;
import co.edu.uniquindio.unimotor.excepciones.ElementoRepetidoException;
import co.edu.uniquindio.unimotor.excepciones.InformacionInvalidaException;

/**
 * Pruebas in-container de la Fase 2.7 sobre {@link ClienteEJB}: caminos de validacion y de error
 * que las pruebas de las fases anteriores no cubren (datos nulos o vacios, ciudades invalidas,
 * limites de la contrasena, favoritos con clientes inexistentes y registro de usuarios base).
 * <p>
 * Cada prueba corre en una transaccion con rollback sobre los 14 datasets JSON de
 * src/test/resources. No se despliega {@code SetupEJB}.
 * <p>
 * Datos de los JSON que se usan: clientes 4 (usuario1@servidor.com), 5 (usuario2@servidor.com) y
 * 6 (usuario3@servidor.com) con contrasena 12345; administrador 1 (cristia@proyecto.com);
 * ciudades 1 (Armenia), 2 (Buenavista) y 3 (Pijao); vehiculo 3 (dueno 5) y vehiculo 1 (dueno 4,
 * favorito de los clientes 5 y 6). En total hay 6 usuarios.
 */
@RunWith(Arquillian.class)
@UsingDataSet({ Datasets.MODELO, Datasets.ADMINISTRADOR, Datasets.CARACTERISTICA, Datasets.CARACTERISTICA_VEHICULO,
		Datasets.CIUDAD, Datasets.CLIENTE, Datasets.FAVORITO, Datasets.FOTO_VEHICULO, Datasets.PREGUNTA,
		Datasets.TELEFONO_USUARIO, Datasets.USUARIO, Datasets.VEHICULO, Datasets.VENTA, Datasets.CALIFICACION })
public class ClienteValidacionTest {

	private static final String EMAIL1 = "usuario1@servidor.com";
	private static final String EMAIL2 = "usuario2@servidor.com";
	private static final String EMAIL_INEXISTENTE = "nadie@servidor.com";
	private static final String PASSWORD_JSON = "12345";
	private static final long TOTAL_USUARIOS = 6L;

	@EJB
	private ClienteEJB clienteEJB;

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
	 * Construye un cliente valido sin persistir, en la ciudad 1 (Armenia).
	 *
	 * @param email correo del cliente
	 * @return cliente nuevo
	 */
	private Cliente clienteNuevo(String email) {
		Cliente c = new Cliente();
		c.setEmail(email);
		c.setPassword("clave123");
		c.setNombre_completo("Cliente de Validacion");
		c.setDireccion("Calle 1 # 2-3");
		Map<String, String> telefonos = new HashMap<>();
		telefonos.put("celular", "3001234567");
		c.setTelefono(telefonos);
		c.setCodigo_ciudad(entityManager.find(Ciudad.class, 1L));
		return c;
	}

	/**
	 * Construye los datos de cambio validos para el cliente 4 (copia desprendida con los mismos
	 * datos del JSON, ciudad 1).
	 *
	 * @return cliente con el codigo 4
	 */
	private Cliente cambiosCliente4() {
		Cliente cambios = new Cliente();
		cambios.setCodigo_usuario(4L);
		cambios.setEmail(EMAIL1);
		cambios.setPassword(PASSWORD_JSON);
		cambios.setNombre_completo("Hector Rodriguez");
		cambios.setDireccion("B/ la clarita Mz F #45");
		cambios.setCodigo_ciudad(entityManager.find(Ciudad.class, 1L));
		return cambios;
	}

	/**
	 * Cuenta los usuarios (clientes y administradores) de la base.
	 *
	 * @return numero de usuarios
	 */
	private long contarUsuarios() {
		entityManager.flush();
		return entityManager.createQuery("select count(u) from Usuario u", Long.class).getSingleResult();
	}

	/**
	 * Crea una ciudad solo con el codigo dado, sin consultarla en la base.
	 *
	 * @param codigo codigo de la ciudad
	 * @return ciudad desprendida
	 */
	private Ciudad ciudadConCodigo(Long codigo) {
		Ciudad c = new Ciudad();
		c.setCodigo_ciudad(codigo);
		return c;
	}

	// ------------------------------------------------------------------
	// registrarCliente
	// ------------------------------------------------------------------

	/**
	 * Registrar un cliente nulo. Esperado: InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void registrarClienteNuloTest() throws Exception {
		clienteEJB.registrarCliente(null);
	}

	/**
	 * Registrar un cliente con el email nulo. Esperado: InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void registrarClienteEmailNuloTest() throws Exception {
		clienteEJB.registrarCliente(clienteNuevo(null));
	}

	/**
	 * Registrar un cliente con el email vacio (solo espacios). Esperado:
	 * InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void registrarClienteEmailVacioTest() throws Exception {
		clienteEJB.registrarCliente(clienteNuevo("   "));
	}

	/**
	 * Registrar un cliente con la contrasena nula. Esperado: InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void registrarClientePasswordNulaTest() throws Exception {
		Cliente c = clienteNuevo("passnula@servidor.com");
		c.setPassword(null);
		clienteEJB.registrarCliente(c);
	}

	/**
	 * Registrar un cliente con la contrasena vacia. Esperado: InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void registrarClientePasswordVaciaTest() throws Exception {
		Cliente c = clienteNuevo("passvacia@servidor.com");
		c.setPassword("");
		clienteEJB.registrarCliente(c);
	}

	/**
	 * Registrar un cliente con el nombre completo nulo. Esperado: InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void registrarClienteNombreNuloTest() throws Exception {
		Cliente c = clienteNuevo("nombrenulo@servidor.com");
		c.setNombre_completo(null);
		clienteEJB.registrarCliente(c);
	}

	/**
	 * Registrar un cliente con el nombre completo vacio (solo espacios). Esperado:
	 * InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void registrarClienteNombreVacioTest() throws Exception {
		Cliente c = clienteNuevo("nombrevacio@servidor.com");
		c.setNombre_completo("   ");
		clienteEJB.registrarCliente(c);
	}

	/**
	 * Registrar un cliente sin ciudad. Esperado: InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void registrarClienteCiudadNulaTest() throws Exception {
		Cliente c = clienteNuevo("ciudadnula@servidor.com");
		c.setCodigo_ciudad(null);
		clienteEJB.registrarCliente(c);
	}

	/**
	 * Registrar un cliente con una ciudad sin codigo. Esperado: InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void registrarClienteCiudadSinCodigoTest() throws Exception {
		Cliente c = clienteNuevo("ciudadsincodigo@servidor.com");
		c.setCodigo_ciudad(ciudadConCodigo(null));
		clienteEJB.registrarCliente(c);
	}

	/**
	 * Registrar un cliente con una ciudad cuyo codigo (999) no existe. Esperado:
	 * InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void registrarClienteCiudadInexistenteTest() throws Exception {
		Cliente c = clienteNuevo("ciudadfalsa@servidor.com");
		c.setCodigo_ciudad(ciudadConCodigo(999L));
		clienteEJB.registrarCliente(c);
	}

	/**
	 * Cada registro invalido lanza InformacionInvalidaException y no inserta nada: despues de
	 * cada intento fallido el numero de usuarios sigue siendo 6 y el email no existe.
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void registrarClienteInvalidoNoInsertaNadaTest() throws Exception {
		Assert.assertEquals(TOTAL_USUARIOS, contarUsuarios());

		Cliente sinPassword = clienteNuevo("noinserta1@servidor.com");
		sinPassword.setPassword("");
		Cliente sinNombre = clienteNuevo("noinserta2@servidor.com");
		sinNombre.setNombre_completo(" ");
		Cliente sinCiudad = clienteNuevo("noinserta3@servidor.com");
		sinCiudad.setCodigo_ciudad(null);
		Cliente ciudadFalsa = clienteNuevo("noinserta4@servidor.com");
		ciudadFalsa.setCodigo_ciudad(ciudadConCodigo(999L));
		Cliente emailMalo = clienteNuevo("esto no es un correo");
		Cliente[] invalidos = { sinPassword, sinNombre, sinCiudad, ciudadFalsa, emailMalo, clienteNuevo(null),
				clienteNuevo("") };

		for (int i = 0; i < invalidos.length; i++) {
			try {
				clienteEJB.registrarCliente(invalidos[i]);
				Assert.fail("El cliente invalido numero " + i + " no deberia registrarse");
			} catch (InformacionInvalidaException e) {
				// esperado
			}
			Assert.assertEquals("Tras el caso " + i, TOTAL_USUARIOS, contarUsuarios());
		}
		Assert.assertFalse(clienteEJB.existeEmail("noinserta1@servidor.com"));
		Assert.assertFalse(clienteEJB.existeEmail("noinserta2@servidor.com"));
		Assert.assertFalse(clienteEJB.existeEmail("noinserta3@servidor.com"));
		Assert.assertFalse(clienteEJB.existeEmail("noinserta4@servidor.com"));
	}

	/**
	 * El email se recorta antes de guardar: registrar con espacios alrededor deja el email sin
	 * espacios y se puede buscar con el email limpio.
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void registrarClienteEmailConEspaciosTest() throws Exception {
		Cliente r = clienteEJB.registrarCliente(clienteNuevo("  espacios@servidor.com  "));
		entityManager.flush();
		Assert.assertEquals("espacios@servidor.com", r.getEmail());
		Assert.assertEquals(r.getCodigo_usuario(), clienteEJB.buscarCliente("espacios@servidor.com").getCodigo_usuario());
		Assert.assertEquals(TOTAL_USUARIOS + 1, contarUsuarios());
	}

	// ------------------------------------------------------------------
	// modificarCliente
	// ------------------------------------------------------------------

	/**
	 * Modificar con el nombre completo vacio. Esperado: InformacionInvalidaException y el nombre
	 * del cliente 4 no cambia.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void modificarClienteNombreVacioTest() throws Exception {
		Cliente cambios = cambiosCliente4();
		cambios.setNombre_completo("   ");
		try {
			clienteEJB.modificarCliente(cambios);
			Assert.fail("Un nombre vacio no es valido");
		} catch (InformacionInvalidaException e) {
			// esperado
		}
		Assert.assertEquals("Hector Rodriguez", clienteEJB.buscarCliente(EMAIL1).getNombre_completo());
	}

	/**
	 * Modificar con el nombre completo nulo. Esperado: InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void modificarClienteNombreNuloTest() throws Exception {
		Cliente cambios = cambiosCliente4();
		cambios.setNombre_completo(null);
		clienteEJB.modificarCliente(cambios);
	}

	/**
	 * Modificar con un email nuevo sin formato valido. Esperado: InformacionInvalidaException y el
	 * email del cliente 4 sigue siendo el del JSON.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void modificarClienteEmailInvalidoTest() throws Exception {
		Cliente cambios = cambiosCliente4();
		cambios.setEmail("esto no es un correo");
		try {
			clienteEJB.modificarCliente(cambios);
			Assert.fail("Un email sin formato valido no se acepta");
		} catch (InformacionInvalidaException e) {
			// esperado
		}
		Assert.assertEquals(Long.valueOf(4), clienteEJB.buscarCliente(EMAIL1).getCodigo_usuario());
	}

	/**
	 * Modificar con una ciudad cuyo codigo (999) no existe. Esperado: InformacionInvalidaException y
	 * el cliente 4 sigue en la ciudad 1.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void modificarClienteCiudadInexistenteTest() throws Exception {
		Cliente cambios = cambiosCliente4();
		cambios.setCodigo_ciudad(ciudadConCodigo(999L));
		try {
			clienteEJB.modificarCliente(cambios);
			Assert.fail("Una ciudad inexistente no es valida");
		} catch (InformacionInvalidaException e) {
			// esperado
		}
		Assert.assertEquals(Long.valueOf(1), clienteEJB.buscarCliente(EMAIL1).getCodigo_ciudad().getCodigo_ciudad());
	}

	/**
	 * Modificar sin ciudad. Esperado: InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void modificarClienteCiudadNulaTest() throws Exception {
		Cliente cambios = cambiosCliente4();
		cambios.setCodigo_ciudad(null);
		clienteEJB.modificarCliente(cambios);
	}

	/**
	 * Modificar con el cliente nulo. Esperado: ElementoNoEncontradoException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = ElementoNoEncontradoException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void modificarClienteNuloTest() throws Exception {
		clienteEJB.modificarCliente(null);
	}

	/**
	 * Modificar un cliente sin codigo de usuario. Esperado: ElementoNoEncontradoException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = ElementoNoEncontradoException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void modificarClienteSinCodigoTest() throws Exception {
		Cliente cambios = cambiosCliente4();
		cambios.setCodigo_usuario(null);
		clienteEJB.modificarCliente(cambios);
	}

	/**
	 * Modificar con el email nulo o vacio: se interpreta como "no cambiar el email". Esperado: el
	 * cliente 4 conserva usuario1@servidor.com y el resto de datos si cambian.
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void modificarClienteEmailVacioConservaEmailTest() throws Exception {
		Cliente cambios = cambiosCliente4();
		cambios.setEmail("  ");
		cambios.setNombre_completo("Hector Cambiado");
		Cliente r = clienteEJB.modificarCliente(cambios);
		Assert.assertEquals(EMAIL1, r.getEmail());
		Assert.assertEquals("Hector Cambiado", r.getNombre_completo());

		Cliente otros = cambiosCliente4();
		otros.setEmail(null);
		Assert.assertEquals(EMAIL1, clienteEJB.modificarCliente(otros).getEmail());
	}

	// ------------------------------------------------------------------
	// cambiarPassword
	// ------------------------------------------------------------------

	/**
	 * Cambiar la contrasena de un email que no existe. Esperado: AutenticacionException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = AutenticacionException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void cambiarPasswordEmailInexistenteTest() throws Exception {
		clienteEJB.cambiarPassword(EMAIL_INEXISTENTE, PASSWORD_JSON, "nueva9876");
	}

	/**
	 * Cambiar la contrasena con el email nulo. Esperado: AutenticacionException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = AutenticacionException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void cambiarPasswordEmailNuloTest() throws Exception {
		clienteEJB.cambiarPassword(null, PASSWORD_JSON, "nueva9876");
	}

	/**
	 * Cambiar la contrasena con la actual nula. Esperado: AutenticacionException y la contrasena
	 * no cambia.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void cambiarPasswordActualNulaTest() throws Exception {
		try {
			clienteEJB.cambiarPassword(EMAIL1, null, "nueva9876");
			Assert.fail("La contrasena actual nula no es valida");
		} catch (AutenticacionException e) {
			// esperado
		}
		Assert.assertEquals(PASSWORD_JSON, clienteEJB.buscarCliente(EMAIL1).getPassword());
	}

	/**
	 * Cambiar la contrasena con la nueva nula. Esperado: InformacionInvalidaException y la
	 * contrasena no cambia.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void cambiarPasswordNuevaNulaTest() throws Exception {
		try {
			clienteEJB.cambiarPassword(EMAIL1, PASSWORD_JSON, null);
			Assert.fail("La contrasena nueva nula no es valida");
		} catch (InformacionInvalidaException e) {
			// esperado
		}
		Assert.assertEquals(PASSWORD_JSON, clienteEJB.buscarCliente(EMAIL1).getPassword());
	}

	/**
	 * Cambiar la contrasena con la nueva vacia. Esperado: InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void cambiarPasswordNuevaVaciaTest() throws Exception {
		clienteEJB.cambiarPassword(EMAIL1, PASSWORD_JSON, "");
	}

	/**
	 * Limite inferior: una contrasena nueva de 3 caracteres se rechaza.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void cambiarPasswordNuevaDeTresCaracteresTest() throws Exception {
		clienteEJB.cambiarPassword(EMAIL1, PASSWORD_JSON, "abc");
	}

	/**
	 * Limite inferior: una contrasena nueva de exactamente 4 caracteres es valida. Esperado: el
	 * cliente se autentica con la nueva y ya no con la anterior.
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void cambiarPasswordNuevaDeCuatroCaracteresTest() throws Exception {
		clienteEJB.cambiarPassword(EMAIL1, PASSWORD_JSON, "abcd");
		Assert.assertEquals(Long.valueOf(4), clienteEJB.autenticarCliente(EMAIL1, "abcd").getCodigo_usuario());
		try {
			clienteEJB.autenticarCliente(EMAIL1, PASSWORD_JSON);
			Assert.fail("La contrasena anterior ya no deberia servir");
		} catch (AutenticacionException e) {
			// esperado
		}
	}

	// ------------------------------------------------------------------
	// Favoritos
	// ------------------------------------------------------------------

	/**
	 * Agregar un favorito con un cliente que no existe. Esperado: ElementoNoEncontradoException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = ElementoNoEncontradoException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void agregarFavoritoClienteInexistenteTest() throws Exception {
		clienteEJB.agregarFavorito(EMAIL_INEXISTENTE, 3L);
	}

	/**
	 * Agregar un favorito con el codigo de vehiculo nulo. Esperado: ElementoNoEncontradoException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = ElementoNoEncontradoException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void agregarFavoritoVehiculoNuloTest() throws Exception {
		clienteEJB.agregarFavorito(EMAIL1, null);
	}

	/**
	 * Quitar un favorito con un cliente que no existe. Esperado: ElementoNoEncontradoException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = ElementoNoEncontradoException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void quitarFavoritoClienteInexistenteTest() throws Exception {
		clienteEJB.quitarFavorito(EMAIL_INEXISTENTE, 1L);
	}

	/**
	 * Quitar un favorito de un vehiculo que no existe. Esperado: ElementoNoEncontradoException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = ElementoNoEncontradoException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void quitarFavoritoVehiculoInexistenteTest() throws Exception {
		clienteEJB.quitarFavorito(EMAIL2, 9999L);
	}

	/**
	 * esFavorito no lanza excepcion con datos nulos: email nulo, codigo nulo y ambos nulos dan
	 * falso (aunque el vehiculo 1 si es favorito del cliente 5).
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void esFavoritoConNulosTest() throws Exception {
		Assert.assertTrue(clienteEJB.esFavorito(EMAIL2, 1L));
		Assert.assertFalse(clienteEJB.esFavorito(null, 1L));
		Assert.assertFalse(clienteEJB.esFavorito(EMAIL2, null));
		Assert.assertFalse(clienteEJB.esFavorito(null, null));
	}

	// ------------------------------------------------------------------
	// autenticarCliente
	// ------------------------------------------------------------------

	/**
	 * Autenticar con el email nulo. Esperado: AutenticacionException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = AutenticacionException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void autenticarClienteEmailNuloTest() throws Exception {
		clienteEJB.autenticarCliente(null, PASSWORD_JSON);
	}

	/**
	 * Autenticar con la contrasena nula. Esperado: AutenticacionException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = AutenticacionException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void autenticarClientePasswordNulaTest() throws Exception {
		clienteEJB.autenticarCliente(EMAIL1, null);
	}

	/**
	 * Autenticar con un email que no existe. Esperado: AutenticacionException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = AutenticacionException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void autenticarClienteEmailInexistenteTest() throws Exception {
		clienteEJB.autenticarCliente(EMAIL_INEXISTENTE, PASSWORD_JSON);
	}

	/**
	 * El email se recorta al autenticar: con espacios alrededor igual se autentica al cliente 4.
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void autenticarClienteEmailConEspaciosTest() throws Exception {
		Cliente c = clienteEJB.autenticarCliente("  " + EMAIL1 + "  ", PASSWORD_JSON);
		Assert.assertEquals(Long.valueOf(4), c.getCodigo_usuario());
	}

	// ------------------------------------------------------------------
	// recuperarPassword
	// ------------------------------------------------------------------

	/**
	 * Recuperar la contrasena con el email nulo. Esperado: InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void recuperarPasswordNuloTest() throws Exception {
		clienteEJB.recuperarPassword(null);
	}

	/**
	 * Recuperar la contrasena con el email en blanco. Esperado: InformacionInvalidaException y la
	 * contrasena del cliente 4 no cambia.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void recuperarPasswordEnBlancoTest() throws Exception {
		try {
			clienteEJB.recuperarPassword("  ");
			Assert.fail("Un email en blanco no es valido");
		} catch (InformacionInvalidaException e) {
			// esperado
		}
		Assert.assertEquals(PASSWORD_JSON, clienteEJB.buscarCliente(EMAIL1).getPassword());
	}

	// ------------------------------------------------------------------
	// registrarUsuario (usuario base, la clase Usuario es concreta)
	// ------------------------------------------------------------------

	/**
	 * Registrar un usuario nulo. Esperado: InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void registrarUsuarioNuloTest() throws Exception {
		clienteEJB.registrarUsuario(null);
	}

	/**
	 * Registrar un usuario con el email nulo. Esperado: InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void registrarUsuarioEmailNuloTest() throws Exception {
		clienteEJB.registrarUsuario(new Usuario(null, "clave"));
	}

	/**
	 * Registrar un usuario con el email vacio. Esperado: InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void registrarUsuarioEmailVacioTest() throws Exception {
		clienteEJB.registrarUsuario(new Usuario("   ", "clave"));
	}

	/**
	 * Registrar un usuario con la contrasena vacia. Esperado: InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void registrarUsuarioPasswordVaciaTest() throws Exception {
		clienteEJB.registrarUsuario(new Usuario("vacia@servidor.com", ""));
	}

	/**
	 * Registrar un usuario con la contrasena nula. Esperado: InformacionInvalidaException.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test(expected = InformacionInvalidaException.class)
	@Transactional(value = TransactionMode.ROLLBACK)
	public void registrarUsuarioPasswordNulaTest() throws Exception {
		clienteEJB.registrarUsuario(new Usuario("nula@servidor.com", null));
	}

	/**
	 * Registrar un usuario cuyo codigo (4) ya existe, aunque su email sea nuevo. Esperado:
	 * ElementoRepetidoException y no se inserta nada.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void registrarUsuarioCodigoExistenteTest() throws Exception {
		Usuario u = new Usuario("codigorepetido@servidor.com", "clave");
		u.setCodigo_usuario(4L);
		try {
			clienteEJB.registrarUsuario(u);
			Assert.fail("El codigo de usuario ya existe");
		} catch (ElementoRepetidoException e) {
			// esperado
		}
		Assert.assertEquals(TOTAL_USUARIOS, contarUsuarios());
		Assert.assertFalse(clienteEJB.existeEmail("codigorepetido@servidor.com"));
	}

	/**
	 * Registrar un usuario con el email de un usuario del JSON. Esperado: ElementoRepetidoException
	 * y no se inserta nada.
	 *
	 * @throws Exception si falla algo distinto
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void registrarUsuarioEmailRepetidoTest() throws Exception {
		try {
			clienteEJB.registrarUsuario(new Usuario(EMAIL1, "clave"));
			Assert.fail("El email ya esta en uso");
		} catch (ElementoRepetidoException e) {
			// esperado
		}
		Assert.assertEquals(TOTAL_USUARIOS, contarUsuarios());
	}

	/**
	 * Registrar un usuario base nuevo (la clase Usuario es concreta y se puede instanciar).
	 * Esperado: queda persistido con identificador, existeEmail pasa a verdadero y hay 7 usuarios.
	 * No es un Cliente, por lo que autenticarCliente lo rechaza.
	 *
	 * @throws Exception si falla el negocio
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void registrarUsuarioExitoTest() throws Exception {
		Assert.assertFalse(clienteEJB.existeEmail("base@servidor.com"));
		Usuario u = new Usuario("base@servidor.com", "clave");
		clienteEJB.registrarUsuario(u);
		Assert.assertEquals(TOTAL_USUARIOS + 1, contarUsuarios());
		Assert.assertNotNull(u.getCodigo_usuario());
		Assert.assertTrue(clienteEJB.existeEmail("base@servidor.com"));
		try {
			clienteEJB.autenticarCliente("base@servidor.com", "clave");
			Assert.fail("Un usuario base no es un cliente");
		} catch (AutenticacionException e) {
			// esperado
		}
	}

	// ------------------------------------------------------------------
	// existeEmail
	// ------------------------------------------------------------------

	/**
	 * existeEmail es verdadero para los usuarios del JSON (cliente y administrador) y falso para un
	 * email desconocido.
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void existeEmailTest() {
		Assert.assertTrue(clienteEJB.existeEmail(EMAIL1));
		Assert.assertTrue(clienteEJB.existeEmail("cristia@proyecto.com"));
		Assert.assertFalse(clienteEJB.existeEmail(EMAIL_INEXISTENTE));
	}

	/**
	 * existeEmail no distingue mayúsculas: el código no normaliza el email, pero la columna de MySQL usa
	 * la collation utf8mb4_unicode_ci (insensible a mayúsculas), así que el email en mayúsculas de un
	 * usuario existente cuenta como existente. Los espacios al inicio sí cuentan (quienes lo llaman,
	 * como registrarCliente y modificarCliente, recortan el email antes).
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void existeEmailIgnoraMayusculasTest() {
		Assert.assertTrue(clienteEJB.existeEmail(EMAIL1.toUpperCase()));
		Assert.assertFalse(clienteEJB.existeEmail(" " + EMAIL1));
	}

	/**
	 * No se puede registrar como cliente nuevo el email de un usuario existente escrito en mayúsculas:
	 * lanza ElementoRepetidoException y no inserta nada.
	 *
	 * @throws Exception si ocurre un error inesperado
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void registrarClienteEmailRepetidoEnMayusculasTest() throws Exception {
		long antes = entityManager.createQuery("SELECT COUNT(u) FROM Usuario u", Long.class).getSingleResult();
		try {
			clienteEJB.registrarCliente(clienteNuevo(EMAIL1.toUpperCase()));
			Assert.fail("Debió rechazar el email repetido en mayúsculas");
		} catch (ElementoRepetidoException e) {
			long despues = entityManager.createQuery("SELECT COUNT(u) FROM Usuario u", Long.class).getSingleResult();
			Assert.assertEquals(antes, despues);
		}
	}
}
