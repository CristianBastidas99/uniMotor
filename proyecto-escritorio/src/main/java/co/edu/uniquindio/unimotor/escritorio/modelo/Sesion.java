package co.edu.uniquindio.unimotor.escritorio.modelo;

import co.edu.uniquindio.unimotor.entidades.Administrador;

/**
 * Guarda el administrador que tiene la sesión activa en la aplicación de escritorio.
 */
public final class Sesion {

	/** Administrador autenticado, o nulo si no hay sesión. */
	private static Administrador administrador;

	/**
	 * Evita que se creen instancias de la clase de utilidad.
	 */
	private Sesion() {
	}

	/**
	 * Inicia la sesión con el administrador indicado.
	 *
	 * @param administrador administrador autenticado
	 */
	public static void iniciar(Administrador administrador) {
		Sesion.administrador = administrador;
	}

	/**
	 * Obtiene el administrador de la sesión.
	 *
	 * @return el administrador autenticado, o nulo si no hay sesión
	 */
	public static Administrador getAdministrador() {
		return administrador;
	}

	/**
	 * Indica si hay una sesión activa.
	 *
	 * @return verdadero si hay un administrador autenticado
	 */
	public static boolean isActiva() {
		return administrador != null;
	}

	/**
	 * Cierra la sesión actual.
	 */
	public static void cerrar() {
		administrador = null;
	}

}
