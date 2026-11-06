package co.edu.uniquindio.unimotor.pruebas;

import java.util.HashMap;
import java.util.Map;

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

import co.edu.uniquindio.unimotor.entidades.Ciudad;
import co.edu.uniquindio.unimotor.entidades.Cliente;
import co.edu.uniquindio.unimotor.entidades.Modelo;

/**
 * Pruebas de persist/find, merge y remove para Cliente (con telefonos),
 * Ciudad y Modelo. Los ids de las entidades nuevas los genera IDENTITY.
 * <p>
 * Datasets: se cargan los 14 JSON de src/test/resources (modelo, administrador, caracteristica, caracteristica_vehiculo, ciudad, cliente, favorito, foto_vehiculo, pregunta, telefono_usuario, usuario, vehiculo, venta y calificacion) sobre la
 * base {@code prueba}. Cada prueba corre con {@code TransactionMode.ROLLBACK}, asi que
 * los cambios no persisten y no afectan a las demas pruebas.
 * <p>
 * Datos del JSON usados: cliente 4, ciudad 3 (Pijao) y modelo 2 (Mazda 3).
 */
@RunWith(Arquillian.class)
@UsingDataSet({ Datasets.MODELO, Datasets.ADMINISTRADOR, Datasets.CARACTERISTICA, Datasets.CARACTERISTICA_VEHICULO,
		Datasets.CIUDAD, Datasets.CLIENTE, Datasets.FAVORITO, Datasets.FOTO_VEHICULO, Datasets.PREGUNTA,
		Datasets.TELEFONO_USUARIO, Datasets.USUARIO, Datasets.VEHICULO, Datasets.VENTA, Datasets.CALIFICACION })
public class UsuarioCatalogoTest {

	@PersistenceContext
	private EntityManager entityManager;

	@Deployment
	public static Archive<?> createTestArchive() {
		return Despliegue.crear();
	}

	/** Crea (sin persistir) un cliente nuevo con dos telefonos en la ciudad dada. */
	private Cliente nuevoCliente(String email, Ciudad ciudad) {
		Map<String, String> telefonos = new HashMap<>();
		telefonos.put("Personal", "3001112233");
		telefonos.put("Casa", "7445566");
		return new Cliente(email, "clave", "Laura Gomez", "Calle 10 #5-20", telefonos, ciudad);
	}

	/** Persiste un cliente con 2 telefonos y verifica email, nombre, ciudad y telefonos tras recargar. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void persistFindClienteTest() {
		Ciudad armenia = entityManager.find(Ciudad.class, 1L);
		Cliente nuevo = nuevoCliente("laura@servidor.com", armenia);

		entityManager.persist(nuevo);
		entityManager.flush();
		Long id = nuevo.getCodigo_usuario();
		entityManager.clear();

		Cliente buscado = entityManager.find(Cliente.class, id);
		Assert.assertNotNull(buscado);
		Assert.assertEquals("laura@servidor.com", buscado.getEmail());
		Assert.assertEquals("Laura Gomez", buscado.getNombre_completo());
		Assert.assertEquals("Armenia", buscado.getCodigo_ciudad().getNombre());
		Assert.assertEquals(2, buscado.getTelefono().size());
		Assert.assertEquals("3001112233", buscado.getTelefono().get("Personal"));
		Assert.assertEquals("7445566", buscado.getTelefono().get("Casa"));
	}

	/** Busca el cliente 4 del dataset y verifica sus datos y sus dos telefonos. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void findClienteExistenteTest() {
		Cliente c = entityManager.find(Cliente.class, 4L);

		Assert.assertNotNull(c);
		Assert.assertEquals("usuario1@servidor.com", c.getEmail());
		Assert.assertEquals("Hector Rodriguez", c.getNombre_completo());
		Assert.assertEquals("Armenia", c.getCodigo_ciudad().getNombre());
		Assert.assertEquals(2, c.getTelefono().size());
		Assert.assertEquals("3135638905", c.getTelefono().get("Personal"));
		Assert.assertEquals("3155679045", c.getTelefono().get("Personal 2"));
	}

	/** Modifica direccion y un telefono de un cliente nuevo y comprueba que se guardo en la base. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void mergeClienteTest() {
		Ciudad armenia = entityManager.find(Ciudad.class, 1L);
		Cliente nuevo = nuevoCliente("merge@servidor.com", armenia);
		entityManager.persist(nuevo);
		entityManager.flush();
		Long id = nuevo.getCodigo_usuario();
		entityManager.clear();

		Cliente buscado = entityManager.find(Cliente.class, id);
		buscado.setDireccion("Carrera 15 #8-30");
		buscado.getTelefono().put("Personal", "3209998877");
		entityManager.merge(buscado);
		entityManager.flush();
		entityManager.clear();

		Cliente actualizado = entityManager.find(Cliente.class, id);
		Assert.assertEquals("Carrera 15 #8-30", actualizado.getDireccion());
		Assert.assertEquals("3209998877", actualizado.getTelefono().get("Personal"));
		Assert.assertEquals("7445566", actualizado.getTelefono().get("Casa"));
		Assert.assertEquals(2, actualizado.getTelefono().size());
	}

	/** Elimina un cliente nuevo sin dependencias y verifica que find devuelve null. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void removeClienteTest() {
		Ciudad armenia = entityManager.find(Ciudad.class, 1L);
		Cliente nuevo = nuevoCliente("borrar@servidor.com", armenia);
		entityManager.persist(nuevo);
		entityManager.flush();
		Long id = nuevo.getCodigo_usuario();
		entityManager.clear();

		Cliente eliminar = entityManager.find(Cliente.class, id);
		Assert.assertNotNull(eliminar);
		entityManager.remove(eliminar);
		entityManager.flush();
		entityManager.clear();

		Assert.assertNull(entityManager.find(Cliente.class, id));
	}

	/** Persiste una ciudad y verifica su nombre al buscarla. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void persistFindCiudadTest() {
		Ciudad nueva = new Ciudad("Calarca", "Quindío");
		entityManager.persist(nueva);
		entityManager.flush();
		Long id = nueva.getCodigo_ciudad();
		entityManager.clear();

		Ciudad buscada = entityManager.find(Ciudad.class, id);
		Assert.assertNotNull(buscada);
		Assert.assertEquals("Calarca", buscada.getNombre());
		Assert.assertEquals("Quindío", buscada.getDepartamento());
	}

	/** Busca la ciudad 3 del dataset (Pijao). */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void findCiudadExistenteTest() {
		Ciudad c = entityManager.find(Ciudad.class, 3L);

		Assert.assertNotNull(c);
		Assert.assertEquals("Pijao", c.getNombre());
	}

	/** Cambia el nombre de una ciudad existente y verifica que se guardo en la base. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void mergeCiudadTest() {
		Ciudad c = entityManager.find(Ciudad.class, 2L);
		c.setNombre("Montenegro");
		entityManager.merge(c);
		entityManager.flush();
		entityManager.clear();

		Assert.assertEquals("Montenegro", entityManager.find(Ciudad.class, 2L).getNombre());
	}

	/** Elimina una ciudad nueva sin clientes y verifica que find devuelve null. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void removeCiudadTest() {
		Ciudad nueva = new Ciudad("Circasia");
		entityManager.persist(nueva);
		entityManager.flush();
		Long id = nueva.getCodigo_ciudad();
		entityManager.clear();

		entityManager.remove(entityManager.find(Ciudad.class, id));
		entityManager.flush();
		entityManager.clear();

		Assert.assertNull(entityManager.find(Ciudad.class, id));
	}

	/** Persiste un modelo y verifica nombre y marca al buscarlo. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void persistFindModeloTest() {
		Modelo nuevo = new Modelo("Corolla", "Toyota");
		entityManager.persist(nuevo);
		entityManager.flush();
		Long id = nuevo.getCodigo_modelo();
		entityManager.clear();

		Modelo buscado = entityManager.find(Modelo.class, id);
		Assert.assertNotNull(buscado);
		Assert.assertEquals("Corolla", buscado.getNombre());
		Assert.assertEquals("Toyota", buscado.getMarca());
	}

	/** Busca el modelo 2 del dataset (Mazda 3). */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void findModeloExistenteTest() {
		Modelo m = entityManager.find(Modelo.class, 2L);

		Assert.assertNotNull(m);
		Assert.assertEquals("Mazda", m.getMarca());
		Assert.assertEquals("3", m.getNombre());
	}

	/** Cambia nombre y marca de un modelo existente y verifica que se guardo en la base. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void mergeModeloTest() {
		Modelo m = entityManager.find(Modelo.class, 1L);
		m.setNombre("A3");
		m.setMarca("Audi AG");
		entityManager.merge(m);
		entityManager.flush();
		entityManager.clear();

		Modelo actualizado = entityManager.find(Modelo.class, 1L);
		Assert.assertEquals("A3", actualizado.getNombre());
		Assert.assertEquals("Audi AG", actualizado.getMarca());
	}

	/** Elimina un modelo nuevo sin vehiculos y verifica que find devuelve null. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void removeModeloTest() {
		Modelo nuevo = new Modelo("Spark", "Chevrolet");
		entityManager.persist(nuevo);
		entityManager.flush();
		Long id = nuevo.getCodigo_modelo();
		entityManager.clear();

		entityManager.remove(entityManager.find(Modelo.class, id));
		entityManager.flush();
		entityManager.clear();

		Assert.assertNull(entityManager.find(Modelo.class, id));
	}
}
