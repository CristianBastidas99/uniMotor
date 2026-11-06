package co.edu.uniquindio.unimotor.ejb;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import co.edu.uniquindio.unimotor.entidades.Cliente;
import co.edu.uniquindio.unimotor.entidades.Pregunta;
import co.edu.uniquindio.unimotor.entidades.Vehiculo;
import co.edu.uniquindio.unimotor.excepciones.ElementoNoEncontradoException;
import co.edu.uniquindio.unimotor.excepciones.EnvioCorreoException;
import co.edu.uniquindio.unimotor.excepciones.InformacionInvalidaException;

/**
 * EJB que administra las preguntas que los clientes hacen a los vendedores sobre los vehículos publicados
 * y las respuestas de estos.
 */
@Stateless
@LocalBean
public class PreguntaEJB {

	private static final Logger LOGGER = Logger.getLogger(PreguntaEJB.class.getName());
	private static final int MAX_PREGUNTA = 500;
	private static final int MAX_RESPUESTA = 1000;

	@PersistenceContext
	private EntityManager entityManager;

	@EJB
	private CorreoEJB correoEJB;

	/**
	 * Crea el EJB de preguntas.
	 */
	public PreguntaEJB() {
		super();
	}

	/**
	 * Registra una pregunta de un cliente sobre un vehículo y avisa por correo al dueño. Si el correo falla,
	 * la pregunta se conserva y solo se registra una advertencia en el log.
	 *
	 * @param emailCliente correo del cliente que pregunta
	 * @param codigoVehiculo identificador del vehículo
	 * @param texto texto de la pregunta (obligatorio, máximo 500 caracteres)
	 * @return la pregunta persistida
	 * @throws ElementoNoEncontradoException si no existe el cliente o el vehículo
	 * @throws InformacionInvalidaException si el texto es vacío o muy largo, el vehículo está inactivo o es del propio cliente
	 */
	public Pregunta hacerPregunta(String emailCliente, Long codigoVehiculo, String texto)
			throws ElementoNoEncontradoException, InformacionInvalidaException {
		if (texto == null || texto.trim().isEmpty()) {
			throw new InformacionInvalidaException("El texto de la pregunta es obligatorio.");
		}
		String descripcion = texto.trim();
		if (descripcion.length() > MAX_PREGUNTA) {
			throw new InformacionInvalidaException(
					"La pregunta no puede superar los " + MAX_PREGUNTA + " caracteres.");
		}
		if (emailCliente == null || emailCliente.trim().isEmpty()) {
			throw new InformacionInvalidaException("El email del cliente es obligatorio.");
		}
		if (codigoVehiculo == null) {
			throw new InformacionInvalidaException("El vehículo es obligatorio.");
		}
		List<Cliente> clientes = entityManager.createNamedQuery(Cliente.FIND_BY_EMAIL, Cliente.class)
				.setParameter("email", emailCliente.trim()).getResultList();
		if (clientes.isEmpty()) {
			throw new ElementoNoEncontradoException("No existe un cliente con el email " + emailCliente.trim() + ".");
		}
		Cliente cliente = clientes.get(0);
		Vehiculo vehiculo = entityManager.find(Vehiculo.class, codigoVehiculo);
		if (vehiculo == null) {
			throw new ElementoNoEncontradoException("No existe el vehículo con código " + codigoVehiculo + ".");
		}
		if (!vehiculo.isActivo()) {
			throw new InformacionInvalidaException("El vehículo no está disponible para recibir preguntas.");
		}
		Cliente dueno = vehiculo.getCodigo_dueno();
		if (dueno != null && cliente.equals(dueno)) {
			throw new InformacionInvalidaException("No puedes hacer preguntas sobre tu propio vehículo.");
		}

		Pregunta pregunta = new Pregunta(vehiculo, cliente, new Date(), descripcion);
		entityManager.persist(pregunta);
		if (vehiculo.getPreguntas() == null) {
			vehiculo.setPreguntas(new ArrayList<Pregunta>());
		}
		vehiculo.getPreguntas().add(pregunta);
		if (cliente.getPreguntas() == null) {
			cliente.setPreguntas(new ArrayList<Pregunta>());
		}
		cliente.getPreguntas().add(pregunta);
		entityManager.flush();

		if (dueno != null) {
			try {
				correoEJB.enviarEmail(PlantillasCorreo.asuntoPreguntaRecibida(pregunta),
						PlantillasCorreo.preguntaRecibida(pregunta), dueno.getEmail());
			} catch (EnvioCorreoException | InformacionInvalidaException e) {
				LOGGER.log(Level.WARNING, "La pregunta " + pregunta.getCodigo_pregunta()
						+ " se guardó, pero no se pudo avisar por correo al vendedor: " + e.getMessage(), e);
			}
		}
		return pregunta;
	}

	/**
	 * Registra la respuesta del dueño del vehículo a una pregunta.
	 *
	 * @param codigoPregunta identificador de la pregunta
	 * @param respuesta texto de la respuesta (obligatorio, máximo 1000 caracteres)
	 * @param emailDueno correo del dueño del vehículo que responde
	 * @return la pregunta con su respuesta
	 * @throws ElementoNoEncontradoException si la pregunta no existe
	 * @throws InformacionInvalidaException si la respuesta es vacía o muy larga, quien responde no es el dueño
	 *             o la pregunta ya fue respondida
	 */
	public Pregunta responderPregunta(Long codigoPregunta, String respuesta, String emailDueno)
			throws ElementoNoEncontradoException, InformacionInvalidaException {
		if (respuesta == null || respuesta.trim().isEmpty()) {
			throw new InformacionInvalidaException("La respuesta es obligatoria.");
		}
		String texto = respuesta.trim();
		if (texto.length() > MAX_RESPUESTA) {
			throw new InformacionInvalidaException(
					"La respuesta no puede superar los " + MAX_RESPUESTA + " caracteres.");
		}
		if (codigoPregunta == null) {
			throw new ElementoNoEncontradoException("No existe la pregunta indicada.");
		}
		Pregunta pregunta = entityManager.find(Pregunta.class, codigoPregunta);
		if (pregunta == null) {
			throw new ElementoNoEncontradoException("No existe la pregunta con código " + codigoPregunta + ".");
		}
		Cliente dueno = pregunta.getVehiculo() == null ? null : pregunta.getVehiculo().getCodigo_dueno();
		String email = emailDueno == null ? null : emailDueno.trim();
		if (dueno == null || email == null || !email.equals(dueno.getEmail())) {
			throw new InformacionInvalidaException("Solo el dueño del vehículo puede responder la pregunta.");
		}
		if (pregunta.getRespuesta() != null) {
			throw new InformacionInvalidaException("La pregunta ya fue respondida.");
		}
		pregunta.setRespuesta(texto);
		pregunta.setFecha_respuesta(new Date());
		return pregunta;
	}

	/**
	 * Lista las preguntas de un vehículo, de la más reciente a la más antigua.
	 *
	 * @param codigoVehiculo identificador del vehículo
	 * @return lista de preguntas
	 */
	public List<Pregunta> listarPreguntasVehiculo(Long codigoVehiculo) {
		return entityManager.createNamedQuery(Pregunta.PREGUNTAS_POR_VEHICULO, Pregunta.class)
				.setParameter("codigo", codigoVehiculo).getResultList();
	}

	/**
	 * Lista las preguntas recibidas sobre los vehículos de un vendedor.
	 *
	 * @param emailDueno correo del vendedor
	 * @return lista de preguntas
	 */
	public List<Pregunta> listarPreguntasRecibidas(String emailDueno) {
		return entityManager.createNamedQuery(Pregunta.PREGUNTAS_RECIBIDAS_POR_VENDEDOR, Pregunta.class)
				.setParameter("email", emailDueno == null ? null : emailDueno.trim()).getResultList();
	}

	/**
	 * Lista las preguntas sin responder sobre los vehículos de un vendedor.
	 *
	 * @param emailDueno correo del vendedor
	 * @return lista de preguntas pendientes
	 */
	public List<Pregunta> listarPreguntasSinResponder(String emailDueno) {
		return entityManager.createNamedQuery(Pregunta.PREGUNTAS_SIN_RESPONDER_POR_VENDEDOR, Pregunta.class)
				.setParameter("email", emailDueno == null ? null : emailDueno.trim()).getResultList();
	}

	/**
	 * Lista las preguntas que ha hecho un cliente.
	 *
	 * @param emailCliente correo del cliente
	 * @return lista de preguntas
	 */
	public List<Pregunta> listarPreguntasHechas(String emailCliente) {
		return entityManager.createNamedQuery(Pregunta.PREGUNTAS_HECHAS_POR_CLIENTE, Pregunta.class)
				.setParameter("email", emailCliente == null ? null : emailCliente.trim()).getResultList();
	}

}
