package co.edu.uniquindio.unimotor.web.rest;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import co.edu.uniquindio.unimotor.ejb.CatalogoEJB;
import co.edu.uniquindio.unimotor.entidades.Ciudad;
import co.edu.uniquindio.unimotor.entidades.Modelo;
import co.edu.uniquindio.unimotor.web.rest.dto.CaracteristicaDTO;
import co.edu.uniquindio.unimotor.web.rest.dto.CiudadDTO;
import co.edu.uniquindio.unimotor.web.rest.dto.ModeloDTO;

/**
 * Recurso REST de solo lectura con los catálogos: ciudades, características, modelos y marcas.
 */
@RequestScoped
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class CatalogoRest {

	@EJB
	private CatalogoEJB catalogoEJB;

	/**
	 * Crea el recurso.
	 */
	public CatalogoRest() {
		super();
	}

	/**
	 * Lista las ciudades, opcionalmente las que contienen un texto en el nombre.
	 *
	 * @param nombre texto a buscar (sin distinguir mayúsculas ni tildes); vacío no filtra
	 * @return ciudades ordenadas por nombre
	 */
	@GET
	@Path("ciudades")
	public List<CiudadDTO> ciudades(@QueryParam("nombre") String nombre) {
		List<Ciudad> ciudades = catalogoEJB.listarCiudades();
		String buscado = normalizar(nombre);
		if (!buscado.isEmpty()) {
			List<Ciudad> filtradas = new ArrayList<Ciudad>();
			for (Ciudad c : ciudades) {
				if (normalizar(c.getNombre()).contains(buscado)) {
					filtradas.add(c);
				}
			}
			ciudades = filtradas;
		}
		return ConversorDTO.ciudades(ciudades);
	}

	/**
	 * Lista las características de los vehículos.
	 *
	 * @return características ordenadas por nombre
	 */
	@GET
	@Path("caracteristicas")
	public List<CaracteristicaDTO> caracteristicas() {
		return ConversorDTO.caracteristicas(catalogoEJB.listarCaracteristicas());
	}

	/**
	 * Lista los modelos, opcionalmente solo los de una marca.
	 *
	 * @param marca marca a filtrar (sin distinguir mayúsculas ni tildes); vacía no filtra
	 * @return modelos ordenados por marca y nombre
	 */
	@GET
	@Path("modelos")
	public List<ModeloDTO> modelos(@QueryParam("marca") String marca) {
		List<Modelo> modelos = catalogoEJB.listarModelos();
		String buscada = normalizar(marca);
		if (!buscada.isEmpty()) {
			List<Modelo> filtrados = new ArrayList<Modelo>();
			for (Modelo m : modelos) {
				if (normalizar(m.getMarca()).equals(buscada)) {
					filtrados.add(m);
				}
			}
			modelos = filtrados;
		}
		return ConversorDTO.modelos(modelos);
	}

	/**
	 * Lista las marcas distintas.
	 *
	 * @return marcas en orden alfabético
	 */
	@GET
	@Path("marcas")
	public List<String> marcas() {
		return catalogoEJB.listarMarcas();
	}

	private static String normalizar(String texto) {
		if (texto == null) {
			return "";
		}
		String sinTildes = Normalizer.normalize(texto.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
		return sinTildes.toLowerCase(Locale.ROOT);
	}
}
