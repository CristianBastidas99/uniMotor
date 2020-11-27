package co.edu.uniquinio.unimotor.ejb;

import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import co.edu.uniquindio.empresa.Usuario;
import co.edu.uniquinio.unimotor.excepciones.AutentificacionEcxeption;

/**
 * Session Bean implementation class PersonaEJB
 */
@Stateless
@LocalBean
public class PersonaEJB implements PersonaEJBRemote {

	@PersistenceContext
	private EntityManager entityManager;

	/**
	 * Default constructor.
	 */
	public PersonaEJB() {
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

}
