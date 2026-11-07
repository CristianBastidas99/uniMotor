package co.edu.uniquindio.unimotor.web.rest.dto;

/** Cuerpo JSON de las respuestas de error. */
public class ErrorDTO {

	private int estado;
	private String error;
	private String mensaje;

	/** Constructor vacío requerido por JSON-B. */
	public ErrorDTO() {
	}

	/**
	 * Crea un error con todos sus datos.
	 *
	 * @param estado código HTTP
	 * @param error texto breve del error
	 * @param mensaje detalle
	 */
	public ErrorDTO(int estado, String error, String mensaje) {
		this.estado = estado;
		this.error = error;
		this.mensaje = mensaje;
	}

	/** @return valor de {@code estado} */
	public int getEstado() {
		return estado;
	}

	/** @param estado nuevo valor de {@code estado} */
	public void setEstado(int estado) {
		this.estado = estado;
	}

	/** @return valor de {@code error} */
	public String getError() {
		return error;
	}

	/** @param error nuevo valor de {@code error} */
	public void setError(String error) {
		this.error = error;
	}

	/** @return valor de {@code mensaje} */
	public String getMensaje() {
		return mensaje;
	}

	/** @param mensaje nuevo valor de {@code mensaje} */
	public void setMensaje(String mensaje) {
		this.mensaje = mensaje;
	}
}
