package co.edu.uniquindio.unimotor.web.beans;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

import co.edu.uniquindio.unimotor.ejb.CatalogoEJB;
import co.edu.uniquindio.unimotor.entidades.TipoVehiculo;
import co.edu.uniquindio.unimotor.web.util.Mensajes;

/**
 * Criterios del buscador de vehículos, reutilizado en la página principal y en los resultados.
 */
@Named
@RequestScoped
public class BuscadorBean {

	@EJB
	private CatalogoEJB catalogoEJB;

	private TipoVehiculo tipo;
	private String marca;
	private Double precioMin;
	private Double precioMax;
	private Integer anioMin;
	private Integer anioMax;

	private List<String> marcas;

	/** @return tipo de vehículo elegido o {@code null} */
	public TipoVehiculo getTipo() { return tipo; }

	/** @param tipo tipo de vehículo */
	public void setTipo(TipoVehiculo tipo) { this.tipo = tipo; }

	/** @return marca elegida o {@code null} */
	public String getMarca() { return marca; }

	/** @param marca marca (la cadena vacía se trata como ninguna) */
	public void setMarca(String marca) { this.marca = (marca == null || marca.trim().isEmpty()) ? null : marca; }

	/** @return precio mínimo o {@code null} */
	public Double getPrecioMin() { return precioMin; }

	/** @param precioMin precio mínimo */
	public void setPrecioMin(Double precioMin) { this.precioMin = precioMin; }

	/** @return precio máximo o {@code null} */
	public Double getPrecioMax() { return precioMax; }

	/** @param precioMax precio máximo */
	public void setPrecioMax(Double precioMax) { this.precioMax = precioMax; }

	/** @return año mínimo o {@code null} */
	public Integer getAnioMin() { return anioMin; }

	/** @param anioMin año mínimo */
	public void setAnioMin(Integer anioMin) { this.anioMin = anioMin; }

	/** @return año máximo o {@code null} */
	public Integer getAnioMax() { return anioMax; }

	/** @param anioMax año máximo */
	public void setAnioMax(Integer anioMax) { this.anioMax = anioMax; }

	/**
	 * Tipos de vehículo ofrecidos en el buscador (los que existen en los datos).
	 *
	 * @return carro, motocicleta y camioneta
	 */
	public List<TipoVehiculo> getTipos() {
		return Arrays.asList(TipoVehiculo.CARRO, TipoVehiculo.MOTOCICLETA, TipoVehiculo.CAMIONETA);
	}

	/**
	 * Marcas disponibles.
	 *
	 * @return lista de marcas del catálogo (vacía si falla la consulta)
	 */
	public List<String> getMarcas() {
		if (marcas == null) {
			try {
				marcas = new ArrayList<>(catalogoEJB.listarMarcas());
			} catch (RuntimeException e) {
				marcas = Collections.emptyList();
			}
		}
		return marcas;
	}

	/**
	 * Indica si no hay ningún criterio.
	 *
	 * @return {@code true} si todos los criterios son nulos
	 */
	public boolean isVacio() {
		return tipo == null && marca == null && precioMin == null && precioMax == null
				&& anioMin == null && anioMax == null;
	}

	/**
	 * Valida los rangos y redirige a la página de resultados con los criterios como parámetros.
	 *
	 * @return la regla de navegación, o {@code null} si hay rangos inválidos
	 */
	public String buscar() {
		if (precioMin != null && precioMax != null && precioMin > precioMax) {
			Mensajes.error("Rango de precio inválido", "El precio mínimo no puede ser mayor que el máximo.");
			return null;
		}
		if (anioMin != null && anioMax != null && anioMin > anioMax) {
			Mensajes.error("Rango de año inválido", "El año mínimo no puede ser mayor que el máximo.");
			return null;
		}
		StringBuilder url = new StringBuilder("/resultados?faces-redirect=true");
		if (tipo != null) {
			url.append("&tipo=").append(tipo.name());
		}
		if (marca != null) {
			url.append("&marca=").append(codificar(marca));
		}
		if (precioMin != null) {
			url.append("&precioMin=").append(precioMin.longValue());
		}
		if (precioMax != null) {
			url.append("&precioMax=").append(precioMax.longValue());
		}
		if (anioMin != null) {
			url.append("&anioMin=").append(anioMin);
		}
		if (anioMax != null) {
			url.append("&anioMax=").append(anioMax);
		}
		return url.toString();
	}

	private static String codificar(String texto) {
		try {
			return URLEncoder.encode(texto, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e);
		}
	}
}
