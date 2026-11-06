package co.edu.uniquindio.unimotor.ejb;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import co.edu.uniquindio.unimotor.entidades.Usuario;
import co.edu.uniquindio.unimotor.excepciones.AutenticacionException;

/**
 * EJB con las operaciones de administración expuestas a la aplicación de escritorio.
 */
@Stateless
public class AdministradorEJB implements AdministradorEJBRemote {

	@PersistenceContext
	private EntityManager entityManager;

	/**
	 * Crea el EJB de administración.
	 */
	public AdministradorEJB() {
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Usuario autenticarUsuario(String email, String password) throws AutenticacionException {
		TypedQuery<Usuario> query = entityManager.createNamedQuery(Usuario.AUTENTIFICAR_USUARIO, Usuario.class);
		query.setParameter("email", email);
		query.setParameter("password", password);

		List<Usuario> usuarios = query.getResultList();

		if (usuarios.isEmpty()) {
			throw new AutenticacionException("El email o la contraseña son incorrectos.");
		}

		return usuarios.get(0);
	}

}
