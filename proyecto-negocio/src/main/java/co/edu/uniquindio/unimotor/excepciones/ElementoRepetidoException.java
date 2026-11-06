package co.edu.uniquindio.unimotor.excepciones;

/**
 * Se lanza cuando se intenta registrar un elemento que ya existe (por ejemplo, un email o una placa duplicados).
 */
public class ElementoRepetidoException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Crea la excepción con el mensaje indicado.
	 *
	 * @param mensaje descripción del error que se mostrará al usuario
	 */
	public ElementoRepetidoException(String mensaje) {
		super(mensaje);
	}

}
