package co.edu.uniquindio.unimotor.web.rest.dto;

import java.util.List;

/** Detalle completo de un vehículo (sin datos sensibles). */
public class VehiculoDetalleDTO extends VehiculoResumenDTO {

	private String descripcion;
	private String color;
	private int cilindraje;
	private int numeroPuertas;
	private String placaTerminacion;
	private List<String> fotos;
	private List<String> caracteristicas;
	private VendedorDTO vendedor;
	private int numeroPreguntas;

	/** Constructor vacío requerido por JSON-B. */
	public VehiculoDetalleDTO() {
	}

	/** @return valor de {@code descripcion} */
	public String getDescripcion() {
		return descripcion;
	}

	/** @param descripcion nuevo valor de {@code descripcion} */
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	/** @return valor de {@code color} */
	public String getColor() {
		return color;
	}

	/** @param color nuevo valor de {@code color} */
	public void setColor(String color) {
		this.color = color;
	}

	/** @return valor de {@code cilindraje} */
	public int getCilindraje() {
		return cilindraje;
	}

	/** @param cilindraje nuevo valor de {@code cilindraje} */
	public void setCilindraje(int cilindraje) {
		this.cilindraje = cilindraje;
	}

	/** @return valor de {@code numeroPuertas} */
	public int getNumeroPuertas() {
		return numeroPuertas;
	}

	/** @param numeroPuertas nuevo valor de {@code numeroPuertas} */
	public void setNumeroPuertas(int numeroPuertas) {
		this.numeroPuertas = numeroPuertas;
	}

	/** @return valor de {@code placaTerminacion} */
	public String getPlacaTerminacion() {
		return placaTerminacion;
	}

	/** @param placaTerminacion nuevo valor de {@code placaTerminacion} */
	public void setPlacaTerminacion(String placaTerminacion) {
		this.placaTerminacion = placaTerminacion;
	}

	/** @return valor de {@code fotos} */
	public List<String> getFotos() {
		return fotos;
	}

	/** @param fotos nuevo valor de {@code fotos} */
	public void setFotos(List<String> fotos) {
		this.fotos = fotos;
	}

	/** @return valor de {@code caracteristicas} */
	public List<String> getCaracteristicas() {
		return caracteristicas;
	}

	/** @param caracteristicas nuevo valor de {@code caracteristicas} */
	public void setCaracteristicas(List<String> caracteristicas) {
		this.caracteristicas = caracteristicas;
	}

	/** @return valor de {@code vendedor} */
	public VendedorDTO getVendedor() {
		return vendedor;
	}

	/** @param vendedor nuevo valor de {@code vendedor} */
	public void setVendedor(VendedorDTO vendedor) {
		this.vendedor = vendedor;
	}

	/** @return valor de {@code numeroPreguntas} */
	public int getNumeroPreguntas() {
		return numeroPreguntas;
	}

	/** @param numeroPreguntas nuevo valor de {@code numeroPreguntas} */
	public void setNumeroPreguntas(int numeroPreguntas) {
		this.numeroPreguntas = numeroPreguntas;
	}
}
