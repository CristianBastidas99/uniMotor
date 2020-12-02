package co.edu.uniuindio.pruebas;

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
import org.junit.Test;
import org.junit.runner.RunWith;

import co.edu.uniquindio.empresa.Ciudad;
import co.edu.uniquindio.empresa.Cliente;
import co.edu.uniquindio.empresa.Usuario;
import co.edu.uniquindio.empresa.Vehiculo;
import co.edu.uniquinio.unimotor.ejb.UnimotorEJB;
import co.edu.uniquinio.unimotor.excepciones.AutentificacionEcxeption;

@RunWith(Arquillian.class)
public class NegocioTest {

	@EJB
	private UnimotorEJB unimotorEJB;

	@Deployment
	public static Archive<?> createTestArchive() {
		return ShrinkWrap.create(JavaArchive.class).addClass(UnimotorEJB.class).addPackage(Vehiculo.class.getPackage())
				.addAsResource("persistenceForTest.xml", "META-INF/persistence.xml")
				.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

	}

	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	@UsingDataSet({ "modelo.json", "administrador.json", "caracteristica.json", "caracteristica_vehiculo.json",
			"ciudad.json", "cliente.json", "favorito.json", "foto_vehiculo.json", "pregunta.json",
			"telefono_usuario.json", "usuario.json", "vehiculo.json", "venta.json" })
	public void generarTest() {

	}

	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	@UsingDataSet({ "modelo.json", "administrador.json", "caracteristica.json", "caracteristica_vehiculo.json",
			"ciudad.json", "cliente.json", "favorito.json", "foto_vehiculo.json", "pregunta.json",
			"telefono_usuario.json", "usuario.json", "vehiculo.json", "venta.json" })
	public void autentificarUsuarioTest(String email, String password) throws Exception {
		// TODO Auto-generated method stub

		try {
			System.out.print(unimotorEJB.autentificarUsuario("cristia@proyecto.coom", "123"));
		} catch (AutentificacionEcxeption e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
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
			unimotorEJB.registrarUsuario(cliente);
		} catch (AutentificacionEcxeption e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

}
