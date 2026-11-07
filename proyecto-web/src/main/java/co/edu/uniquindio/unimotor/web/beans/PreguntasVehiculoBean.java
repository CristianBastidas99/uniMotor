package co.edu.uniquindio.unimotor.web.beans;

import java.io.Serializable;
import java.util.List;

import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import co.edu.uniquindio.unimotor.ejb.PreguntaEJB;
import co.edu.uniquindio.unimotor.entidades.Cliente;
import co.edu.uniquindio.unimotor.entidades.Pregunta;
import co.edu.uniquindio.unimotor.excepciones.ElementoNoEncontradoException;
import co.edu.uniquindio.unimotor.excepciones.InformacionInvalidaException;
import co.edu.uniquindio.unimotor.web.util.Mensajes;

/**
 * Bean de la vista de detalle que gestiona las preguntas y respuestas del
 * vehículo mostrado y el formulario para preguntar.
 */
@Named
@ViewScoped
public class PreguntasVehiculoBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@EJB
	private PreguntaEJB preguntaEJB;

	@Inject
	private SeguridadBean seguridadBean;

	@Inject
	private DetalleVehiculoBean detalleVehiculoBean;

	/** Texto de la pregunta escrita en el formulario. */
	private String texto;

	/** Preguntas cargadas de forma perezosa; {@code null} fuerza la recarga. */
	private List<Pregunta> preguntas;

	/**
	 * Obtiene las preguntas del vehículo, de la más reciente a la más antigua
	 * (orden de la consulta). Se cargan una vez y se recargan tras preguntar.
	 *
	 * @return la lista de preguntas (nunca {@code null})
	 */
	public List<Pregunta> getPreguntas() {
		if (preguntas == null) {
			preguntas = preguntaEJB.listarPreguntasVehiculo(detalleVehiculoBean.getId());
		}
		return preguntas;
	}

	/**
	 * Obtiene el número de preguntas del vehículo.
	 *
	 * @return el total de preguntas
	 */
	public int getTotal() {
		return getPreguntas().size();
	}

	/**
	 * Envía la pregunta del formulario al vendedor.
	 */
	public void enviar() {
		if (!seguridadBean.isAutenticado()) {
			Mensajes.advertencia("Sesión requerida", "Inicia sesión para hacer una pregunta.");
			return;
		}
		try {
			preguntaEJB.hacerPregunta(seguridadBean.getCliente().getEmail(), detalleVehiculoBean.getId(), texto);
			texto = null;
			preguntas = null;
			Mensajes.info("Pregunta enviada", "El vendedor recibirá un correo con tu pregunta.");
		} catch (InformacionInvalidaException | ElementoNoEncontradoException e) {
			Mensajes.error("No se pudo enviar la pregunta", e.getMessage());
		}
	}

	/**
	 * Devuelve el nombre público de quien preguntó: nombre y primera letra del
	 * último apellido, sin exponer nunca el correo.
	 *
	 * @param cliente quien hizo la pregunta
	 * @return el nombre abreviado
	 */
	public String nombrePublico(Cliente cliente) {
		if (cliente == null || cliente.getNombre_completo() == null || cliente.getNombre_completo().trim().isEmpty()) {
			return "Usuario";
		}
		String[] partes = cliente.getNombre_completo().trim().split("\\s+");
		if (partes.length == 1) {
			return partes[0];
		}
		return partes[0] + " " + Character.toUpperCase(partes[partes.length - 1].charAt(0)) + ".";
	}

	/**
	 * Construye la dirección de inicio de sesión con regreso a esta página.
	 *
	 * @return ruta relativa al login con el parámetro {@code origen} codificado
	 */
	public String getUrlLogin() {
		return FavoritoBean.urlLogin(detalleVehiculoBean.getId());
	}

	/**
	 * Obtiene el texto del formulario.
	 *
	 * @return el texto de la pregunta
	 */
	public String getTexto() {
		return texto;
	}

	/**
	 * Establece el texto del formulario.
	 *
	 * @param texto el texto de la pregunta
	 */
	public void setTexto(String texto) {
		this.texto = texto;
	}
}
