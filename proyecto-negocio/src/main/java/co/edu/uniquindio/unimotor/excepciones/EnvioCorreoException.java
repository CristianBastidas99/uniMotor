package co.edu.uniquindio.unimotor.excepciones;

import javax.ejb.ApplicationException;

/**
 * Se lanza cuando no es posible enviar un correo electrónico. Al ser una excepción de aplicación con
 * {@code rollback = true}, deshace la transacción en curso (por ejemplo, el cambio de contraseña).
 */
@ApplicationException(rollback = true)
public class EnvioCorreoException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Crea la excepción con el mensaje indicado.
	 *
	 * @param mensaje descripción del error
	 */
	public EnvioCorreoException(String mensaje) {
		super(mensaje);
	}

	/**
	 * Crea la excepción con el mensaje y la causa indicados.
	 *
	 * @param mensaje descripción del error
	 * @param causa excepción original que provocó el fallo
	 */
	public EnvioCorreoException(String mensaje, Throwable causa) {
		super(mensaje, causa);
	}

}
