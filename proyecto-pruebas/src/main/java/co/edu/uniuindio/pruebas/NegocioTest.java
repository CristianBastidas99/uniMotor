package co.edu.uniuindio.pruebas;

import javax.ejb.EJB;

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

import co.edu.uniquindio.empresa.Usuario;
import co.edu.uniquindio.empresa.Vehiculo;
import co.edu.uniquinio.unimotor.ejb.PersonaEJB;
import co.edu.uniquinio.unimotor.excepciones.AutentificacionEcxeption;

@RunWith(Arquillian.class)
public class NegocioTest {

	@EJB
	private PersonaEJB personaEJB;

	@Deployment
	public static Archive<?> createTestArchive() {
		return ShrinkWrap.create(JavaArchive.class).addClass(PersonaEJB.class).addPackage(Vehiculo.class.getPackage())
				.addAsResource("persistenceForTest.xml", "META-INF/persistence.xml")
				.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

	}
	
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	@UsingDataSet({"modelo.json", 
		"administrador.json", 
		"caracteristica.json",
		"caracteristica_vehiculo.json",
		"ciudad.json",
		"cliente.json",
		"favorito.json",
		"foto_vehiculo.json",
		"pregunta.json",
		"telefono_usuario.json",
		"usuario.json",
		"vehiculo.json",
		"venta.json"})
	public void generarTest() {

	}
	
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	@UsingDataSet({"modelo.json", 
		"administrador.json", 
		"caracteristica.json",
		"caracteristica_vehiculo.json",
		"ciudad.json",
		"cliente.json",
		"favorito.json",
		"foto_vehiculo.json",
		"pregunta.json",
		"telefono_usuario.json",
		"usuario.json",
		"vehiculo.json",
		"venta.json"})
	public void autentificarUsuarioTest(String email, String password) throws Exception {
		// TODO Auto-generated method stub
		
		//System.out.print(personaEJB.autentificarAdministrador("cfbastidaso@uqvirtual.edu.co", "12345"));
		try {
			System.out.print(personaEJB.autentificarUsuario("cristia@proyecto.com", "123"));
		} catch (AutentificacionEcxeption e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

}
