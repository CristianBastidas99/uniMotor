package co.edu.uniquinio.unimotor.ejb;

import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import co.edu.uniquindio.empresa.Caracteristica;
import co.edu.uniquindio.empresa.Ciudad;
import co.edu.uniquindio.empresa.Modelo;
import co.edu.uniquindio.empresa.Usuario;
import co.edu.uniquindio.empresa.Vehiculo;
import co.edu.uniquinio.unimotor.excepciones.AutentificacionEcxeption;

/**
 * Session Bean implementation class PersonaEJB
 */
@Stateless
@LocalBean
public class UnimotorEJB implements UnimotorEJBRemote {

	@PersistenceContext
	private EntityManager entityManager;

	/**
	 * Default constructor.
	 */
	public UnimotorEJB() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void registrarUsuario(Usuario usuario) throws Exception {
		if (entityManager.find(Usuario.class, usuario.getCodigo_usuario()) != null) {
			throw new Exception("El usuario ya se encuentra registrado");
		}
		if (buscarEmail(usuario.getEmail())) {
			throw new Exception("El email ya se encuentra en uso");
		}
		entityManager.persist(usuario);
	}

	public boolean buscarEmail(String email) {

		boolean bandera = true;
		TypedQuery<Usuario> query = entityManager.createNamedQuery(Usuario.AUTENTIFICAR_EMAIL, Usuario.class);
		query.setParameter("email", email);

		List<Usuario> usuarios = query.getResultList();

		if (usuarios.isEmpty()) {

			bandera = false;
		}

		return bandera;
	}

	@Override
	public Usuario autentificarUsuario(String email, String password) throws AutentificacionEcxeption {
		// TODO Auto-generated method stub
		TypedQuery<Usuario> query = entityManager.createNamedQuery(Usuario.AUTENTIFICAR_USUARO, Usuario.class);
		query.setParameter("email", email);
		query.setParameter("password", password);

		List<Usuario> usuarios = query.getResultList();

		if (usuarios.isEmpty()) {
			throw new AutentificacionEcxeption("El email o password esta incorrerecto.");
		}

		return usuarios.get(0);
	}

	@Override
	public void registrarVehiculos(Vehiculo vehiculo) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Vehiculo> obtenerListaVehiculo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void modificarVehiculos(Vehiculo vehiculo) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void EliminarVehiculos(Vehiculo vehiculo) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Caracteristica> obtenerListaCaracteristica() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Ciudad> obtenerListaCiudad() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Modelo> obtenerListaModelos() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void enviarEmail(String asunto, String mensaje, String destinatario) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
