package co.edu.uniquindio.unimotor.web.rest.dto;

/** Datos públicos mínimos del vendedor (sin contacto). */
public class VendedorDTO {

	private String nombre;
	private String ciudad;

	/** Constructor vacío requerido por JSON-B. */
	public VendedorDTO() {
	}

	/** @return valor de {@code nombre} */
	public String getNombre() {
		return nombre;
	}

	/** @param nombre nuevo valor de {@code nombre} */
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	/** @return valor de {@code ciudad} */
	public String getCiudad() {
		return ciudad;
	}

	/** @param ciudad nuevo valor de {@code ciudad} */
	public void setCiudad(String ciudad) {
		this.ciudad = ciudad;
	}
}
