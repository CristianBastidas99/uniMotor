package co.edu.uniquindio.unimotor.ejb;

import javax.ejb.Remote;

import co.edu.uniquindio.unimotor.entidades.Usuario;
import co.edu.uniquindio.unimotor.excepciones.AutenticacionException;

/**
 * Interfaz remota del EJB de administración, usada por la aplicación de escritorio.
 */
@Remote
public interface AdministradorEJBRemote {

	/**
	 * Nombre JNDI global del EJB de administración.
	 */
	public static final String JNDI = "java:global/proyecto-ear/proyecto-negocio/AdministradorEJB!co.edu.uniquindio.unimotor.ejb.AdministradorEJBRemote";

	/**
	 * Autentica a un usuario por email y contraseña.
	 *
	 * @param email correo electrónico del usuario
	 * @param password contraseña del usuario
	 * @return el usuario autenticado
	 * @throws AutenticacionException si el email o la contraseña son incorrectos
	 */
	Usuario autenticarUsuario(String email, String password) throws AutenticacionException;

}
