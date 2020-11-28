package co.edu.uniuindio.pruebas;

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

import co.edu.uniquindio.empresa.*;

@RunWith(Arquillian.class)
public class EntidadTest {

	@PersistenceContext
	private EntityManager entityManager;

	@Deployment
	public static Archive<?> createTestArchive() {
		return ShrinkWrap.create(WebArchive.class,

				"prueba.war").addPackage(Cliente.class.getPackage()).addAsResource("persistenceForTest.xml",

						"META-INF/persistence.xml")

				.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

	}

	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	@UsingDataSet({ "modelo.json", "administrador.json", "caracteristica.json", "caracteristica_vehiculo.json",
			"ciudad.json", "cliente.json", "favorito.json", "foto_vehiculo.json", "pregunta.json",
			"telefono_usuario.json", "usuario.json", "vehiculo.json", "venta.json" })
	public void buscarTests() {

	}

	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	@UsingDataSet({ "modelo.json", "administrador.json", "caracteristica.json", "caracteristica_vehiculo.json",
			"ciudad.json", "cliente.json", "favorito.json", "foto_vehiculo.json", "pregunta.json",
			"telefono_usuario.json", "usuario.json", "vehiculo.json", "venta.json" })
	public void persistFindAdministradorTest() {

		Administrador miAdministrador = new Administrador();

		miAdministrador.setCodigo_usuario("7");
		miAdministrador.setNombre_administrador("Pepito");
		miAdministrador.setEmail("pepito@email.com");
		miAdministrador.setPassword("123");

		entityManager.persist(miAdministrador);

		Administrador administradorbuscado = entityManager.find(Administrador.class, "7");

		Assert.assertEquals("pepito@email.com", administradorbuscado.getEmail());

	}

	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	@UsingDataSet({ "modelo.json", "administrador.json", "caracteristica.json", "caracteristica_vehiculo.json",
			"ciudad.json", "cliente.json", "favorito.json", "foto_vehiculo.json", "pregunta.json",
			"telefono_usuario.json", "usuario.json", "vehiculo.json", "venta.json" })
	public void mergeAdministradorTest() {

		Administrador administradorbuscado = entityManager.find(Administrador.class, "1");

		administradorbuscado.setNombre_administrador("Fabian");

		entityManager.merge(administradorbuscado);

		Administrador miAdministrador = entityManager.find(Administrador.class, "1");

		Assert.assertEquals("Fabian", miAdministrador.getNombre_administrador());

	}

	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	@UsingDataSet({ "modelo.json", "administrador.json", "caracteristica.json", "caracteristica_vehiculo.json",
			"ciudad.json", "cliente.json", "favorito.json", "foto_vehiculo.json", "pregunta.json",
			"telefono_usuario.json", "usuario.json", "vehiculo.json", "venta.json" })
	public void removeAdministradorTest() {

		Administrador administradorEliminar = entityManager.find(Administrador.class, "3");
		entityManager.remove(administradorEliminar);

		TypedQuery<Administrador> query = entityManager.createNamedQuery(Administrador.GETALL, Administrador.class);
		List<Administrador> resultados = query.getResultList();

		Assert.assertEquals(2, resultados.size());
	}

	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	@UsingDataSet({ "modelo.json", "administrador.json", "caracteristica.json", "caracteristica_vehiculo.json",
			"ciudad.json", "cliente.json", "favorito.json", "foto_vehiculo.json", "pregunta.json",
			"telefono_usuario.json", "usuario.json", "vehiculo.json", "venta.json" })
	public void persistFindCaracteristicaTest() {

		Caracteristica miCaracteristica = new Caracteristica();

		miCaracteristica.setCodigo_caracteristica("4");
		miCaracteristica.setNombre("Frenos ABS");

		entityManager.persist(miCaracteristica);

		Caracteristica buscado = entityManager.find(Caracteristica.class, "4");

		Assert.assertEquals("Frenos ABS", buscado.getNombre());

	}

	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	@UsingDataSet({ "modelo.json", "administrador.json", "caracteristica.json", "caracteristica_vehiculo.json",
			"ciudad.json", "cliente.json", "favorito.json", "foto_vehiculo.json", "pregunta.json",
			"telefono_usuario.json", "usuario.json", "vehiculo.json", "venta.json" })
	public void mergeCaracteristicaTest() {

		Caracteristica buscado = entityManager.find(Caracteristica.class, "1");

		buscado.setNombre("Aire Acondicionado");

		entityManager.merge(buscado);

		Caracteristica miCaracteristica = entityManager.find(Caracteristica.class, "1");

		Assert.assertEquals("Aire Acondicionado", miCaracteristica.getNombre());

	}

	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	@UsingDataSet({ "modelo.json", "administrador.json", "caracteristica.json", "caracteristica_vehiculo.json",
			"ciudad.json", "cliente.json", "favorito.json", "foto_vehiculo.json", "pregunta.json",
			"telefono_usuario.json", "usuario.json", "vehiculo.json", "venta.json" })
	public void removeCaracteristicaTest() {

		Caracteristica eliminar = entityManager.find(Caracteristica.class, "3");
		entityManager.remove(eliminar);

		TypedQuery<Caracteristica> query = entityManager.createNamedQuery(Caracteristica.GETALL, Caracteristica.class);
		List<Caracteristica> resultados = query.getResultList();

		Assert.assertEquals(2, resultados.size());
	}

	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	@UsingDataSet({ "modelo.json", "administrador.json", "caracteristica.json", "caracteristica_vehiculo.json",
			"ciudad.json", "cliente.json", "favorito.json", "foto_vehiculo.json", "pregunta.json",
			"telefono_usuario.json", "usuario.json", "vehiculo.json", "venta.json" })
	public void listarCiudadCLiente() {

		TypedQuery<Object[]> query = entityManager.createNamedQuery(Cliente.LISTA_CIUDAD_CLIENTE, Object[].class);

		for (Object[] o : query.getResultList()) {

			System.out.print(o[0] + " - " + o[1] + "\n");

		}

	}

	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	@UsingDataSet({ "modelo.json", "administrador.json", "caracteristica.json", "caracteristica_vehiculo.json",
			"ciudad.json", "cliente.json", "favorito.json", "foto_vehiculo.json", "pregunta.json",
			"telefono_usuario.json", "usuario.json", "vehiculo.json", "venta.json" })
	public void listarFavoritoCLiente() {

		TypedQuery<Vehiculo> query = entityManager.createNamedQuery(Cliente.LISTA_FAVORITO_CLIENTE, Vehiculo.class);
		query.setParameter("email", "usuario1@servidor.com");

		for (Vehiculo o : query.getResultList()) {
			System.out.print(o + "\n");
		}
	}

}
