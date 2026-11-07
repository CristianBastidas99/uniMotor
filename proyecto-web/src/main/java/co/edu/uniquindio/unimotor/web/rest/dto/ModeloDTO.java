package co.edu.uniquindio.unimotor.web.rest.dto;

/** Modelo de vehículo con su marca. */
public class ModeloDTO {

	private Long id;
	private String marca;
	private String nombre;

	/** Constructor vacío requerido por JSON-B. */
	public ModeloDTO() {
	}

	/** @return valor de {@code id} */
	public Long getId() {
		return id;
	}

	/** @param id nuevo valor de {@code id} */
	public void setId(Long id) {
		this.id = id;
	}

	/** @return valor de {@code marca} */
	public String getMarca() {
		return marca;
	}

	/** @param marca nuevo valor de {@code marca} */
	public void setMarca(String marca) {
		this.marca = marca;
	}

	/** @return valor de {@code nombre} */
	public String getNombre() {
		return nombre;
	}

	/** @param nombre nuevo valor de {@code nombre} */
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
}
