package co.edu.uniquindio.unimotor.ejb;

import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import co.edu.uniquindio.unimotor.entidades.Usuario;
import co.edu.uniquindio.unimotor.excepciones.ElementoRepetidoException;
import co.edu.uniquindio.unimotor.excepciones.InformacionInvalidaException;

/**
 * EJB con las operaciones de los clientes, usado por la aplicación web.
 */
@Stateless
@LocalBean
public class ClienteEJB {

	@PersistenceContext
	private EntityManager entityManager;

	/**
	 * Crea el EJB de clientes.
	 */
	public ClienteEJB() {
		super();
	}

	/**
	 * Registra un nuevo usuario.
	 *
	 * @param usuario usuario a registrar
	 * @throws ElementoRepetidoException si ya existe un usuario con el mismo código o email
	 * @throws InformacionInvalidaException si el usuario es nulo o su email o contraseña son nulos o vacíos
	 */
	public void registrarUsuario(Usuario usuario) throws ElementoRepetidoException, InformacionInvalidaException {
		if (usuario == null) {
			throw new InformacionInvalidaException("El usuario es obligatorio.");
		}
		if (usuario.getEmail() == null || usuario.getEmail().trim().isEmpty()) {
			throw new InformacionInvalidaException("El email es obligatorio.");
		}
		if (usuario.getPassword() == null || usuario.getPassword().isEmpty()) {
			throw new InformacionInvalidaException("La contraseña es obligatoria.");
		}
		if (usuario.getCodigo_usuario() != null
				&& entityManager.find(Usuario.class, usuario.getCodigo_usuario()) != null) {
			throw new ElementoRepetidoException("El usuario ya se encuentra registrado.");
		}
		if (existeEmail(usuario.getEmail())) {
			throw new ElementoRepetidoException("El email ya se encuentra en uso.");
		}
		entityManager.persist(usuario);
	}

	/**
	 * Indica si ya existe un usuario con el email dado.
	 *
	 * @param email correo electrónico a buscar
	 * @return {@code true} si existe un usuario con ese email, {@code false} en caso contrario
	 */
	public boolean existeEmail(String email) {
		TypedQuery<Usuario> query = entityManager.createNamedQuery(Usuario.AUTENTIFICAR_EMAIL, Usuario.class);
		query.setParameter("email", email);
		List<Usuario> usuarios = query.getResultList();
		return !usuarios.isEmpty();
	}

}
