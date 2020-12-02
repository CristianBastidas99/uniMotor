package co.edu.uniquinio.unimotor.ejb;

import java.util.List;

import javax.ejb.Remote;

import co.edu.uniquindio.empresa.*;
import co.edu.uniquinio.unimotor.excepciones.AutentificacionEcxeption;

@Remote
public interface UnimotorEJBRemote {
	
	public static final String JNDI = "java:global/proyecto-ear/protecto-negocio/UnimotorEJB!co.edu.uniquinio.unimotor.ejb.UnimotorEJBRemote";

	void registrarUsuario(Usuario usuario) throws Exception;

	Usuario autentificarUsuario(String email, String password) throws AutentificacionEcxeption;
	
	void registrarCliente(Cliente cliente) throws Exception;
	
	void registrarAdministrador(Administrador administrador) throws Exception;
	
	void registrarVehiculos(Vehiculo vehiculo) throws Exception;
	
	List<Vehiculo> obtenerListaVehiculo();
	
	List<Vehiculo> obtenerListaVehiculoCiudad();
	
	void modificarVehiculos(Vehiculo vehiculo) throws Exception;
	
	void EliminarVehiculos(Vehiculo vehiculo) throws Exception;
	
	List<Caracteristica> obtenerListaCaracteristica();
	
	List<Ciudad> obtenerListaCiudad();
	
	List<Modelo> obtenerListaModelos();
	
	void enviarEmail(String asunto, String mensaje, String destinatario) throws Exception;

}
