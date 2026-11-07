package co.edu.uniquindio.unimotor.ejb.demo;

import java.io.Serializable;

import co.edu.uniquindio.unimotor.entidades.TipoVehiculo;

/**
 * Foto de demostración alojada en Wikimedia Commons, con sus datos de crédito.
 * Es inmutable.
 */
public final class FotoDemo implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String marca;
	private final String modelo;
	private final TipoVehiculo tipo;
	private final String url;
	private final String autor;
	private final String licencia;
	private final String pagina;

	/**
	 * Crea una foto de demostración.
	 *
	 * @param marca    marca del vehículo fotografiado
	 * @param modelo   modelo del vehículo fotografiado
	 * @param tipo     tipo de vehículo
	 * @param url      enlace absoluto a la imagen
	 * @param autor    autor de la foto
	 * @param licencia licencia de uso (por ejemplo, CC BY-SA 4.0)
	 * @param pagina   página de la foto en Wikimedia Commons
	 */
	public FotoDemo(String marca, String modelo, TipoVehiculo tipo, String url, String autor, String licencia,
			String pagina) {
		this.marca = marca;
		this.modelo = modelo;
		this.tipo = tipo;
		this.url = url;
		this.autor = autor;
		this.licencia = licencia;
		this.pagina = pagina;
	}

	/** @return marca del vehículo */
	public String getMarca() {
		return marca;
	}

	/** @return modelo del vehículo */
	public String getModelo() {
		return modelo;
	}

	/** @return tipo de vehículo */
	public TipoVehiculo getTipo() {
		return tipo;
	}

	/** @return enlace absoluto a la imagen */
	public String getUrl() {
		return url;
	}

	/** @return autor de la foto */
	public String getAutor() {
		return autor;
	}

	/** @return licencia de uso */
	public String getLicencia() {
		return licencia;
	}

	/** @return página de la foto en Wikimedia Commons */
	public String getPagina() {
		return pagina;
	}
}
