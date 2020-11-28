package co.edu.uniquinio.unimotor.ejb;

import java.util.List;

import javax.ejb.Remote;

import co.edu.uniquindio.empresa.*;
import co.edu.uniquinio.unimotor.excepciones.AutentificacionEcxeption;

@Remote
public interface UnimotorEJBRemote {

	void registrarUsuario(Usuario usuario) throws Exception;

	Usuario autentificarUsuario(String email, String password) throws AutentificacionEcxeption;
	
	void registrarVehiculos(Vehiculo vehiculo) throws Exception;
	
	List<Vehiculo> obtenerListaVehiculo();
	
	void modificarVehiculos(Vehiculo vehiculo) throws Exception;
	
	void EliminarVehiculos(Vehiculo vehiculo) throws Exception;
	
	List<Caracteristica> obtenerListaCaracteristica();
	
	List<Ciudad> obtenerListaCiudad();
	
	List<Modelo> obtenerListaModelos();
	
	void enviarEmail(String asunto, String mensaje, String destinatario) throws Exception;

}