package co.edu.uniquindio.unimotor.excepciones;

/**
 * Se lanza cuando los datos recibidos son nulos, están vacíos o no cumplen las reglas de negocio.
 */
public class InformacionInvalidaException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Crea la excepción con el mensaje indicado.
	 *
	 * @param mensaje descripción del error que se mostrará al usuario
	 */
	public InformacionInvalidaException(String mensaje) {
		super(mensaje);
	}

}
