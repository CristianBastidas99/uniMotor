package co.edu.uniquindio.unimotor.pruebas;

import java.util.HashMap;
import java.util.Map;

import javax.ejb.EJB;

import org.apache.commons.collections.map.HashedMap;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import co.edu.uniquindio.unimotor.entidades.Ciudad;
import co.edu.uniquindio.unimotor.entidades.Cliente;
import co.edu.uniquindio.unimotor.entidades.Usuario;
import co.edu.uniquindio.unimotor.entidades.Vehiculo;
import co.edu.uniquindio.unimotor.ejb.AdministradorEJB;
import co.edu.uniquindio.unimotor.ejb.AdministradorEJBRemote;
import co.edu.uniquindio.unimotor.ejb.ClienteEJB;
import co.edu.uniquindio.unimotor.ejb.VehiculoEJB;
import co.edu.uniquindio.unimotor.excepciones.ElementoRepetidoException;
import co.edu.uniquindio.unimotor.excepciones.InformacionInvalidaException;
import co.edu.uniquindio.unimotor.excepciones.AutenticacionException;

/**
 * Pruebas del EJB de negocio {@link AdministradorEJB} y {@link ClienteEJB} (autenticacion y registro de usuarios).
 * <p>
 * Toda la clase esta marcada con {@code @Ignore} (por eso aparece como 1 prueba omitida):
 * queda pendiente para la Fase 2.7 y por ahora los metodos son esqueletos sin asserts.
 * Cuando se active, cada metodo carga los JSON de src/test/resources sobre la base {@code prueba}
 * y hace rollback al terminar.
 */
@Ignore("Fase 2.7: pruebas de negocio")
@RunWith(Arquillian.class)
public class NegocioTest {

	@EJB
	private ClienteEJB clienteEJB;

	@EJB
	private AdministradorEJBRemote administradorEJB;

	@Deployment
	public static Archive<?> createTestArchive() {
		return ShrinkWrap.create(JavaArchive.class).addClass(AdministradorEJB.class)
				.addClass(AdministradorEJBRemote.class).addClass(ClienteEJB.class).addClass(VehiculoEJB.class)
				.addPackage(AutenticacionException.class.getPackage()).addPackage(Vehiculo.class.getPackage())
				.addAsResource("persistenceForTest.xml", "META-INF/persistence.xml")
				.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

	}

	/**
	 * Esqueleto: solo carga los datasets; pendiente de implementar en la Fase 2.7.
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	@UsingDataSet({ "modelo.json", "administrador.json", "caracteristica.json", "caracteristica_vehiculo.json",
			"ciudad.json", "cliente.json", "favorito.json", "foto_vehiculo.json", "pregunta.json",
			"telefono_usuario.json", "usuario.json", "vehiculo.json", "venta.json" })
	public void generarTest() {

	}

	/**
	 * Esqueleto: intenta autenticar a un usuario del JSON con {@code AdministradorEJB.autenticarUsuario}; pendiente de completar (tiene parametros que JUnit no puede inyectar) en la Fase 2.7.
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	@UsingDataSet({ "modelo.json", "administrador.json", "caracteristica.json", "caracteristica_vehiculo.json",
			"ciudad.json", "cliente.json", "favorito.json", "foto_vehiculo.json", "pregunta.json",
			"telefono_usuario.json", "usuario.json", "vehiculo.json", "venta.json" })
	public void autenticarUsuarioTest(String email, String password) throws Exception {
		// TODO Auto-generated method stub

		try {
			System.out.print(administradorEJB.autenticarUsuario("cristia@proyecto.coom", "123"));
		} catch (AutenticacionException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	/**
	 * Esqueleto: registra un cliente nuevo con {@code ClienteEJB.registrarUsuario}; pendiente de agregar asserts en la Fase 2.7.
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	@UsingDataSet({ "modelo.json", "administrador.json", "caracteristica.json", "caracteristica_vehiculo.json",
			"ciudad.json", "cliente.json", "favorito.json", "foto_vehiculo.json", "pregunta.json",
			"telefono_usuario.json", "usuario.json", "vehiculo.json", "venta.json" })
	public void registrarUsuarioTest() throws Exception {
		// TODO Auto-generated method stub

		String email = "prueba@servidor.com";
		String password = "1234";
		String nombre_completo = "CLiente de Prueba";
		String direccion = "Cll 13 #34-23";
		Map<String, String> telefono = new HashMap<String, String>();
		telefono.put("Casa", "7444445");
		telefono.put("Trabajo", "7456789");
		
		Ciudad codigo_ciudad = new Ciudad("Quimbya");
		
		Cliente cliente = new Cliente(email, password, nombre_completo, direccion, telefono, codigo_ciudad);
		
		Usuario usuario = new Usuario(email, password);
		
		
		try {
			clienteEJB.registrarUsuario(cliente);
		} catch (ElementoRepetidoException | InformacionInvalidaException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

}
