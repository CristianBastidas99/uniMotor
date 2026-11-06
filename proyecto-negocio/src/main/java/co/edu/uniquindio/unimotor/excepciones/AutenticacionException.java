package co.edu.uniquindio.unimotor.excepciones;

/**
 * Se lanza cuando las credenciales de inicio de sesión son incorrectas.
 */
public class AutenticacionException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Crea la excepción con el mensaje indicado.
	 *
	 * @param mensaje descripción del error que se mostrará al usuario
	 */
	public AutenticacionException(String mensaje) {
		super(mensaje);
	}

}
