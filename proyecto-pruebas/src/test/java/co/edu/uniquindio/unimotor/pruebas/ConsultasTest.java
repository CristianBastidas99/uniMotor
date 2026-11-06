package co.edu.uniquindio.unimotor.pruebas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.arquillian.transaction.api.annotation.TransactionMode;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import co.edu.uniquindio.unimotor.dto.EstadisticaDTO;
import co.edu.uniquindio.unimotor.dto.PuntoDTO;
import co.edu.uniquindio.unimotor.entidades.*;

/**
 * Pruebas de las NamedQueries de negocio y de estadisticas, con valores
 * esperados calculados a mano a partir de los datasets JSON de
 * src/test/resources. Datos clave: vehiculos activos = 1, 3, 4, 5; duenos:
 * 4 -> {1,4,5}, 5 -> {3,6}, 6 -> {2,7}.
 */
@RunWith(Arquillian.class)
@UsingDataSet({ Datasets.MODELO, Datasets.ADMINISTRADOR, Datasets.CARACTERISTICA, Datasets.CARACTERISTICA_VEHICULO,
		Datasets.CIUDAD, Datasets.CLIENTE, Datasets.FAVORITO, Datasets.FOTO_VEHICULO, Datasets.PREGUNTA,
		Datasets.TELEFONO_USUARIO, Datasets.USUARIO, Datasets.VEHICULO, Datasets.VENTA, Datasets.CALIFICACION })
public class ConsultasTest {

	private static final double DELTA = 0.001;

	@PersistenceContext
	private EntityManager entityManager;

	@Deployment
	public static Archive<?> createTestArchive() {
		return Despliegue.crear();
	}

	// ---------- utilidades ----------

	private List<Long> idsVehiculos(List<Vehiculo> lista) {
		List<Long> ids = new ArrayList<>();
		for (Vehiculo v : lista) {
			ids.add(v.getCodigo_vehiculo());
		}
		return ids;
	}

	private List<Long> idsPreguntas(List<Pregunta> lista) {
		List<Long> ids = new ArrayList<>();
		for (Pregunta p : lista) {
			ids.add(p.getCodigo_pregunta());
		}
		return ids;
	}

	private List<EstadisticaDTO> estadistica(String consulta) {
		return entityManager.createNamedQuery(consulta, EstadisticaDTO.class).getResultList();
	}

	/** Convierte la lista en mapa etiqueta -> valor (long), verificando que no haya etiquetas repetidas. */
	private Map<String, Long> aMapaLong(List<EstadisticaDTO> lista) {
		Map<String, Long> mapa = new HashMap<>();
		for (EstadisticaDTO e : lista) {
			Assert.assertNull("Etiqueta repetida: " + e.getEtiqueta(), mapa.put(e.getEtiqueta(), e.getValor().longValue()));
		}
		return mapa;
	}

	private void assertEstadistica(String etiqueta, long valor, EstadisticaDTO e) {
		Assert.assertEquals(etiqueta, e.getEtiqueta());
		Assert.assertEquals(valor, e.getValor().longValue());
	}

	private void assertEstadistica(String etiqueta, double valor, EstadisticaDTO e) {
		Assert.assertEquals(etiqueta, e.getEtiqueta());
		Assert.assertEquals(valor, e.getValor().doubleValue(), DELTA);
	}

	// ---------- Vehiculo ----------

	/** El dueno 4 tiene los vehiculos 1, 4 y 5, del mas reciente al mas antiguo. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void vehiculosPorDuenoTest() {
		List<Vehiculo> r = entityManager.createNamedQuery(Vehiculo.VEHICULOS_POR_DUENO, Vehiculo.class)
				.setParameter("codigo", 4L).getResultList();
		Assert.assertEquals(Arrays.asList(5L, 4L, 1L), idsVehiculos(r));
	}

	/** Por email del dueno incluye tambien inactivos: usuario3 tiene 2 y 7. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void vehiculosPorEmailDuenoTest() {
		List<Vehiculo> r = entityManager.createNamedQuery(Vehiculo.VEHICULOS_POR_EMAIL_DUENO, Vehiculo.class)
				.setParameter("email", "usuario3@servidor.com").getResultList();
		Assert.assertEquals(Arrays.asList(2L, 7L), idsVehiculos(r));
		Assert.assertEquals("usuario3@servidor.com", r.get(0).getCodigo_dueno().getEmail());
	}

	private TypedQuery<Vehiculo> busqueda(Object tipo, Object marca, Object pMin, Object pMax, Object aMin,
			Object aMax) {
		return entityManager.createNamedQuery(Vehiculo.BUSQUEDA_COMBINADA, Vehiculo.class)
				.setParameter("tipo", tipo).setParameter("marca", marca).setParameter("precioMin", pMin)
				.setParameter("precioMax", pMax).setParameter("anioMin", aMin).setParameter("anioMax", aMax);
	}

	/** Carros Mazda activos con rango de precio y anio: 3 (2006) y 1 (2018), por fecha desc. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void busquedaCombinadaConFiltrosTest() {
		List<Vehiculo> r = busqueda(TipoVehiculo.CARRO, "Mazda", 1000000d, 30000000d, 2000, 2020).getResultList();
		Assert.assertEquals(Arrays.asList(3L, 1L), idsVehiculos(r));

		// Se estrecha el precio: solo el 3 (6.5M)
		r = busqueda(TipoVehiculo.CARRO, "Mazda", 1000000d, 10000000d, 2000, 2020).getResultList();
		Assert.assertEquals(Arrays.asList(3L), idsVehiculos(r));

		// Marca sin vehiculos activos: Renoult solo tiene inactivos
		r = busqueda(null, "Renoult", null, null, null, null).getResultList();
		Assert.assertTrue(r.isEmpty());
	}

	/** Con todos los parametros en null devuelve todos los activos (5, 4, 3, 1). */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void busquedaCombinadaSinFiltrosTest() {
		List<Vehiculo> r = busqueda(null, null, null, null, null, null).getResultList();
		Assert.assertEquals(Arrays.asList(5L, 4L, 3L, 1L), idsVehiculos(r));
	}

	/** La caracteristica 1 la tienen los vehiculos 1 y 3 (activos); la 2 solo el 2 (inactivo). */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void vehiculosPorCaracteristicaTest() {
		List<Vehiculo> r = entityManager.createNamedQuery(Vehiculo.VEHICULOS_POR_CARACTERISTICA, Vehiculo.class)
				.setParameter("codigoCaracteristica", 1L).getResultList();
		Assert.assertEquals(2, r.size());
		Assert.assertEquals(new HashSet<>(Arrays.asList(1L, 3L)), new HashSet<>(idsVehiculos(r)));

		r = entityManager.createNamedQuery(Vehiculo.VEHICULOS_POR_CARACTERISTICA, Vehiculo.class)
				.setParameter("codigoCaracteristica", 2L).getResultList();
		Assert.assertTrue(r.isEmpty());
	}

	/** Con la lista {1,2} salen los activos 1 y 3 sin duplicados; con {2} ninguno. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void vehiculosConCaracteristicasTest() {
		List<Vehiculo> r = entityManager.createNamedQuery(Vehiculo.VEHICULOS_CON_CARACTERISTICAS, Vehiculo.class)
				.setParameter("caracteristicas", Arrays.asList(1L, 2L)).getResultList();
		Assert.assertEquals(2, r.size());
		Assert.assertEquals(new HashSet<>(Arrays.asList(1L, 3L)), new HashSet<>(idsVehiculos(r)));

		r = entityManager.createNamedQuery(Vehiculo.VEHICULOS_CON_CARACTERISTICAS, Vehiculo.class)
				.setParameter("caracteristicas", Arrays.asList(2L)).getResultList();
		Assert.assertTrue(r.isEmpty());
	}

	/** Top de usados activos por busquedas: 3 (33) y 4 (8); nuevos: 5 (95) y 1 (57). */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void topBuscadosTest() {
		List<Vehiculo> r = entityManager.createNamedQuery(Vehiculo.TOP_BUSCADOS, Vehiculo.class)
				.setParameter("condicion", Condicion.USADO).setMaxResults(10).getResultList();
		Assert.assertEquals(Arrays.asList(3L, 4L), idsVehiculos(r));
		Assert.assertEquals(33, r.get(0).getNumero_busquedas());
		Assert.assertEquals(8, r.get(1).getNumero_busquedas());

		r = entityManager.createNamedQuery(Vehiculo.TOP_BUSCADOS, Vehiculo.class)
				.setParameter("condicion", Condicion.NUEVO).setMaxResults(10).getResultList();
		Assert.assertEquals(Arrays.asList(5L, 1L), idsVehiculos(r));

		r = entityManager.createNamedQuery(Vehiculo.TOP_BUSCADOS, Vehiculo.class)
				.setParameter("condicion", Condicion.NUEVO).setMaxResults(1).getResultList();
		Assert.assertEquals(Arrays.asList(5L), idsVehiculos(r));
	}

	/** Los activos, del mas reciente al mas antiguo: 5, 4, 3, 1. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void vehiculosActivosTest() {
		List<Vehiculo> r = entityManager.createNamedQuery(Vehiculo.VEHICULOS_ACTIVOS, Vehiculo.class).getResultList();
		Assert.assertEquals(Arrays.asList(5L, 4L, 3L, 1L), idsVehiculos(r));
	}

	/** Busqueda por placa existente e inexistente. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void findByPlacaTest() {
		List<Vehiculo> r = entityManager.createNamedQuery(Vehiculo.FIND_BY_PLACA, Vehiculo.class)
				.setParameter("placa", "VEV592").getResultList();
		Assert.assertEquals(1, r.size());
		Assert.assertEquals(Long.valueOf(1L), r.get(0).getCodigo_vehiculo());

		r = entityManager.createNamedQuery(Vehiculo.FIND_BY_PLACA, Vehiculo.class).setParameter("placa", "ZZZ000")
				.getResultList();
		Assert.assertTrue(r.isEmpty());
	}

	/** Vehiculos por tipo: CARRO 5 (primero), MOTOCICLETA 1, CAMIONETA 1. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void estadisticaPorTipoTest() {
		List<EstadisticaDTO> r = estadistica(Vehiculo.ESTADISTICA_POR_TIPO);
		Assert.assertEquals(3, r.size());
		assertEstadistica("CARRO", 5L, r.get(0));
		Map<String, Long> m = aMapaLong(r);
		Assert.assertEquals(Long.valueOf(1), m.get("MOTOCICLETA"));
		Assert.assertEquals(Long.valueOf(1), m.get("CAMIONETA"));
	}

	/** Por condicion: USADO 5 (2,3,4,6,7) y NUEVO 2 (1,5). */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void estadisticaPorCondicionTest() {
		List<EstadisticaDTO> r = estadistica(Vehiculo.ESTADISTICA_POR_CONDICION);
		Assert.assertEquals(2, r.size());
		assertEstadistica("USADO", 5L, r.get(0));
		assertEstadistica("NUEVO", 2L, r.get(1));
	}

	/** Por transmision: MECANICO 5 y AUTOMATICO 2. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void estadisticaPorTransmisionTest() {
		List<EstadisticaDTO> r = estadistica(Vehiculo.ESTADISTICA_POR_TRANSMISION);
		Assert.assertEquals(2, r.size());
		assertEstadistica("MECANICO", 5L, r.get(0));
		assertEstadistica("AUTOMATICO", 2L, r.get(1));
	}

	/** Por combustible: GASOLINA 6 y DISEL 1. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void estadisticaPorCombustibleTest() {
		List<EstadisticaDTO> r = estadistica(Vehiculo.ESTADISTICA_POR_COMBUSTIBLE);
		Assert.assertEquals(2, r.size());
		assertEstadistica("GASOLINA", 6L, r.get(0));
		assertEstadistica("DISEL", 1L, r.get(1));
	}

	/** Combustible por tipo (con serie): CAMIONETA/DISEL 1, CARRO/GASOLINA 5, MOTOCICLETA/GASOLINA 1. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void estadisticaCombustiblePorTipoTest() {
		List<EstadisticaDTO> r = estadistica(Vehiculo.ESTADISTICA_COMBUSTIBLE_POR_TIPO);
		Assert.assertEquals(3, r.size());
		Map<String, Long> m = new HashMap<>();
		for (EstadisticaDTO e : r) {
			m.put(e.getEtiqueta() + "/" + e.getSerie(), e.getValor().longValue());
		}
		Assert.assertEquals(Long.valueOf(1), m.get("CAMIONETA/DISEL"));
		Assert.assertEquals(Long.valueOf(5), m.get("CARRO/GASOLINA"));
		Assert.assertEquals(Long.valueOf(1), m.get("MOTOCICLETA/GASOLINA"));
	}

	/** Por marca: Mazda 3 (primero), Audi 2, Renoult 2. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void estadisticaPorMarcaTest() {
		List<EstadisticaDTO> r = estadistica(Vehiculo.ESTADISTICA_POR_MARCA);
		Assert.assertEquals(3, r.size());
		assertEstadistica("Mazda", 3L, r.get(0));
		Map<String, Long> m = aMapaLong(r);
		Assert.assertEquals(Long.valueOf(2), m.get("Audi"));
		Assert.assertEquals(Long.valueOf(2), m.get("Renoult"));
	}

	/** Precio promedio por marca: Audi 34.5M, Mazda 23.5M, Renoult 5.25M. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void estadisticaPrecioPromedioPorMarcaTest() {
		List<EstadisticaDTO> r = estadistica(Vehiculo.ESTADISTICA_PRECIO_PROMEDIO_POR_MARCA);
		Assert.assertEquals(3, r.size());
		assertEstadistica("Audi", 34500000d, r.get(0));
		assertEstadistica("Mazda", 23500000d, r.get(1));
		assertEstadistica("Renoult", 5250000d, r.get(2));
	}

	/** Por ciudad: Armenia 4, Buenavista 2, Pijao 1. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void estadisticaPorCiudadTest() {
		List<EstadisticaDTO> r = estadistica(Vehiculo.ESTADISTICA_POR_CIUDAD);
		Assert.assertEquals(3, r.size());
		assertEstadistica("Armenia", 4L, r.get(0));
		assertEstadistica("Buenavista", 2L, r.get(1));
		assertEstadistica("Pijao", 1L, r.get(2));
	}

	/** Por anio, ascendente: 1990, 2006, 2015, 2017, 2018, 2019, 2020 con 1 vehiculo cada uno. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void estadisticaPorAnioTest() {
		List<EstadisticaDTO> r = estadistica(Vehiculo.ESTADISTICA_POR_ANIO);
		String[] anios = { "1990", "2006", "2015", "2017", "2018", "2019", "2020" };
		Assert.assertEquals(anios.length, r.size());
		for (int i = 0; i < anios.length; i++) {
			assertEstadistica(anios[i], 1L, r.get(i));
		}
	}

	/** Top buscados con etiqueta "marca modelo placa" ordenado por busquedas desc. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void estadisticaTopBuscadosTest() {
		List<EstadisticaDTO> r = estadistica(Vehiculo.ESTADISTICA_TOP_BUSCADOS);
		String[] etiquetas = { "Renoult 4 NCV903", "Audi A1 DKR478", "Mazda 3 CMN781", "Mazda 3 VEV592",
				"Mazda 3 BJR464", "Renoult 4 MTR45A", "Audi A1 BSK237" };
		long[] valores = { 120, 95, 70, 57, 33, 21, 8 };
		Assert.assertEquals(etiquetas.length, r.size());
		for (int i = 0; i < etiquetas.length; i++) {
			assertEstadistica(etiquetas[i], valores[i], r.get(i));
		}
	}

	/** Puntos (kilometros, precio) de los 7 vehiculos. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void puntosPrecioKilometrajeTest() {
		List<PuntoDTO> r = entityManager.createNamedQuery(Vehiculo.PUNTOS_PRECIO_KILOMETRAJE, PuntoDTO.class)
				.getResultList();
		Set<String> obtenidos = new HashSet<>();
		for (PuntoDTO p : r) {
			obtenidos.add(p.getX().intValue() + ":" + Math.round(p.getY().doubleValue()));
		}
		Set<String> esperados = new HashSet<>(Arrays.asList("10000:26000000", "100000:6000000", "30000:6500000",
				"10000:5000000", "0:64000000", "15000:4500000", "45000:38000000"));
		Assert.assertEquals(7, r.size());
		Assert.assertEquals(esperados, obtenidos);
	}

	/** Puntos (anio, precio, busquedas como tamano) de los 7 vehiculos. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void puntosPrecioAnioBusquedasTest() {
		List<PuntoDTO> r = entityManager.createNamedQuery(Vehiculo.PUNTOS_PRECIO_ANIO_BUSQUEDAS, PuntoDTO.class)
				.getResultList();
		Set<String> obtenidos = new HashSet<>();
		for (PuntoDTO p : r) {
			obtenidos.add(p.getX().intValue() + ":" + Math.round(p.getY().doubleValue()) + ":"
					+ p.getTamano().intValue());
		}
		Set<String> esperados = new HashSet<>(Arrays.asList("2018:26000000:57", "1990:6000000:120",
				"2006:6500000:33", "2015:5000000:8", "2020:64000000:95", "2019:4500000:21", "2017:38000000:70"));
		Assert.assertEquals(7, r.size());
		Assert.assertEquals(esperados, obtenidos);
	}

	// ---------- Pregunta ----------

	/** El vehiculo 2 tiene las preguntas 3 (09-30) y 1 (09-24). */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void preguntasPorVehiculoTest() {
		List<Pregunta> r = entityManager.createNamedQuery(Pregunta.PREGUNTAS_POR_VEHICULO, Pregunta.class)
				.setParameter("codigo", 2L).getResultList();
		Assert.assertEquals(Arrays.asList(3L, 1L), idsPreguntas(r));
	}

	/** Preguntas recibidas: usuario3 (dueno del 2) recibe 3 y 1; usuario1 (dueno del 1) recibe la 2. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void preguntasRecibidasPorVendedorTest() {
		List<Pregunta> r = entityManager.createNamedQuery(Pregunta.PREGUNTAS_RECIBIDAS_POR_VENDEDOR, Pregunta.class)
				.setParameter("email", "usuario3@servidor.com").getResultList();
		Assert.assertEquals(Arrays.asList(3L, 1L), idsPreguntas(r));

		r = entityManager.createNamedQuery(Pregunta.PREGUNTAS_RECIBIDAS_POR_VENDEDOR, Pregunta.class)
				.setParameter("email", "usuario1@servidor.com").getResultList();
		Assert.assertEquals(Arrays.asList(2L), idsPreguntas(r));
	}

	/** Sin responder: usuario3 solo la 3 (la 1 tiene respuesta); usuario2 la 4. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void preguntasSinResponderPorVendedorTest() {
		List<Pregunta> r = entityManager
				.createNamedQuery(Pregunta.PREGUNTAS_SIN_RESPONDER_POR_VENDEDOR, Pregunta.class)
				.setParameter("email", "usuario3@servidor.com").getResultList();
		Assert.assertEquals(Arrays.asList(3L), idsPreguntas(r));
		Assert.assertNull(r.get(0).getRespuesta());

		r = entityManager.createNamedQuery(Pregunta.PREGUNTAS_SIN_RESPONDER_POR_VENDEDOR, Pregunta.class)
				.setParameter("email", "usuario2@servidor.com").getResultList();
		Assert.assertEquals(Arrays.asList(4L), idsPreguntas(r));
	}

	/** Preguntas hechas: usuario2 hizo 2 (10-11) y 1 (09-24); usuario1 hizo 4 y 3. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void preguntasHechasPorClienteTest() {
		List<Pregunta> r = entityManager.createNamedQuery(Pregunta.PREGUNTAS_HECHAS_POR_CLIENTE, Pregunta.class)
				.setParameter("email", "usuario2@servidor.com").getResultList();
		Assert.assertEquals(Arrays.asList(2L, 1L), idsPreguntas(r));

		r = entityManager.createNamedQuery(Pregunta.PREGUNTAS_HECHAS_POR_CLIENTE, Pregunta.class)
				.setParameter("email", "usuario1@servidor.com").getResultList();
		Assert.assertEquals(Arrays.asList(4L, 3L), idsPreguntas(r));
	}

	/** Preguntas por vehiculo: "Renoult 4 - NCV903" 2 (primero), "Mazda 3 - VEV592" 1, "Mazda 3 - BJR464" 1. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void estadisticaPreguntasPorVehiculoTest() {
		List<EstadisticaDTO> r = estadistica(Pregunta.ESTADISTICA_PREGUNTAS_POR_VEHICULO);
		Assert.assertEquals(3, r.size());
		assertEstadistica("Renoult 4 - NCV903", 2L, r.get(0));
		Map<String, Long> m = aMapaLong(r);
		Assert.assertEquals(Long.valueOf(1), m.get("Mazda 3 - VEV592"));
		Assert.assertEquals(Long.valueOf(1), m.get("Mazda 3 - BJR464"));
	}

	// ---------- Cliente / Usuario ----------

	/** Cliente por email. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void clienteFindByEmailTest() {
		List<Cliente> r = entityManager.createNamedQuery(Cliente.FIND_BY_EMAIL, Cliente.class)
				.setParameter("email", "usuario2@servidor.com").getResultList();
		Assert.assertEquals(1, r.size());
		Assert.assertEquals(Long.valueOf(5L), r.get(0).getCodigo_usuario());
		Assert.assertEquals("Rodolfo el Grande", r.get(0).getNombre_completo());

		r = entityManager.createNamedQuery(Cliente.FIND_BY_EMAIL, Cliente.class)
				.setParameter("email", "cristia@proyecto.com").getResultList();
		Assert.assertTrue("Un administrador no es cliente", r.isEmpty());
	}

	/** Un cliente en cada ciudad: Armenia, Pijao, Buenavista (1 cada una). */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void estadisticaClientesPorCiudadTest() {
		List<EstadisticaDTO> r = estadistica(Cliente.ESTADISTICA_CLIENTES_POR_CIUDAD);
		Assert.assertEquals(3, r.size());
		Map<String, Long> m = aMapaLong(r);
		Assert.assertEquals(Long.valueOf(1), m.get("Armenia"));
		Assert.assertEquals(Long.valueOf(1), m.get("Pijao"));
		Assert.assertEquals(Long.valueOf(1), m.get("Buenavista"));
	}

	/** Autenticacion con credenciales correctas e incorrectas. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void autentificarUsuarioTest() {
		List<Usuario> r = entityManager.createNamedQuery(Usuario.AUTENTIFICAR_USUARIO, Usuario.class)
				.setParameter("email", "usuario1@servidor.com").setParameter("password", "12345").getResultList();
		Assert.assertEquals(1, r.size());
		Assert.assertEquals(Long.valueOf(4L), r.get(0).getCodigo_usuario());

		r = entityManager.createNamedQuery(Usuario.AUTENTIFICAR_USUARIO, Usuario.class)
				.setParameter("email", "usuario1@servidor.com").setParameter("password", "incorrecta")
				.getResultList();
		Assert.assertTrue(r.isEmpty());

		r = entityManager.createNamedQuery(Usuario.AUTENTIFICAR_USUARIO, Usuario.class)
				.setParameter("email", "noexiste@servidor.com").setParameter("password", "12345").getResultList();
		Assert.assertTrue(r.isEmpty());
	}

	/** Usuario por email: un administrador y un email inexistente. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void autentificarEmailTest() {
		List<Usuario> r = entityManager.createNamedQuery(Usuario.AUTENTIFICAR_EMAIL, Usuario.class)
				.setParameter("email", "cristia@proyecto.com").getResultList();
		Assert.assertEquals(1, r.size());
		Assert.assertEquals(Long.valueOf(1L), r.get(0).getCodigo_usuario());
		Assert.assertTrue(r.get(0) instanceof Administrador);

		r = entityManager.createNamedQuery(Usuario.AUTENTIFICAR_EMAIL, Usuario.class)
				.setParameter("email", "noexiste@servidor.com").getResultList();
		Assert.assertTrue(r.isEmpty());
	}

	// ---------- Modelo / Ciudad / Caracteristica ----------

	/** Marcas distintas ordenadas. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void marcasTest() {
		List<String> r = entityManager.createNamedQuery(Modelo.MARCAS, String.class).getResultList();
		Assert.assertEquals(Arrays.asList("Audi", "Mazda", "Renoult"), r);
	}

	/** Modelos de una marca. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void modelosPorMarcaTest() {
		List<Modelo> r = entityManager.createNamedQuery(Modelo.MODELOS_POR_MARCA, Modelo.class)
				.setParameter("marca", "Mazda").getResultList();
		Assert.assertEquals(1, r.size());
		Assert.assertEquals(Long.valueOf(2L), r.get(0).getCodigo_modelo());
		Assert.assertEquals("3", r.get(0).getNombre());

		r = entityManager.createNamedQuery(Modelo.MODELOS_POR_MARCA, Modelo.class).setParameter("marca", "Ferrari")
				.getResultList();
		Assert.assertTrue(r.isEmpty());
	}

	/** Ciudades ordenadas por nombre. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void ciudadesOrdenadasTest() {
		List<Ciudad> r = entityManager.createNamedQuery(Ciudad.CIUDADES_ORDENADAS, Ciudad.class).getResultList();
		List<String> nombres = new ArrayList<>();
		for (Ciudad c : r) {
			nombres.add(c.getNombre());
		}
		Assert.assertEquals(Arrays.asList("Armenia", "Buenavista", "Pijao"), nombres);
	}

	/** Ciudad por nombre. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void ciudadPorNombreTest() {
		List<Ciudad> r = entityManager.createNamedQuery(Ciudad.CIUDAD_POR_NOMBRE, Ciudad.class)
				.setParameter("nombre", "Pijao").getResultList();
		Assert.assertEquals(1, r.size());
		Assert.assertEquals(Long.valueOf(3L), r.get(0).getCodigo_ciudad());

		r = entityManager.createNamedQuery(Ciudad.CIUDAD_POR_NOMBRE, Ciudad.class).setParameter("nombre", "Bogota")
				.getResultList();
		Assert.assertTrue(r.isEmpty());
	}

	/** Caracteristica por nombre. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void caracteristicaPorNombreTest() {
		List<Caracteristica> r = entityManager
				.createNamedQuery(Caracteristica.CARACTERISTICA_POR_NOMBRE, Caracteristica.class)
				.setParameter("nombre", "Luces Led").getResultList();
		Assert.assertEquals(1, r.size());
		Assert.assertEquals(Long.valueOf(2L), r.get(0).getCodigo_caracteristica());

		r = entityManager.createNamedQuery(Caracteristica.CARACTERISTICA_POR_NOMBRE, Caracteristica.class)
				.setParameter("nombre", "Techo Solar").getResultList();
		Assert.assertTrue(r.isEmpty());
	}

	/** Caracteristicas ordenadas por nombre. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void caracteristicasOrdenadasTest() {
		List<Caracteristica> r = entityManager
				.createNamedQuery(Caracteristica.CARACTERISTICAS_ORDENADAS, Caracteristica.class).getResultList();
		List<String> nombres = new ArrayList<>();
		for (Caracteristica c : r) {
			nombres.add(c.getNombre());
		}
		Assert.assertEquals(Arrays.asList("Aire Acondicionado", "Asientos de Cuero", "Luces Led"), nombres);
	}

	/** Frecuencia de caracteristicas: Aire Acondicionado 2, Luces Led 1 (Asientos de Cuero no aparece). */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void estadisticaCaracteristicasFrecuentesTest() {
		List<EstadisticaDTO> r = estadistica(Caracteristica.ESTADISTICA_CARACTERISTICAS_FRECUENTES);
		Assert.assertEquals(2, r.size());
		assertEstadistica("Aire Acondicionado", 2L, r.get(0));
		assertEstadistica("Luces Led", 1L, r.get(1));
	}

	// ---------- Venta / Calificacion ----------

	/** Ventas por mes (aaaa-mm): 2020-08, 2020-09 y 2020-10 con una venta cada una. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void estadisticaVentasPorMesTest() {
		List<EstadisticaDTO> r = estadistica(Venta.ESTADISTICA_VENTAS_POR_MES);
		Assert.assertEquals(3, r.size());
		assertEstadistica("2020-08", 1L, r.get(0));
		assertEstadistica("2020-09", 1L, r.get(1));
		assertEstadistica("2020-10", 1L, r.get(2));
	}

	/** El vendedor 6 tiene las calificaciones 1 y 2; el 4 ninguna. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void calificacionesVendedorTest() {
		List<Calificacion> r = entityManager.createNamedQuery(Calificacion.CALIFICACIONES_VENDEDOR, Calificacion.class)
				.setParameter("codigo", 6L).getResultList();
		Set<Long> ids = new HashSet<>();
		for (Calificacion c : r) {
			ids.add(c.getCodigo_calificacion());
		}
		Assert.assertEquals(2, r.size());
		Assert.assertEquals(new HashSet<>(Arrays.asList(1L, 2L)), ids);

		r = entityManager.createNamedQuery(Calificacion.CALIFICACIONES_VENDEDOR, Calificacion.class)
				.setParameter("codigo", 4L).getResultList();
		Assert.assertTrue(r.isEmpty());
	}

	/** Promedio de estrellas: vendedor 6 = 4.5, vendedor 5 = 3.0, vendedor 4 sin calificaciones = null. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void promedioVendedorTest() {
		Double p6 = entityManager.createNamedQuery(Calificacion.PROMEDIO_VENDEDOR, Double.class)
				.setParameter("codigo", 6L).getSingleResult();
		Assert.assertEquals(4.5, p6, DELTA);

		Double p5 = entityManager.createNamedQuery(Calificacion.PROMEDIO_VENDEDOR, Double.class)
				.setParameter("codigo", 5L).getSingleResult();
		Assert.assertEquals(3.0, p5, DELTA);

		Double p4 = entityManager.createNamedQuery(Calificacion.PROMEDIO_VENDEDOR, Double.class)
				.setParameter("codigo", 4L).getSingleResult();
		Assert.assertNull(p4);
	}

	/** Promedio por vendedor, descendente: Ruben zuluaga 4.5 y Rodolfo el Grande 3.0. */
	@Test
	@Transactional(value = TransactionMode.ROLLBACK)
	public void estadisticaPromedioPorVendedorTest() {
		List<EstadisticaDTO> r = estadistica(Calificacion.ESTADISTICA_PROMEDIO_POR_VENDEDOR);
		Assert.assertEquals(2, r.size());
		assertEstadistica("Ruben zuluaga", 4.5, r.get(0));
		assertEstadistica("Rodolfo el Grande", 3.0, r.get(1));
	}
}
