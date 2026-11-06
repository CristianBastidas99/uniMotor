package co.edu.uniquindio.unimotor.ejb;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import co.edu.uniquindio.unimotor.entidades.Calificacion;
import co.edu.uniquindio.unimotor.entidades.Cliente;
import co.edu.uniquindio.unimotor.entidades.Venta;
import co.edu.uniquindio.unimotor.excepciones.ElementoNoEncontradoException;
import co.edu.uniquindio.unimotor.excepciones.ElementoRepetidoException;
import co.edu.uniquindio.unimotor.excepciones.InformacionInvalidaException;

/**
 * EJB que administra las calificaciones (de 1 a 5 estrellas) que los compradores dan a los vendedores
 * a quienes les han comprado un vehículo.
 */
@Stateless
@LocalBean
public class CalificacionEJB {

	private static final int MAX_COMENTARIO = 500;

	@PersistenceContext
	private EntityManager entityManager;

	/**
	 * Crea el EJB de calificaciones.
	 */
	public CalificacionEJB() {
		super();
	}

	/**
	 * Califica a un vendedor. El calificador debe haberle comprado al menos un vehículo y solo puede
	 * calificarlo una vez.
	 *
	 * @param emailCalificador correo del cliente que califica
	 * @param emailVendedor correo del vendedor calificado
	 * @param estrellas puntaje de 1 a 5
	 * @param comentario comentario opcional (máximo 500 caracteres)
	 * @return la calificación persistida
	 * @throws ElementoNoEncontradoException si no existe el calificador o el vendedor
	 * @throws ElementoRepetidoException si el calificador ya calificó a ese vendedor
	 * @throws InformacionInvalidaException si los datos son inválidos, ambos son la misma persona
	 *             o el calificador no le ha comprado al vendedor
	 */
	public Calificacion calificarVendedor(String emailCalificador, String emailVendedor, int estrellas,
			String comentario)
			throws ElementoNoEncontradoException, ElementoRepetidoException, InformacionInvalidaException {
		if (emailCalificador == null || emailCalificador.trim().isEmpty()) {
			throw new InformacionInvalidaException("El email del calificador es obligatorio.");
		}
		if (emailVendedor == null || emailVendedor.trim().isEmpty()) {
			throw new InformacionInvalidaException("El email del vendedor es obligatorio.");
		}
		String emailCal = emailCalificador.trim();
		String emailVen = emailVendedor.trim();
		if (estrellas < 1 || estrellas > 5) {
			throw new InformacionInvalidaException("Las estrellas deben estar entre 1 y 5.");
		}
		String texto = null;
		if (comentario != null && !comentario.trim().isEmpty()) {
			texto = comentario.trim();
			if (texto.length() > MAX_COMENTARIO) {
				throw new InformacionInvalidaException(
						"El comentario no puede superar los " + MAX_COMENTARIO + " caracteres.");
			}
		}
		Cliente calificador = buscarCliente(emailCal);
		if (calificador == null) {
			throw new ElementoNoEncontradoException("No existe un cliente con el email " + emailCal + ".");
		}
		Cliente vendedor = buscarCliente(emailVen);
		if (vendedor == null) {
			throw new ElementoNoEncontradoException("No existe un cliente con el email " + emailVen + ".");
		}
		if (calificador.equals(vendedor)) {
			throw new InformacionInvalidaException("No puedes calificarte a ti mismo.");
		}
		if (contarCompras(emailCal, emailVen) <= 0) {
			throw new InformacionInvalidaException(
					"Solo puedes calificar a vendedores a los que les has comprado un vehículo.");
		}
		if (existeCalificacion(emailCal, emailVen)) {
			throw new ElementoRepetidoException("Ya calificaste a este vendedor.");
		}

		Calificacion calificacion = new Calificacion(calificador, vendedor, estrellas, texto, new Date());
		entityManager.persist(calificacion);
		if (calificador.getCalificaciones_hechas() == null) {
			calificador.setCalificaciones_hechas(new ArrayList<Calificacion>());
		}
		calificador.getCalificaciones_hechas().add(calificacion);
		if (vendedor.getCalificaciones_recibidas() == null) {
			vendedor.setCalificaciones_recibidas(new ArrayList<Calificacion>());
		}
		vendedor.getCalificaciones_recibidas().add(calificacion);
		entityManager.flush();
		return calificacion;
	}

	/**
	 * Lista las calificaciones recibidas por un vendedor, de la más reciente a la más antigua.
	 *
	 * @param emailVendedor correo del vendedor
	 * @return lista de calificaciones (vacía si no tiene)
	 */
	public List<Calificacion> listarCalificacionesVendedor(String emailVendedor) {
		return entityManager.createNamedQuery(Calificacion.CALIFICACIONES_POR_EMAIL_VENDEDOR, Calificacion.class)
				.setParameter("email", emailVendedor == null ? null : emailVendedor.trim()).getResultList();
	}

	/**
	 * Calcula el promedio de estrellas de un vendedor, redondeado a un decimal.
	 *
	 * @param emailVendedor correo del vendedor
	 * @return el promedio, o {@code null} si no tiene calificaciones o el correo no existe
	 */
	public Double promedioVendedor(String emailVendedor) {
		Double promedio = entityManager.createNamedQuery(Calificacion.PROMEDIO_POR_EMAIL_VENDEDOR, Double.class)
				.setParameter("email", emailVendedor == null ? null : emailVendedor.trim()).getSingleResult();
		if (promedio == null) {
			return null;
		}
		return Math.round(promedio * 10) / 10.0;
	}

	/**
	 * Indica si un cliente puede calificar a un vendedor: ambos existen, son distintos, el cliente le ha
	 * comprado al vendedor y todavía no lo ha calificado. Nunca lanza excepciones.
	 *
	 * @param emailCalificador correo del posible calificador
	 * @param emailVendedor correo del vendedor
	 * @return {@code true} si puede calificarlo
	 */
	public boolean puedeCalificar(String emailCalificador, String emailVendedor) {
		try {
			if (emailCalificador == null || emailVendedor == null || emailCalificador.trim().isEmpty()
					|| emailVendedor.trim().isEmpty()) {
				return false;
			}
			String emailCal = emailCalificador.trim();
			String emailVen = emailVendedor.trim();
			if (emailCal.equals(emailVen)) {
				return false;
			}
			if (buscarCliente(emailCal) == null || buscarCliente(emailVen) == null) {
				return false;
			}
			return contarCompras(emailCal, emailVen) > 0 && !existeCalificacion(emailCal, emailVen);
		} catch (RuntimeException e) {
			return false;
		}
	}

	private Cliente buscarCliente(String email) {
		List<Cliente> clientes = entityManager.createNamedQuery(Cliente.FIND_BY_EMAIL, Cliente.class)
				.setParameter("email", email).getResultList();
		return clientes.isEmpty() ? null : clientes.get(0);
	}

	private long contarCompras(String emailComprador, String emailVendedor) {
		Long compras = entityManager.createNamedQuery(Venta.COMPRAS_A_VENDEDOR, Long.class)
				.setParameter("emailComprador", emailComprador).setParameter("emailVendedor", emailVendedor)
				.getSingleResult();
		return compras == null ? 0 : compras;
	}

	private boolean existeCalificacion(String emailCalificador, String emailVendedor) {
		return !entityManager.createNamedQuery(Calificacion.CALIFICACION_POR_CALIFICADOR_Y_VENDEDOR, Calificacion.class)
				.setParameter("emailCalificador", emailCalificador).setParameter("emailVendedor", emailVendedor)
				.getResultList().isEmpty();
	}

}
