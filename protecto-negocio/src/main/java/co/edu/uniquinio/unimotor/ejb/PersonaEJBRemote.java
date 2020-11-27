package co.edu.uniquinio.unimotor.ejb;

import javax.ejb.Remote;

import co.edu.uniquindio.empresa.*;
import co.edu.uniquinio.unimotor.excepciones.AutentificacionEcxeption;

@Remote
public interface PersonaEJBRemote {

	void registrarUsuario(Usuario usuario) throws Exception;

	Usuario autentificarUsuario(String email, String password) throws AutentificacionEcxeption;

}
