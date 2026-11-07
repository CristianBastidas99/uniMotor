package co.edu.uniquindio.unimotor.escritorio.modelo;

/**
 * Indica que no fue posible comunicarse con el servidor de aplicaciones.
 */
public class ErrorConexionException extends RuntimeException {

	/** Identificador de serialización. */
	private static final long serialVersionUID = 1L;

	/**
	 * Crea la excepción con un mensaje y la causa original.
	 *
	 * @param mensaje mensaje descriptivo para el usuario
	 * @param causa excepción original que provocó el fallo de conexión
	 */
	public ErrorConexionException(String mensaje, Throwable causa) {
		super(mensaje, causa);
	}

}
