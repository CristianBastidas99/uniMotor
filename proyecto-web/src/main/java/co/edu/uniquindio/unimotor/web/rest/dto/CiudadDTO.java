package co.edu.uniquindio.unimotor.web.rest.dto;

/** Ciudad del catálogo. */
public class CiudadDTO {

	private Long id;
	private String nombre;
	private String departamento;

	/** Constructor vacío requerido por JSON-B. */
	public CiudadDTO() {
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

	/** @return valor de {@code departamento} */
	public String getDepartamento() {
		return departamento;
	}

	/** @param departamento nuevo valor de {@code departamento} */
	public void setDepartamento(String departamento) {
		this.departamento = departamento;
	}
}
