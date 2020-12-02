package co.edu.uniquinio.unimotor.ejb;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import co.edu.uniquindio.empresa.Administrador;
import co.edu.uniquindio.empresa.Ciudad;
import co.edu.uniquindio.empresa.Cliente;
import co.edu.uniquindio.empresa.Condicion;
import co.edu.uniquindio.empresa.Modelo;
import co.edu.uniquindio.empresa.TipoCombustible;
import co.edu.uniquindio.empresa.TipoTrasmision;
import co.edu.uniquindio.empresa.TipoVehiculo;
import co.edu.uniquindio.empresa.Usuario;
import co.edu.uniquindio.empresa.Vehiculo;

/**
 * Session Bean implementation class SetupEJB
 */
@Singleton
@LocalBean
@Startup
public class SetupEJB {

	@PersistenceContext
	private EntityManager entityManager;

	/**
	 * Default constructor.
	 */

	public SetupEJB() {
		// TODO Auto-generated constructor stub
	}

	@PostConstruct
	public void config() {
		boolean bandera = isUsuario();

		if (!bandera) {

			Administrador administrador = new Administrador("cristia@proyecto.com", "123", "Cristian Bastidas");
			entityManager.persist(administrador);
			Administrador administrador2 = new Administrador("profesor@proyecto.com", "4444", "Profesor");
			entityManager.persist(administrador2);

			Ciudad ciudad = new Ciudad("Armenia");
			entityManager.persist(ciudad);
			Ciudad ciudad2 = new Ciudad("Calarca");
			entityManager.persist(ciudad2);
			Ciudad ciudad3 = new Ciudad("Pijao");
			entityManager.persist(ciudad3);

			Map<String, String> telefono = new HashMap<String, String>();
			telefono.put("Casa", "7444445");
			telefono.put("Trabajo", "7456789");
			Map<String, String> telefono2 = new HashMap<String, String>();
			telefono.put("Casa", "5643567");
			telefono.put("Trabajo", "3134526849");
			Map<String, String> telefono3 = new HashMap<String, String>();
			telefono.put("Casa", "3127456789");

			Cliente cliente = new Cliente("usuario1@servidor.com", "12345", "Hector Rodriguez",
					"B/ la clarita Mz F #45", telefono, ciudad);
			entityManager.persist(cliente);
			Cliente cliente2 = new Cliente("usuario2@servidor.com", "12345", "Rodolfo el Grande", "Cra 5 #23-45",
					telefono2, ciudad2);
			entityManager.persist(cliente2);
			Cliente cliente3 = new Cliente("usuario3@servidor.com", "12345", "Ruben zuluaga", "Calle 2 #10-12",
					telefono3, ciudad3);
			entityManager.persist(cliente3);

			Modelo modelo = new Modelo("A1", "Audi");
			entityManager.persist(modelo);
			Modelo modelo2 = new Modelo("Mazda 2", "Mazda");
			entityManager.persist(modelo2);
			Modelo modelo3 = new Modelo("4", "Renoult");
			entityManager.persist(modelo3);

			Vehiculo vehiculo = new Vehiculo(
					"Bolido perfectamente cuidado, con menos de mil kilometros, practicamente nuevo", "Negro", "VEV592",
					26000000, 2018, 2000, 2, 10000, ciudad, cliente, modelo, TipoCombustible.GASOLINA,
					TipoVehiculo.CARRO, TipoTrasmision.MECANICO, Condicion.NUEVO);

		}

	}

	public boolean isUsuario() {

		TypedQuery<Usuario> query = entityManager.createNamedQuery(Usuario.GETALL, Usuario.class);

		if (query.getResultList().isEmpty()) {

			return false;
		}

		return true;
	}

}
