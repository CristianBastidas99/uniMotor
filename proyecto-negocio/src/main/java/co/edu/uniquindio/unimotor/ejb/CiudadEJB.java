package co.edu.uniquindio.unimotor.ejb;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import co.edu.uniquindio.unimotor.entidades.Ciudad;
import co.edu.uniquindio.unimotor.excepciones.ElementoNoEncontradoException;

/**
 * EJB que administra el catálogo de ciudades de Colombia, obtenido de api-colombia.com con un
 * archivo de respaldo incluido en la aplicación.
 */
@Stateless
@LocalBean
public class CiudadEJB {

	/** URL base de la API de Colombia. */
	public static final String URL_API = "https://api-colombia.com/api/v1/";
	/** Recurso de respaldo con las ciudades en formato JSON. */
	public static final String RESPALDO = "/ciudades-colombia.json";

	private static final Logger LOGGER = Logger.getLogger(CiudadEJB.class.getName());
	private static final int TIMEOUT_MS = 10000;

	@PersistenceContext
	private EntityManager entityManager;

	/**
	 * Crea el EJB de ciudades.
	 */
	public CiudadEJB() {
		super();
	}

	/**
	 * Consulta las ciudades de Colombia en la API y, si falla, en el archivo de respaldo.
	 *
	 * @return ciudades (sin persistir) sin pares nombre-departamento repetidos
	 */
	public List<Ciudad> consultarCiudadesColombia() {
		List<Ciudad> ciudades;
		try {
			ciudades = consultarApi();
			if (ciudades.isEmpty()) {
				throw new IOException("La API devolvió una lista vacía.");
			}
			LOGGER.info("Ciudades obtenidas de la API (" + URL_API + "): " + ciudades.size());
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "No se pudo consultar la API de ciudades, se usará el respaldo: " + e);
			try {
				ciudades = leerRespaldo();
				LOGGER.info("Ciudades obtenidas del respaldo (" + RESPALDO + "): " + ciudades.size());
			} catch (Exception e2) {
				LOGGER.log(Level.WARNING, "No se pudo leer el respaldo de ciudades: " + e2);
				ciudades = new ArrayList<>();
			}
		}
		return ciudades;
	}

	/**
	 * Carga las ciudades en la base de datos solo si la tabla está vacía.
	 *
	 * @return cantidad de ciudades insertadas ({@code 0} si ya existían)
	 */
	public int cargarCiudades() {
		Long existentes = entityManager.createQuery("select count(c) from Ciudad c", Long.class).getSingleResult();
		if (existentes != null && existentes > 0) {
			return 0;
		}
		List<Ciudad> ciudades = consultarCiudadesColombia();
		for (Ciudad ciudad : ciudades) {
			entityManager.persist(ciudad);
		}
		return ciudades.size();
	}

	/**
	 * Lista todas las ciudades ordenadas por nombre.
	 *
	 * @return lista de ciudades
	 */
	public List<Ciudad> listarCiudades() {
		return entityManager.createNamedQuery(Ciudad.CIUDADES_ORDENADAS, Ciudad.class).getResultList();
	}

	/**
	 * Busca una ciudad por nombre y departamento.
	 *
	 * @param nombre nombre de la ciudad
	 * @param departamento nombre del departamento
	 * @return la ciudad encontrada
	 * @throws ElementoNoEncontradoException si no existe la ciudad en ese departamento
	 */
	public Ciudad buscarCiudad(String nombre, String departamento) throws ElementoNoEncontradoException {
		TypedQuery<Ciudad> query = entityManager.createNamedQuery(Ciudad.CIUDAD_POR_NOMBRE_Y_DEPARTAMENTO,
				Ciudad.class);
		query.setParameter("nombre", nombre);
		query.setParameter("departamento", departamento);
		List<Ciudad> resultado = query.getResultList();
		if (resultado.isEmpty()) {
			throw new ElementoNoEncontradoException(
					"No existe la ciudad " + nombre + " en el departamento " + departamento + ".");
		}
		return resultado.get(0);
	}

	private List<Ciudad> consultarApi() throws IOException {
		JsonArray departamentos = leerArreglo(URL_API + "Department");
		Map<Integer, String> nombresDepartamentos = new HashMap<>();
		for (JsonValue v : departamentos) {
			JsonObject d = (JsonObject) v;
			nombresDepartamentos.put(d.getInt("id"), d.getString("name").trim());
		}
		JsonArray datos = leerArreglo(URL_API + "City");
		List<Ciudad> ciudades = new ArrayList<>();
		Set<String> vistos = new HashSet<>();
		for (JsonValue v : datos) {
			JsonObject c = (JsonObject) v;
			String nombre = c.getString("name").trim();
			String departamento = nombresDepartamentos.get(c.getInt("departmentId"));
			if (departamento == null || nombre.isEmpty()) {
				continue;
			}
			if (vistos.add(nombre + "\u0000" + departamento)) {
				ciudades.add(new Ciudad(nombre, departamento));
			}
		}
		return ciudades;
	}

	private JsonArray leerArreglo(String direccion) throws IOException {
		HttpURLConnection conexion = (HttpURLConnection) new URL(direccion).openConnection();
		try {
			conexion.setConnectTimeout(TIMEOUT_MS);
			conexion.setReadTimeout(TIMEOUT_MS);
			conexion.setRequestProperty("Accept", "application/json");
			int codigo = conexion.getResponseCode();
			if (codigo != HttpURLConnection.HTTP_OK) {
				throw new IOException("HTTP " + codigo + " al consultar " + direccion);
			}
			try (InputStream in = conexion.getInputStream();
					JsonReader lector = Json.createReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
				return lector.readArray();
			}
		} finally {
			conexion.disconnect();
		}
	}

	private List<Ciudad> leerRespaldo() throws IOException {
		try (InputStream in = getClass().getResourceAsStream(RESPALDO)) {
			if (in == null) {
				throw new IOException("No se encontró el recurso " + RESPALDO);
			}
			try (JsonReader lector = Json.createReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
				JsonArray datos = lector.readArray();
				List<Ciudad> ciudades = new ArrayList<>();
				Set<String> vistos = new HashSet<>();
				for (JsonValue v : datos) {
					JsonObject c = (JsonObject) v;
					String nombre = c.getString("nombre").trim();
					String departamento = c.getString("departamento").trim();
					if (vistos.add(nombre + "\u0000" + departamento)) {
						ciudades.add(new Ciudad(nombre, departamento));
					}
				}
				return ciudades;
			}
		}
	}

}
