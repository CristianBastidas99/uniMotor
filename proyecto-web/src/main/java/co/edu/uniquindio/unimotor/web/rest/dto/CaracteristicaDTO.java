package co.edu.uniquindio.unimotor.web.rest.dto;

/** Característica de un vehículo. */
public class CaracteristicaDTO {

	private Long id;
	private String nombre;

	/** Constructor vacío requerido por JSON-B. */
	public CaracteristicaDTO() {
	}

	/** @return valor de {@code id} */
	public Long getId() {
		return id;
	}

	/** @param id nuevo valor de {@code id} */
	public void setId(Long id) {
		this.id = id;
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
