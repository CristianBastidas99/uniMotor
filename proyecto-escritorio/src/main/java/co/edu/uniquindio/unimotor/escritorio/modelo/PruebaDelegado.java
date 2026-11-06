package co.edu.uniquindio.unimotor.escritorio.modelo;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import co.edu.uniquindio.unimotor.ejb.AdministradorEJBRemote;
import co.edu.uniquindio.unimotor.entidades.Usuario;
import co.edu.uniquindio.unimotor.excepciones.AutenticacionException;

/**
 * Delegado de negocio de la aplicacion de escritorio: oculta el lookup JNDI del
 * EJB remoto {@link AdministradorEJBRemote} y le delega las operaciones.
 * Se usa como singleton mediante {@link #pruebaDelegado}.
 */
public class PruebaDelegado implements AdministradorEJBRemote {

	/** Instancia unica del delegado. */
	public static PruebaDelegado pruebaDelegado = instancia();

	/** Referencia remota al EJB de administrador. */
	private AdministradorEJBRemote administradorEJB;

	/**
	 * Crea el delegado y busca el EJB remoto por su nombre JNDI.
	 */
	private PruebaDelegado() {
		try {
			administradorEJB = (AdministradorEJBRemote) new InitialContext().lookup(AdministradorEJBRemote.JNDI);
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Obtiene la instancia unica del delegado, creandola si no existe.
	 *
	 * @return la instancia unica de {@code PruebaDelegado}
	 */
	private static PruebaDelegado instancia() {
		if (pruebaDelegado == null) {
			pruebaDelegado = new PruebaDelegado();
		}
		return pruebaDelegado;
	}

	/**
	 * Autentica a un usuario delegando en el EJB remoto.
	 *
	 * @param email    correo electronico del usuario
	 * @param password contrasena del usuario
	 * @return el usuario autenticado
	 * @throws AutenticacionException si las credenciales no son validas
	 * @see AdministradorEJBRemote#autenticarUsuario(String, String)
	 */
	@Override
	public Usuario autenticarUsuario(String email, String password) throws AutenticacionException {
		return administradorEJB.autenticarUsuario(email, password);
	}

}
