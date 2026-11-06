package co.edu.uniquindio.unimotor.dto;

import java.io.Serializable;

/**
 * Punto (x, y) o burbuja (x, y, tamaño) para los gráficos de dispersión y de
 * burbujas del administrador. Se usa desde JPQL con una expresión constructora, p. ej.
 * {@code select new co.edu.uniquindio.unimotor.dto.PuntoDTO(v.kilometros, v.precio) from Vehiculo v}
 */
public class PuntoDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private Number x;
	private Number y;
	private Number tamano;

	public PuntoDTO() {
	}

	public PuntoDTO(Number x, Number y) {
		this(x, y, null);
	}

	public PuntoDTO(Number x, Number y, Number tamano) {
		this.x = x;
		this.y = y;
		this.tamano = tamano;
	}

	public Number getX() {
		return x;
	}

	public void setX(Number x) {
		this.x = x;
	}

	public Number getY() {
		return y;
	}

	public void setY(Number y) {
		this.y = y;
	}

	public Number getTamano() {
		return tamano;
	}

	public void setTamano(Number tamano) {
		this.tamano = tamano;
	}

	@Override
	public String toString() {
		return "PuntoDTO [x=" + x + ", y=" + y + (tamano == null ? "" : ", tamano=" + tamano) + "]";
	}
}
