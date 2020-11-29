package co.edu.uniquindio.proyecto.modelo;

import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import co.edu.uniquindio.empresa.Caracteristica;
import co.edu.uniquindio.empresa.Ciudad;
import co.edu.uniquindio.empresa.Modelo;
import co.edu.uniquindio.empresa.Usuario;
import co.edu.uniquindio.empresa.Vehiculo;
import co.edu.uniquinio.unimotor.ejb.UnimotorEJBRemote;
import co.edu.uniquinio.unimotor.excepciones.AutentificacionEcxeption;

public class PruebaDelegado implements UnimotorEJBRemote {

	private UnimotorEJBRemote unimotorEJB;
	public static PruebaDelegado pruebaDelegado = instancia();

	private PruebaDelegado() {
		try {
			unimotorEJB = (UnimotorEJBRemote) new InitialContext().lookup(UnimotorEJBRemote.JNDI);
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static PruebaDelegado instancia() {
		if (pruebaDelegado == null) {
			pruebaDelegado = new PruebaDelegado();
			return pruebaDelegado;
		}
		return pruebaDelegado;
	}

	/**
	 * @param usuario
	 * @throws Exception
	 * @see co.edu.uniquinio.unimotor.ejb.UnimotorEJBRemote#registrarUsuario(co.edu.uniquindio.empresa.Usuario)
	 */
	public void registrarUsuario(Usuario usuario) throws Exception {
		unimotorEJB.registrarUsuario(usuario);
	}

	/**
	 * @param email
	 * @param password
	 * @return
	 * @throws AutentificacionEcxeption
	 * @see co.edu.uniquinio.unimotor.ejb.UnimotorEJBRemote#autentificarUsuario(java.lang.String,
	 *      java.lang.String)
	 */
	public Usuario autentificarUsuario(String email, String password) throws AutentificacionEcxeption {
		return unimotorEJB.autentificarUsuario(email, password);
	}

	/**
	 * @param vehiculo
	 * @throws Exception
	 * @see co.edu.uniquinio.unimotor.ejb.UnimotorEJBRemote#registrarVehiculos(co.edu.uniquindio.empresa.Vehiculo)
	 */
	public void registrarVehiculos(Vehiculo vehiculo) throws Exception {
		unimotorEJB.registrarVehiculos(vehiculo);
	}

	/**
	 * @return
	 * @see co.edu.uniquinio.unimotor.ejb.UnimotorEJBRemote#obtenerListaVehiculo()
	 */
	public List<Vehiculo> obtenerListaVehiculo() {
		return unimotorEJB.obtenerListaVehiculo();
	}

	/**
	 * @return
	 * @see co.edu.uniquinio.unimotor.ejb.UnimotorEJBRemote#obtenerListaVehiculoCiudad()
	 */
	public List<Vehiculo> obtenerListaVehiculoCiudad() {
		return unimotorEJB.obtenerListaVehiculoCiudad();
	}

	/**
	 * @param vehiculo
	 * @throws Exception
	 * @see co.edu.uniquinio.unimotor.ejb.UnimotorEJBRemote#modificarVehiculos(co.edu.uniquindio.empresa.Vehiculo)
	 */
	public void modificarVehiculos(Vehiculo vehiculo) throws Exception {
		unimotorEJB.modificarVehiculos(vehiculo);
	}

	/**
	 * @param vehiculo
	 * @throws Exception
	 * @see co.edu.uniquinio.unimotor.ejb.UnimotorEJBRemote#EliminarVehiculos(co.edu.uniquindio.empresa.Vehiculo)
	 */
	public void EliminarVehiculos(Vehiculo vehiculo) throws Exception {
		unimotorEJB.EliminarVehiculos(vehiculo);
	}

	/**
	 * @return
	 * @see co.edu.uniquinio.unimotor.ejb.UnimotorEJBRemote#obtenerListaCaracteristica()
	 */
	public List<Caracteristica> obtenerListaCaracteristica() {
		return unimotorEJB.obtenerListaCaracteristica();
	}

	/**
	 * @return
	 * @see co.edu.uniquinio.unimotor.ejb.UnimotorEJBRemote#obtenerListaCiudad()
	 */
	public List<Ciudad> obtenerListaCiudad() {
		return unimotorEJB.obtenerListaCiudad();
	}

	/**
	 * @return
	 * @see co.edu.uniquinio.unimotor.ejb.UnimotorEJBRemote#obtenerListaModelos()
	 */
	public List<Modelo> obtenerListaModelos() {
		return unimotorEJB.obtenerListaModelos();
	}

	/**
	 * @param asunto
	 * @param mensaje
	 * @param destinatario
	 * @throws Exception
	 * @see co.edu.uniquinio.unimotor.ejb.UnimotorEJBRemote#enviarEmail(java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	public void enviarEmail(String asunto, String mensaje, String destinatario) throws Exception {
		unimotorEJB.enviarEmail(asunto, mensaje, destinatario);
	}

}
