package co.edu.uniquindio.unimotor.ejb;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import co.edu.uniquindio.unimotor.entidades.Calificacion;
import co.edu.uniquindio.unimotor.entidades.Ciudad;
import co.edu.uniquindio.unimotor.entidades.Cliente;
import co.edu.uniquindio.unimotor.entidades.Pregunta;
import co.edu.uniquindio.unimotor.entidades.Usuario;
import co.edu.uniquindio.unimotor.entidades.Vehiculo;
import co.edu.uniquindio.unimotor.excepciones.AutenticacionException;
import co.edu.uniquindio.unimotor.excepciones.ElementoNoEncontradoException;
import co.edu.uniquindio.unimotor.excepciones.ElementoRepetidoException;
import co.edu.uniquindio.unimotor.excepciones.EnvioCorreoException;
import co.edu.uniquindio.unimotor.excepciones.InformacionInvalidaException;

/**
 * EJB con las operaciones de los clientes, usado por la aplicación web.
 */
@Stateless
@LocalBean
public class ClienteEJB {

	@PersistenceContext
	private EntityManager entityManager;

	@EJB
	private CorreoEJB correoEJB;

	@EJB
	private VehiculoEJB vehiculoEJB;

	private static final String ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";

	private final SecureRandom random = new SecureRandom();

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

	/**
	 * Genera una contraseña temporal para el usuario con el email dado y se la envía por correo.
	 *
	 * @param email correo electrónico del usuario
	 * @throws InformacionInvalidaException si el email es nulo o vacío
	 * @throws ElementoNoEncontradoException si no existe un usuario con ese email
	 * @throws EnvioCorreoException si no se puede enviar el correo (se deshace el cambio de contraseña)
	 */
	public void recuperarPassword(String email)
			throws InformacionInvalidaException, ElementoNoEncontradoException, EnvioCorreoException {
		if (email == null || email.trim().isEmpty()) {
			throw new InformacionInvalidaException("El email es obligatorio.");
		}
		TypedQuery<Usuario> query = entityManager.createNamedQuery(Usuario.AUTENTIFICAR_EMAIL, Usuario.class);
		query.setParameter("email", email.trim());
		List<Usuario> usuarios = query.getResultList();
		if (usuarios.isEmpty()) {
			throw new ElementoNoEncontradoException("No existe un usuario con el email " + email + ".");
		}
		Usuario usuario = usuarios.get(0);
		String temporal = generarPasswordTemporal();
		usuario.setPassword(temporal);
		correoEJB.enviarEmail(PlantillasCorreo.asuntoRecuperarPassword(),
				PlantillasCorreo.recuperarPassword(usuario, temporal), usuario.getEmail());
	}

	/**
	 * Registra un nuevo cliente.
	 *
	 * @param cliente cliente a registrar
	 * @return el cliente persistido
	 * @throws ElementoRepetidoException si ya existe un usuario con ese email
	 * @throws InformacionInvalidaException si faltan datos obligatorios, el email no es válido o la ciudad no existe
	 */
	public Cliente registrarCliente(Cliente cliente) throws ElementoRepetidoException, InformacionInvalidaException {
		if (cliente == null) {
			throw new InformacionInvalidaException("El cliente es obligatorio.");
		}
		if (cliente.getEmail() == null || cliente.getEmail().trim().isEmpty()) {
			throw new InformacionInvalidaException("El email es obligatorio.");
		}
		cliente.setEmail(cliente.getEmail().trim());
		validarFormatoEmail(cliente.getEmail());
		if (cliente.getPassword() == null || cliente.getPassword().isEmpty()) {
			throw new InformacionInvalidaException("La contraseña es obligatoria.");
		}
		if (cliente.getNombre_completo() == null || cliente.getNombre_completo().trim().isEmpty()) {
			throw new InformacionInvalidaException("El nombre completo es obligatorio.");
		}
		cliente.setCodigo_ciudad(buscarCiudad(cliente.getCodigo_ciudad()));
		registrarUsuario(cliente);
		return cliente;
	}

	/**
	 * Autentica a un cliente con su email y contraseña.
	 *
	 * @param email correo electrónico
	 * @param password contraseña
	 * @return el cliente autenticado
	 * @throws AutenticacionException si las credenciales son incorrectas o el usuario no es un cliente
	 */
	public Cliente autenticarCliente(String email, String password) throws AutenticacionException {
		if (email == null || password == null) {
			throw new AutenticacionException("El email o la contraseña son incorrectos.");
		}
		TypedQuery<Usuario> query = entityManager.createNamedQuery(Usuario.AUTENTIFICAR_USUARIO, Usuario.class);
		query.setParameter("email", email.trim());
		query.setParameter("password", password);
		List<Usuario> usuarios = query.getResultList();
		if (usuarios.isEmpty() || !(usuarios.get(0) instanceof Cliente)) {
			throw new AutenticacionException("El email o la contraseña son incorrectos.");
		}
		return (Cliente) usuarios.get(0);
	}

	/**
	 * Busca un cliente por su email.
	 *
	 * @param email correo electrónico del cliente
	 * @return el cliente encontrado
	 * @throws ElementoNoEncontradoException si no existe un cliente con ese email
	 */
	public Cliente buscarCliente(String email) throws ElementoNoEncontradoException {
		Cliente cliente = obtenerCliente(email);
		if (cliente == null) {
			throw new ElementoNoEncontradoException("No existe un cliente con el email " + email + ".");
		}
		return cliente;
	}

	/**
	 * Modifica los datos personales de un cliente (nombre, dirección, teléfonos, ciudad y email). No cambia la contraseña.
	 *
	 * @param cliente cliente con los nuevos datos; se identifica por su código de usuario
	 * @return la entidad gestionada ya modificada
	 * @throws ElementoNoEncontradoException si el cliente no existe
	 * @throws ElementoRepetidoException si el nuevo email ya lo usa otro usuario
	 * @throws InformacionInvalidaException si algún dato es inválido o la ciudad no existe
	 */
	public Cliente modificarCliente(Cliente cliente)
			throws ElementoNoEncontradoException, ElementoRepetidoException, InformacionInvalidaException {
		if (cliente == null || cliente.getCodigo_usuario() == null) {
			throw new ElementoNoEncontradoException("El cliente no existe.");
		}
		Cliente gestionado = entityManager.find(Cliente.class, cliente.getCodigo_usuario());
		if (gestionado == null) {
			throw new ElementoNoEncontradoException("El cliente no existe.");
		}
		if (cliente.getNombre_completo() == null || cliente.getNombre_completo().trim().isEmpty()) {
			throw new InformacionInvalidaException("El nombre completo es obligatorio.");
		}
		Ciudad ciudad = buscarCiudad(cliente.getCodigo_ciudad());
		String nuevoEmail = cliente.getEmail() == null ? null : cliente.getEmail().trim();
		boolean cambiaEmail = nuevoEmail != null && !nuevoEmail.isEmpty() && !nuevoEmail.equals(gestionado.getEmail());
		if (cambiaEmail) {
			validarFormatoEmail(nuevoEmail);
			if (existeEmail(nuevoEmail)) {
				throw new ElementoRepetidoException("El email ya se encuentra en uso.");
			}
		}
		gestionado.setNombre_completo(cliente.getNombre_completo());
		gestionado.setDireccion(cliente.getDireccion());
		Map<String, String> telefonos = cliente.getTelefono() == null ? null
				: new HashMap<String, String>(cliente.getTelefono());
		gestionado.setTelefono(telefonos);
		gestionado.setCodigo_ciudad(ciudad);
		if (cambiaEmail) {
			gestionado.setEmail(nuevoEmail);
		}
		return gestionado;
	}

	/**
	 * Cambia la contraseña de un cliente.
	 *
	 * @param email correo electrónico del cliente
	 * @param passwordActual contraseña actual
	 * @param passwordNueva contraseña nueva, de al menos 4 caracteres
	 * @throws AutenticacionException si el email no existe o la contraseña actual no coincide
	 * @throws InformacionInvalidaException si la contraseña nueva es vacía o tiene menos de 4 caracteres
	 */
	public void cambiarPassword(String email, String passwordActual, String passwordNueva)
			throws AutenticacionException, InformacionInvalidaException {
		Cliente cliente = obtenerCliente(email);
		if (cliente == null || passwordActual == null || !passwordActual.equals(cliente.getPassword())) {
			throw new AutenticacionException("El email o la contraseña son incorrectos.");
		}
		if (passwordNueva == null || passwordNueva.length() < 4) {
			throw new InformacionInvalidaException("La contraseña nueva debe tener al menos 4 caracteres.");
		}
		cliente.setPassword(passwordNueva);
	}

	/**
	 * Elimina un cliente junto con sus favoritos, preguntas, calificaciones y vehículos.
	 *
	 * @param email correo electrónico del cliente
	 * @throws ElementoNoEncontradoException si el cliente no existe
	 * @throws InformacionInvalidaException si el cliente tiene ventas o compras registradas
	 */
	public void eliminarCliente(String email) throws ElementoNoEncontradoException, InformacionInvalidaException {
		Cliente cliente = buscarCliente(email);
		if (cliente.getMis_compras() != null && !cliente.getMis_compras().isEmpty()) {
			throw new InformacionInvalidaException("El cliente tiene ventas o compras registradas.");
		}
		List<Long> codigosVehiculos = new ArrayList<Long>();
		if (cliente.getVehiculos_venta() != null) {
			for (Vehiculo v : cliente.getVehiculos_venta()) {
				if (v.getCodigo_venta() != null) {
					throw new InformacionInvalidaException("El cliente tiene ventas o compras registradas.");
				}
				codigosVehiculos.add(v.getCodigo_vehiculo());
			}
		}
		if (cliente.getVehiculos_favoritos() != null) {
			for (Vehiculo v : new ArrayList<Vehiculo>(cliente.getVehiculos_favoritos())) {
				if (v.getClientes() != null) {
					v.getClientes().remove(cliente);
				}
			}
			cliente.getVehiculos_favoritos().clear();
		}
		if (cliente.getPreguntas() != null) {
			for (Pregunta p : new ArrayList<Pregunta>(cliente.getPreguntas())) {
				if (p.getVehiculo() != null && p.getVehiculo().getPreguntas() != null) {
					p.getVehiculo().getPreguntas().remove(p);
				}
				entityManager.remove(p);
			}
			cliente.getPreguntas().clear();
		}
		if (cliente.getCalificaciones_hechas() != null) {
			for (Calificacion c : new ArrayList<Calificacion>(cliente.getCalificaciones_hechas())) {
				if (c.getVendedor() != null && c.getVendedor().getCalificaciones_recibidas() != null) {
					c.getVendedor().getCalificaciones_recibidas().remove(c);
				}
				entityManager.remove(c);
			}
			cliente.getCalificaciones_hechas().clear();
		}
		if (cliente.getCalificaciones_recibidas() != null) {
			for (Calificacion c : new ArrayList<Calificacion>(cliente.getCalificaciones_recibidas())) {
				if (c.getCalificador() != null && c.getCalificador().getCalificaciones_hechas() != null) {
					c.getCalificador().getCalificaciones_hechas().remove(c);
				}
				entityManager.remove(c);
			}
			cliente.getCalificaciones_recibidas().clear();
		}
		entityManager.flush();
		for (Long codigo : codigosVehiculos) {
			vehiculoEJB.eliminarVehiculo(codigo, cliente.getEmail());
		}
		entityManager.flush();
		entityManager.remove(cliente);
		entityManager.flush();
	}

	/**
	 * Agrega un vehículo a los favoritos de un cliente.
	 *
	 * @param email correo electrónico del cliente
	 * @param codigoVehiculo código del vehículo
	 * @throws ElementoNoEncontradoException si el cliente o el vehículo no existen
	 * @throws ElementoRepetidoException si el vehículo ya es favorito del cliente
	 * @throws InformacionInvalidaException si el vehículo pertenece al propio cliente
	 */
	public void agregarFavorito(String email, Long codigoVehiculo)
			throws ElementoNoEncontradoException, ElementoRepetidoException, InformacionInvalidaException {
		Cliente cliente = buscarCliente(email);
		Vehiculo vehiculo = buscarVehiculoPorCodigo(codigoVehiculo);
		if (vehiculo.getCodigo_dueno() != null && cliente.equals(vehiculo.getCodigo_dueno())) {
			throw new InformacionInvalidaException("No se puede agregar a favoritos un vehículo propio.");
		}
		if (vehiculo.getClientes() != null && vehiculo.getClientes().contains(cliente)) {
			throw new ElementoRepetidoException("El vehículo ya es un favorito del cliente.");
		}
		if (vehiculo.getClientes() == null) {
			vehiculo.setClientes(new ArrayList<Cliente>());
		}
		if (cliente.getVehiculos_favoritos() == null) {
			cliente.setVehiculos_favoritos(new ArrayList<Vehiculo>());
		}
		vehiculo.getClientes().add(cliente);
		cliente.getVehiculos_favoritos().add(vehiculo);
	}

	/**
	 * Quita un vehículo de los favoritos de un cliente.
	 *
	 * @param email correo electrónico del cliente
	 * @param codigoVehiculo código del vehículo
	 * @throws ElementoNoEncontradoException si el cliente o el vehículo no existen, o el vehículo no era favorito
	 */
	public void quitarFavorito(String email, Long codigoVehiculo) throws ElementoNoEncontradoException {
		Cliente cliente = buscarCliente(email);
		Vehiculo vehiculo = buscarVehiculoPorCodigo(codigoVehiculo);
		if (vehiculo.getClientes() == null || !vehiculo.getClientes().contains(cliente)) {
			throw new ElementoNoEncontradoException("El vehículo no está en los favoritos del cliente.");
		}
		vehiculo.getClientes().remove(cliente);
		if (cliente.getVehiculos_favoritos() != null) {
			cliente.getVehiculos_favoritos().remove(vehiculo);
		}
	}

	/**
	 * Lista los vehículos favoritos de un cliente.
	 *
	 * @param email correo electrónico del cliente
	 * @return la lista de vehículos favoritos
	 * @throws ElementoNoEncontradoException si el cliente no existe
	 */
	public List<Vehiculo> listarFavoritos(String email) throws ElementoNoEncontradoException {
		Cliente cliente = buscarCliente(email);
		TypedQuery<Vehiculo> query = entityManager.createNamedQuery(Cliente.LISTA_FAVORITO_CLIENTE_JOIN,
				Vehiculo.class);
		query.setParameter("email", cliente.getEmail());
		return query.getResultList();
	}

	/**
	 * Indica si un vehículo es favorito de un cliente.
	 *
	 * @param email correo electrónico del cliente
	 * @param codigoVehiculo código del vehículo
	 * @return {@code true} si es favorito; {@code false} si no lo es o alguno de los dos no existe
	 */
	public boolean esFavorito(String email, Long codigoVehiculo) {
		Cliente cliente = obtenerCliente(email);
		if (cliente == null || codigoVehiculo == null) {
			return false;
		}
		Vehiculo vehiculo = entityManager.find(Vehiculo.class, codigoVehiculo);
		return vehiculo != null && vehiculo.getClientes() != null && vehiculo.getClientes().contains(cliente);
	}

	private Cliente obtenerCliente(String email) {
		if (email == null) {
			return null;
		}
		TypedQuery<Cliente> query = entityManager.createNamedQuery(Cliente.FIND_BY_EMAIL, Cliente.class);
		query.setParameter("email", email.trim());
		List<Cliente> clientes = query.getResultList();
		return clientes.isEmpty() ? null : clientes.get(0);
	}

	private Vehiculo buscarVehiculoPorCodigo(Long codigoVehiculo) throws ElementoNoEncontradoException {
		Vehiculo vehiculo = codigoVehiculo == null ? null : entityManager.find(Vehiculo.class, codigoVehiculo);
		if (vehiculo == null) {
			throw new ElementoNoEncontradoException("El vehículo no existe.");
		}
		return vehiculo;
	}

	private Ciudad buscarCiudad(Ciudad ciudad) throws InformacionInvalidaException {
		if (ciudad == null || ciudad.getCodigo_ciudad() == null) {
			throw new InformacionInvalidaException("La ciudad es obligatoria.");
		}
		Ciudad gestionada = entityManager.find(Ciudad.class, ciudad.getCodigo_ciudad());
		if (gestionada == null) {
			throw new InformacionInvalidaException("La ciudad indicada no existe.");
		}
		return gestionada;
	}

	private void validarFormatoEmail(String email) throws InformacionInvalidaException {
		try {
			new InternetAddress(email, true);
		} catch (AddressException e) {
			throw new InformacionInvalidaException("El email no tiene un formato válido.");
		}
	}

	private String generarPasswordTemporal() {
		StringBuilder sb = new StringBuilder(8);
		for (int i = 0; i < 8; i++) {
			sb.append(ALFABETO.charAt(random.nextInt(ALFABETO.length())));
		}
		return sb.toString();
	}

}
