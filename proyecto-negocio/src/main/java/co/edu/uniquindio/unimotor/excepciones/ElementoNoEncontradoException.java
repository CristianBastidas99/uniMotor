package co.edu.uniquindio.unimotor.excepciones;

/**
 * Se lanza cuando el elemento buscado no existe en la base de datos.
 */
public class ElementoNoEncontradoException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Crea la excepción con el mensaje indicado.
	 *
	 * @param mensaje descripción del error que se mostrará al usuario
	 */
	public ElementoNoEncontradoException(String mensaje) {
		super(mensaje);
	}

}
