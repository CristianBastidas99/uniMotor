package co.edu.uniquindio.unimotor.pruebas;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;


import co.edu.uniquindio.unimotor.entidades.*;

/**
 * Pruebas de persistencia de Administrador y Caracteristica (persist/find, merge y remove)
 * y de algunas NamedQueries de Cliente y Vehiculo (listados que solo imprimen resultados).
 * <p>
 * Datasets: se cargan los 14 JSON de src/test/resources (modelo, administrador, caracteristica, caracteristica_vehiculo, ciudad, cliente, favorito, foto_vehiculo, pregunta, telefono_usuario, usuario, vehiculo, venta y calificacion) con DBUnit antes de cada
 * prueba, sobre la base {@code prueba}. Cada prueba corre con {@code TransactionMode.ROLLBACK},
 * por lo que los cambios no se conservan y las pruebas son independientes entre si.
 * <p>
 * Datos usados: administradores 1 y 3 (hay 3 en total) y caracteristicas 1 y 3 (hay 3 en total).
 */
@RunWith(Arquillian.class)
@UsingDataSet({ Datasets.MODELO, Datasets.ADMINISTRADOR, Datasets.CARACTERISTICA, Datasets.CARACTERISTICA_VEHICULO,
		Datasets.CIUDAD, Datasets.CLIENTE, Datasets.FAVORITO, Datasets.FOTO_VEHICULO, Datasets.PREGUNTA,
		Datasets.TELEFONO_USUARIO, Datasets.USUARIO, Datasets.VEHICULO, Datasets.VENTA, Datasets.CALIFICACION })
public class EntidadTest {

	@PersistenceContext
	private EntityManager entityManager;

	@Deployment
	public static Archive<?> createTestArchive() {
		return Despliegue.crear();
	}

	/**
	 * Prueba vacia reservada; solo comprueba que el despliegue y la carga de los datasets funcionan.
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void buscarTests() {

	}
	
	/**
	 * Prueba vacia reservada; solo comprueba que el despliegue y la carga de los datasets funcionan.
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void buscarTests2() {

	}

	/**
	 * Persiste un administrador nuevo (codigo 7) y verifica su email al buscarlo con find.
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void persistFindAdministradorTest() {

		Administrador miAdministrador = new Administrador();

		miAdministrador.setCodigo_usuario(7l);
		miAdministrador.setNombre_administrador("Pepito");
		miAdministrador.setEmail("pepito@email.com");
		miAdministrador.setPassword("123");

		entityManager.persist(miAdministrador);

		Administrador administradorbuscado = entityManager.find(Administrador.class, 7l);

		Assert.assertEquals("pepito@email.com", administradorbuscado.getEmail());

	}

	/**
	 * Cambia el nombre del administrador 1 del JSON y verifica que el cambio se refleja al buscarlo.
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void mergeAdministradorTest() {

		Administrador administradorbuscado = entityManager.find(Administrador.class, 1l);

		administradorbuscado.setNombre_administrador("Fabian");

		entityManager.merge(administradorbuscado);

		Administrador miAdministrador = entityManager.find(Administrador.class, 1l);

		Assert.assertEquals("Fabian", miAdministrador.getNombre_administrador());

	}

	/**
	 * Elimina el administrador 3 del JSON y verifica con {@code Administrador.GETALL} que quedan 2.
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void removeAdministradorTest() {

		Administrador administradorEliminar = entityManager.find(Administrador.class, 3l);
		entityManager.remove(administradorEliminar);

		TypedQuery<Administrador> query = entityManager.createNamedQuery(Administrador.GETALL, Administrador.class);
		List<Administrador> resultados = query.getResultList();

		Assert.assertEquals(2, resultados.size());
	}

	/**
	 * Persiste la caracteristica 4 ("Frenos ABS") y verifica su nombre al buscarla.
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void persistFindCaracteristicaTest() {

		Caracteristica miCaracteristica = new Caracteristica();

		miCaracteristica.setCodigo_caracteristica(4l);
		miCaracteristica.setNombre("Frenos ABS");

		entityManager.persist(miCaracteristica);

		Caracteristica buscado = entityManager.find(Caracteristica.class, 4l);

		Assert.assertEquals("Frenos ABS", buscado.getNombre());

	}

	/**
	 * Cambia el nombre de la caracteristica 1 del JSON y verifica que se actualizo.
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void mergeCaracteristicaTest() {

		Caracteristica buscado = entityManager.find(Caracteristica.class, 1l);

		buscado.setNombre("Aire Acondicionado");

		entityManager.merge(buscado);

		Caracteristica miCaracteristica = entityManager.find(Caracteristica.class, 1l);

		Assert.assertEquals("Aire Acondicionado", miCaracteristica.getNombre());

	}

	/**
	 * Elimina la caracteristica 3 del JSON y verifica con {@code Caracteristica.GETALL} que quedan 2.
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void removeCaracteristicaTest() {

		Caracteristica eliminar = entityManager.find(Caracteristica.class, 3l);
		entityManager.remove(eliminar);

		TypedQuery<Caracteristica> query = entityManager.createNamedQuery(Caracteristica.GETALL, Caracteristica.class);
		List<Caracteristica> resultados = query.getResultList();

		Assert.assertEquals(2, resultados.size());
	}

	/**
	 * Ejecuta {@code Cliente.LISTA_CIUDAD_CLIENTE} sobre los clientes del JSON e imprime cada par (no tiene asserts).
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void listarCiudadCLiente() {

		TypedQuery<Object[]> query = entityManager.createNamedQuery(Cliente.LISTA_CIUDAD_CLIENTE, Object[].class);

		for (Object[] o : query.getResultList()) {

			System.out.print(o[0] + " - " + o[1] + "\n");

		}

	}

	/**
	 * Ejecuta {@code Cliente.LISTA_FAVORITO_CLIENTE} para usuario1@servidor.com (favorito.json) e imprime los vehiculos (no tiene asserts).
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void listarFavoritoCLiente() {

		TypedQuery<Vehiculo> query = entityManager.createNamedQuery(Cliente.LISTA_FAVORITO_CLIENTE, Vehiculo.class);
		query.setParameter("email", "usuario1@servidor.com");

		for (Vehiculo o : query.getResultList()) {
			System.out.print(o + "\n");
		}
	}
	
	/**
	 * Ejecuta {@code Cliente.CANTIDAD_DE_CLIENTES} e imprime el total de clientes del JSON (no tiene asserts).
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void cantidadDePersonasTest() {

		TypedQuery<Long> query = entityManager.createNamedQuery(Cliente.CANTIDAD_DE_CLIENTES, Long.class);

			System.out.print(query.getSingleResult());
		
	}
	
	/**
	 * Ejecuta {@code Vehiculo.GET_ALL_COLOR} e imprime los colores de los vehiculos del JSON (no tiene asserts).
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void listarColoresVehiculo() {

		TypedQuery<String> query = entityManager.createNamedQuery(Vehiculo.GET_ALL_COLOR, String.class);

		for (String o : query.getResultList()) {
			System.out.print(o + "\n");
		}
	}
	
	/**
	 * Ejecuta {@code Vehiculo.FIND_BY_ASCENDENTE_PRECIO} e imprime los vehiculos de menor a mayor precio (no tiene asserts).
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void listarPrecioAscendente() {

		TypedQuery<Vehiculo> query = entityManager.createNamedQuery(Vehiculo.FIND_BY_ASCENDENTE_PRECIO, Vehiculo.class);

		for (Vehiculo o : query.getResultList()) {
			System.out.print(o + "\n");
		}
	}
	
	/**
	 * Ejecuta {@code Vehiculo.FIND_BY_DESCENDENTE_PRECIO} e imprime los vehiculos de mayor a menor precio (no tiene asserts).
	 */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void listarPrecioDescendente() {

		TypedQuery<Vehiculo> query = entityManager.createNamedQuery(Vehiculo.FIND_BY_DESCENDENTE_PRECIO, Vehiculo.class);

		for (Vehiculo o : query.getResultList()) {
			System.out.print(o + "\n");
		}
	}

}
