package co.edu.uniquindio.unimotor.web.beans;

import java.io.Serializable;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.ejb.EJB;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import co.edu.uniquindio.unimotor.ejb.CatalogoEJB;
import co.edu.uniquindio.unimotor.entidades.Ciudad;

/**
 * Caché en memoria de las ciudades (municipios) para los formularios. Ofrece búsqueda sin distinguir
 * mayúsculas ni tildes. Las entidades guardadas están desacopladas del {@code EntityManager}: solo
 * sirven para mostrar y para obtener el identificador.
 */
@Named
@ApplicationScoped
public class CiudadesBean implements Serializable {

	private static final long serialVersionUID = 1L;

	/** Máximo de sugerencias devueltas por búsqueda. */
	private static final int MAXIMO_RESULTADOS = 20;

	@EJB
	private transient CatalogoEJB catalogoEJB;

	/** Ciudades ordenadas por nombre y departamento; se carga la primera vez que se necesita. */
	private volatile List<Ciudad> ciudades;

	/** Índice por código para búsquedas directas. */
	private volatile Map<Long, Ciudad> porCodigo;

	/** Textos normalizados (nombre, departamento) de cada ciudad, en el mismo orden que la lista. */
	private volatile List<String[]> normalizadas;

	/**
	 * Carga la lista de ciudades la primera vez que se llama.
	 */
	private void cargar() {
		if (ciudades != null) {
			return;
		}
		synchronized (this) {
			if (ciudades != null) {
				return;
			}
			List<Ciudad> lista = new ArrayList<>(catalogoEJB.listarCiudades());
			lista.sort(Comparator
					.comparing((Ciudad c) -> normalizar(c.getNombre()))
					.thenComparing(c -> normalizar(c.getDepartamento())));
			Map<Long, Ciudad> indice = new HashMap<>();
			List<String[]> textos = new ArrayList<>();
			for (Ciudad c : lista) {
				indice.put(c.getCodigo_ciudad(), c);
				textos.add(new String[] { normalizar(c.getNombre()), normalizar(c.getDepartamento()) });
			}
			porCodigo = indice;
			normalizadas = textos;
			ciudades = lista;
		}
	}

	/**
	 * Quita tildes y pasa a minúsculas.
	 *
	 * @param texto texto original (puede ser {@code null})
	 * @return texto normalizado, nunca {@code null}
	 */
	private static String normalizar(String texto) {
		if (texto == null) {
			return "";
		}
		String sinTildes = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
		return sinTildes.toLowerCase(Locale.ROOT).trim();
	}

	/**
	 * Sugiere ciudades para el autocompletado, priorizando las que empiezan por la consulta.
	 *
	 * @param consulta texto escrito por el usuario
	 * @return hasta 20 ciudades que coinciden por nombre o departamento
	 */
	public List<Ciudad> completar(String consulta) {
		cargar();
		String q = normalizar(consulta);
		List<Ciudad> resultado = new ArrayList<>();
		if (q.isEmpty()) {
			return resultado;
		}
		List<Ciudad> contienen = new ArrayList<>();
		for (int i = 0; i < ciudades.size(); i++) {
			String[] t = normalizadas.get(i);
			if (t[0].startsWith(q)) {
				resultado.add(ciudades.get(i));
				if (resultado.size() >= MAXIMO_RESULTADOS) {
					return resultado;
				}
			} else if (t[0].contains(q) || t[1].contains(q)) {
				contienen.add(ciudades.get(i));
			}
		}
		for (Ciudad c : contienen) {
			if (resultado.size() >= MAXIMO_RESULTADOS) {
				break;
			}
			resultado.add(c);
		}
		return resultado;
	}

	/**
	 * Busca una ciudad en la caché.
	 *
	 * @param codigo identificador de la ciudad
	 * @return la ciudad o {@code null} si no existe
	 */
	public Ciudad buscarPorCodigo(Long codigo) {
		cargar();
		return codigo == null ? null : porCodigo.get(codigo);
	}

	/**
	 * Texto para mostrar una ciudad.
	 *
	 * @param c la ciudad (puede ser {@code null})
	 * @return "Nombre (Departamento)", solo el nombre si no hay departamento, o cadena vacía si es nula
	 */
	public String etiqueta(Ciudad c) {
		if (c == null) {
			return "";
		}
		if (c.getDepartamento() == null) {
			return c.getNombre();
		}
		return c.getNombre() + " (" + c.getDepartamento() + ")";
	}
}
