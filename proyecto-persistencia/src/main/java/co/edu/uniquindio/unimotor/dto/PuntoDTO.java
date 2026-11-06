package co.edu.uniquindio.unimotor.dto;

import java.io.Serializable;

/**
 * Punto (x, y) o burbuja (x, y, tamaño) para los gráficos de dispersión y de
 * burbujas del administrador. Se usa desde JPQL con una expresión constructora, p. ej.
 * {@code select new co.edu.uniquindio.unimotor.dto.PuntoDTO(v.kilometros, v.precio) from Vehiculo v}
 */
public class PuntoDTO implements Serializable {

	/**
	 * Versión de serialización.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Coordenada en el eje X.
	 */
	private Number x;
	/**
	 * Coordenada en el eje Y.
	 */
	private Number y;
	/**
	 * Tamaño de la burbuja; {@code null} en los gráficos de dispersión simples.
	 */
	private Number tamano;

	/**
	 * Constructor vacío requerido para el uso como JavaBean.
	 */
	public PuntoDTO() {
	}

	/**
	 * Punto simple (sin tamaño) para gráficos de dispersión.
	 *
	 * @param x coordenada X
	 * @param y coordenada Y
	 */
	public PuntoDTO(Number x, Number y) {
		this(x, y, null);
	}

	/**
	 * Burbuja (x, y, tamaño).
	 *
	 * @param x coordenada X
	 * @param y coordenada Y
	 * @param tamano tamaño de la burbuja
	 */
	public PuntoDTO(Number x, Number y, Number tamano) {
		this.x = x;
		this.y = y;
		this.tamano = tamano;
	}

	/**
	 * Obtiene el valor de {@code X}.
	 *
	 * @return valor de {@code X}
	 */
	public Number getX() {
		return x;
	}

	/**
	 * Establece el valor de {@code X}.
	 *
	 * @param x nuevo valor
	 */
	public void setX(Number x) {
		this.x = x;
	}

	/**
	 * Obtiene el valor de {@code Y}.
	 *
	 * @return valor de {@code Y}
	 */
	public Number getY() {
		return y;
	}

	/**
	 * Establece el valor de {@code Y}.
	 *
	 * @param y nuevo valor
	 */
	public void setY(Number y) {
		this.y = y;
	}

	/**
	 * Obtiene el valor de {@code Tamano}.
	 *
	 * @return valor de {@code Tamano}
	 */
	public Number getTamano() {
		return tamano;
	}

	/**
	 * Establece el valor de {@code Tamano}.
	 *
	 * @param tamano nuevo valor
	 */
	public void setTamano(Number tamano) {
		this.tamano = tamano;
	}

	/**
	 * Representación en texto del punto.
	 *
	 * @return texto descriptivo del objeto
	 */
	@Override
	public String toString() {
		return "PuntoDTO [x=" + x + ", y=" + y + (tamano == null ? "" : ", tamano=" + tamano) + "]";
	}
}
