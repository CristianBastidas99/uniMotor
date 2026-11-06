package co.edu.uniquindio.unimotor.pruebas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import co.edu.uniquindio.unimotor.entidades.*;

/**
 * Pruebas CRUD (persist/find, merge, remove) de Vehiculo, Pregunta, Venta y
 * Calificacion. Todas corren en una transaccion con rollback sobre los datasets
 * JSON. Datos usados: clientes 4, 5 y 6; ciudad 1; modelo 1; caracteristicas 1 y 2;
 * vehiculos 1 a 7 (vendidos: 2, 6 y 7); calificaciones 5 a 6, 4 a 6 y 6 a 5.
 * <p>
 * Datasets: se cargan los 14 JSON de src/test/resources (modelo, administrador, caracteristica, caracteristica_vehiculo, ciudad, cliente, favorito, foto_vehiculo, pregunta, telefono_usuario, usuario, vehiculo, venta y calificacion) sobre la
 * base {@code prueba}; cada prueba hace rollback al terminar, por lo que los datos
 * vuelven siempre al estado del JSON.
 */
@RunWith(Arquillian.class)
@UsingDataSet({ Datasets.MODELO, Datasets.ADMINISTRADOR, Datasets.CARACTERISTICA, Datasets.CARACTERISTICA_VEHICULO,
		Datasets.CIUDAD, Datasets.CLIENTE, Datasets.FAVORITO, Datasets.FOTO_VEHICULO, Datasets.PREGUNTA,
		Datasets.TELEFONO_USUARIO, Datasets.USUARIO, Datasets.VEHICULO, Datasets.VENTA, Datasets.CALIFICACION })
public class VehiculoTest {

	private static final double PRECIO_GRANDE = 3500000000d;

	@PersistenceContext
	private EntityManager entityManager;

	@Deployment
	public static Archive<?> createTestArchive() {
		return Despliegue.crear();
	}

	/** Construye un vehiculo valido (sin persistir) con la placa indicada. */
	private Vehiculo nuevoVehiculo(String placa) {
		Vehiculo v = new Vehiculo();
		v.setPlaca(placa);
		v.setDescripcion("Vehiculo de prueba");
		v.setColor("Plata");
		v.setPrecio(PRECIO_GRANDE);
		v.setYear(2021);
		v.setCilindraje(1800);
		v.setNumero_puertas(4);
		v.setKilometros(500);
		v.setCodigo_ciudad(entityManager.find(Ciudad.class, 1l));
		v.setCodigo_dueno(entityManager.find(Cliente.class, 4l));
		v.setCodigo_modelo(entityManager.find(Modelo.class, 1l));
		v.setTipo_combustible(TipoCombustible.GASOLINA);
		v.setTipo_vehiculo(TipoVehiculo.CARRO);
		v.setTipo_trasmision(TipoTrasmision.AUTOMATICO);
		v.setCondicion(Condicion.USADO);
		v.setUrl_foto(new ArrayList<>(Arrays.asList("/imagenes/nueva1.jpg", "/imagenes/nueva2.jpg")));
		v.setCaracteristicas(new ArrayList<>(Arrays.asList(entityManager.find(Caracteristica.class, 1l),
				entityManager.find(Caracteristica.class, 2l))));
		return v;
	}

	private void sincronizar() {
		entityManager.flush();
		entityManager.clear();
	}

	/** Persiste un vehiculo con 2 fotos y 2 caracteristicas y verifica todo tras flush+clear. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void persistFindVehiculoTest() {
		Vehiculo v = nuevoVehiculo("NUE123");
		entityManager.persist(v);
		sincronizar();

		Vehiculo b = entityManager.find(Vehiculo.class, v.getCodigo_vehiculo());
		Assert.assertNotNull(b);
		Assert.assertEquals("NUE123", b.getPlaca());
		Assert.assertEquals(PRECIO_GRANDE, b.getPrecio(), 0.001);
		Assert.assertTrue(b.getPrecio() > Integer.MAX_VALUE);
		Assert.assertEquals(4l, b.getCodigo_dueno().getCodigo_usuario().longValue());
		Assert.assertEquals(2, b.getUrl_foto().size());
		Assert.assertTrue(b.getUrl_foto().contains("/imagenes/nueva1.jpg"));
		Assert.assertTrue(b.getUrl_foto().contains("/imagenes/nueva2.jpg"));
		Assert.assertEquals(2, b.getCaracteristicas().size());
		List<Long> ids = new ArrayList<>();
		for (Caracteristica c : b.getCaracteristicas()) {
			ids.add(c.getCodigo_caracteristica());
		}
		Assert.assertTrue(ids.contains(1l) && ids.contains(2l));
		Assert.assertNotNull("@PrePersist debio llenar la fecha", b.getFecha_publicacion());
		Assert.assertEquals(0, b.getNumero_busquedas());
		Assert.assertTrue(b.isActivo());
	}

	/** Un vehiculo existente del JSON se encuentra con sus datos y colecciones. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void findVehiculoExistenteTest() {
		Vehiculo v = entityManager.find(Vehiculo.class, 1l);
		Assert.assertNotNull(v);
		Assert.assertEquals("VEV592", v.getPlaca());
		Assert.assertEquals(26000000d, v.getPrecio(), 0.001);
		Assert.assertEquals(2, v.getUrl_foto().size());
		Assert.assertEquals(57, v.getNumero_busquedas());
		Assert.assertTrue(v.isActivo());
		Assert.assertEquals(4l, v.getCodigo_dueno().getCodigo_usuario().longValue());
	}

	/** Modifica precio, busquedas y activo; verifica que se guardaron en la base. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void mergeVehiculoTest() {
		Vehiculo v = entityManager.find(Vehiculo.class, 1l);
		v.setPrecio(PRECIO_GRANDE);
		v.setNumero_busquedas(v.getNumero_busquedas() + 1);
		v.setActivo(false);
		entityManager.merge(v);
		sincronizar();

		Vehiculo b = entityManager.find(Vehiculo.class, 1l);
		Assert.assertEquals(PRECIO_GRANDE, b.getPrecio(), 0.001);
		Assert.assertEquals(58, b.getNumero_busquedas());
		Assert.assertFalse(b.isActivo());
	}

	/** Elimina un vehiculo nuevo (sin preguntas ni venta) y verifica que find da null. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void removeVehiculoTest() {
		Vehiculo v = nuevoVehiculo("DEL999");
		entityManager.persist(v);
		sincronizar();
		Long id = v.getCodigo_vehiculo();

		Vehiculo a = entityManager.find(Vehiculo.class, id);
		Assert.assertNotNull(a);
		entityManager.remove(a);
		sincronizar();

		Assert.assertNull(entityManager.find(Vehiculo.class, id));
	}

	/** Persistir un vehiculo con una placa ya existente debe lanzar PersistenceException. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void placaDuplicadaTest() {
		Vehiculo v = nuevoVehiculo("VEV592");
		try {
			entityManager.persist(v);
			entityManager.flush();
			Assert.fail("Se esperaba PersistenceException por placa duplicada");
		} catch (PersistenceException e) {
			// esperado
		}
	}

	/** Persiste una pregunta (id autogenerado) y verifica sus datos. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void persistFindPreguntaTest() {
		Pregunta p = new Pregunta();
		p.setVehiculo(entityManager.find(Vehiculo.class, 3l));
		p.setCliente(entityManager.find(Cliente.class, 5l));
		p.setFecha(new Date());
		p.setDescripcion("Aceptan permuta?");
		entityManager.persist(p);
		sincronizar();

		Assert.assertNotNull(p.getCodigo_pregunta());
		Pregunta b = entityManager.find(Pregunta.class, p.getCodigo_pregunta());
		Assert.assertEquals("Aceptan permuta?", b.getDescripcion());
		Assert.assertEquals(3l, b.getVehiculo().getCodigo_vehiculo().longValue());
		Assert.assertEquals(5l, b.getCliente().getCodigo_usuario().longValue());
		Assert.assertNull(b.getRespuesta());
		Assert.assertNull(b.getFecha_respuesta());
	}

	/** El mismo cliente puede hacer dos preguntas al mismo vehiculo con ids distintos. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void dosPreguntasMismoClienteVehiculoTest() {
		Pregunta p1 = new Pregunta();
		Pregunta p2 = new Pregunta();
		Pregunta[] ps = { p1, p2 };
		String[] textos = { "Primera pregunta", "Segunda pregunta" };
		for (int i = 0; i < 2; i++) {
			ps[i].setVehiculo(entityManager.find(Vehiculo.class, 4l));
			ps[i].setCliente(entityManager.find(Cliente.class, 5l));
			ps[i].setFecha(new Date());
			ps[i].setDescripcion(textos[i]);
			entityManager.persist(ps[i]);
		}
		sincronizar();

		Assert.assertNotEquals(p1.getCodigo_pregunta(), p2.getCodigo_pregunta());
		Assert.assertEquals("Primera pregunta", entityManager.find(Pregunta.class, p1.getCodigo_pregunta()).getDescripcion());
		Assert.assertEquals("Segunda pregunta", entityManager.find(Pregunta.class, p2.getCodigo_pregunta()).getDescripcion());
	}

	/** Responde una pregunta sin respuesta del JSON (id 2) y verifica la persistencia. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void mergePreguntaTest() {
		Pregunta p = entityManager.find(Pregunta.class, 2l);
		Assert.assertNull(p.getRespuesta());
		p.setRespuesta("Si, todo al dia.");
		p.setFecha_respuesta(new Date());
		entityManager.merge(p);
		sincronizar();

		Pregunta b = entityManager.find(Pregunta.class, 2l);
		Assert.assertEquals("Si, todo al dia.", b.getRespuesta());
		Assert.assertNotNull(b.getFecha_respuesta());
	}

	/** Elimina una pregunta existente y verifica que ya no se encuentra. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void removePreguntaTest() {
		Pregunta p = entityManager.find(Pregunta.class, 4l);
		Assert.assertNotNull(p);
		entityManager.remove(p);
		sincronizar();

		Assert.assertNull(entityManager.find(Pregunta.class, 4l));
		Assert.assertNotNull(entityManager.find(Pregunta.class, 3l));
	}

	/** Persiste una venta sobre el vehiculo 1 (no vendido en venta.json). */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void persistFindVentaTest() {
		Venta v = new Venta();
		v.setFecha(new Date());
		v.setCodigo_vehiculo(entityManager.find(Vehiculo.class, 1l));
		v.setCodigo_usuario(entityManager.find(Cliente.class, 5l));
		entityManager.persist(v);
		sincronizar();

		Assert.assertNotNull(v.getCodigo_venta());
		Venta b = entityManager.find(Venta.class, v.getCodigo_venta());
		Assert.assertNotNull(b.getFecha());
		Assert.assertEquals(1l, b.getCodigo_vehiculo().getCodigo_vehiculo().longValue());
		Assert.assertEquals(5l, b.getCodigo_usuario().getCodigo_usuario().longValue());
	}

	/** Cambia la fecha de una venta existente (id 1) y verifica que se guardo. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void mergeVentaTest() {
		Date nueva = new Date(1700000000000L);
		Venta v = entityManager.find(Venta.class, 1l);
		v.setFecha(nueva);
		entityManager.merge(v);
		sincronizar();

		Venta b = entityManager.find(Venta.class, 1l);
		Assert.assertEquals(nueva.getTime(), b.getFecha().getTime());
	}

	/** Elimina una venta existente (id 3) y verifica que find devuelve null. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void removeVentaTest() {
		Venta v = entityManager.find(Venta.class, 3l);
		Assert.assertNotNull(v);
		entityManager.remove(v);
		sincronizar();

		Assert.assertNull(entityManager.find(Venta.class, 3l));
		Assert.assertNotNull(entityManager.find(Venta.class, 1l));
	}

	private Calificacion nuevaCalificacion(long calificador, long vendedor, int estrellas) {
		Calificacion c = new Calificacion();
		c.setCalificador(entityManager.find(Cliente.class, calificador));
		c.setVendedor(entityManager.find(Cliente.class, vendedor));
		c.setEstrellas(estrellas);
		c.setComentario("Comentario de prueba");
		return c;
	}

	/** Persiste calificacion 4 a 5 (no existe en el JSON) y verifica que la fecha la pone el @PrePersist. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void persistFindCalificacionTest() {
		Calificacion c = nuevaCalificacion(4l, 5l, 4);
		Assert.assertNull(c.getFecha());
		entityManager.persist(c);
		sincronizar();

		Calificacion b = entityManager.find(Calificacion.class, c.getCodigo_calificacion());
		Assert.assertEquals(4, b.getEstrellas());
		Assert.assertEquals("Comentario de prueba", b.getComentario());
		Assert.assertNotNull(b.getFecha());
		Assert.assertEquals(4l, b.getCalificador().getCodigo_usuario().longValue());
		Assert.assertEquals(5l, b.getVendedor().getCodigo_usuario().longValue());
	}

	/** Modifica comentario y estrellas de una calificacion existente. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void mergeCalificacionTest() {
		Calificacion c = entityManager.find(Calificacion.class, 3l);
		c.setComentario("Cambie de opinion");
		c.setEstrellas(5);
		entityManager.merge(c);
		sincronizar();

		Calificacion b = entityManager.find(Calificacion.class, 3l);
		Assert.assertEquals("Cambie de opinion", b.getComentario());
		Assert.assertEquals(5, b.getEstrellas());
	}

	/** Elimina una calificacion y verifica que find devuelve null. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void removeCalificacionTest() {
		Calificacion c = entityManager.find(Calificacion.class, 2l);
		Assert.assertNotNull(c);
		entityManager.remove(c);
		sincronizar();

		Assert.assertNull(entityManager.find(Calificacion.class, 2l));
		Assert.assertNotNull(entityManager.find(Calificacion.class, 1l));
	}

	/** Estrellas fuera de 1..5 deben fallar con IllegalArgumentException (posiblemente envuelta). */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void estrellasInvalidasTest() {
		int[] invalidas = { 0, 6 };
		for (int e : invalidas) {
			Calificacion c = nuevaCalificacion(4l, 5l, e);
			try {
				entityManager.persist(c);
				entityManager.flush();
				Assert.fail("Se esperaba excepcion para estrellas=" + e);
			} catch (RuntimeException ex) {
				Throwable t = ex;
				boolean encontrada = false;
				while (t != null) {
					if (t instanceof IllegalArgumentException) {
						encontrada = true;
						break;
					}
					t = t.getCause();
				}
				Assert.assertTrue("La causa debia ser IllegalArgumentException: " + ex, encontrada);
			}
		}
	}

	/** El mismo calificador (5) no puede calificar dos veces al mismo vendedor (6). */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void calificacionDuplicadaTest() {
		Calificacion c = nuevaCalificacion(5l, 6l, 3);
		try {
			entityManager.persist(c);
			entityManager.flush();
			Assert.fail("Se esperaba PersistenceException por calificacion duplicada");
		} catch (PersistenceException e) {
			// esperado
		}
	}
}
