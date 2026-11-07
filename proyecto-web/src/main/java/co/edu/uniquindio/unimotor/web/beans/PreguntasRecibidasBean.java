package co.edu.uniquindio.unimotor.web.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;

import co.edu.uniquindio.unimotor.ejb.PreguntaEJB;
import co.edu.uniquindio.unimotor.entidades.Cliente;
import co.edu.uniquindio.unimotor.entidades.Pregunta;
import co.edu.uniquindio.unimotor.excepciones.ElementoNoEncontradoException;
import co.edu.uniquindio.unimotor.excepciones.InformacionInvalidaException;
import co.edu.uniquindio.unimotor.web.util.Mensajes;

/**
 * Bean de la página "Preguntas recibidas": filtra y responde las preguntas de los vehículos del cliente.
 */
@Named
@ViewScoped
public class PreguntasRecibidasBean implements Serializable {

	private static final long serialVersionUID = 1L;

	@EJB
	private PreguntaEJB preguntaEJB;

	@Inject
	private SeguridadBean seguridadBean;

	private List<Pregunta> todas = new ArrayList<Pregunta>();

	private String filtro = "SIN";

	private Pregunta seleccionada;

	private String respuesta;

	/** Carga inicial. */
	@PostConstruct
	public void init() {
		recargar();
	}

	private void recargar() {
		todas = preguntaEJB.listarPreguntasRecibidas(seguridadBean.getCliente().getEmail());
	}

	/**
	 * Indica si una pregunta ya tiene respuesta.
	 *
	 * @param p pregunta
	 * @return {@code true} si fue respondida
	 */
	public boolean respondida(Pregunta p) {
		return p.getRespuesta() != null && !p.getRespuesta().trim().isEmpty();
	}

	/**
	 * Preguntas según el filtro actual.
	 *
	 * @return lista filtrada
	 */
	public List<Pregunta> getVisibles() {
		if ("TODAS".equals(filtro)) {
			return todas;
		}
		boolean buscarRespondidas = "RESP".equals(filtro);
		List<Pregunta> res = new ArrayList<Pregunta>();
		for (Pregunta p : todas) {
			if (respondida(p) == buscarRespondidas) {
				res.add(p);
			}
		}
		return res;
	}

	/**
	 * Total de preguntas.
	 *
	 * @return total
	 */
	public int getTotal() {
		return todas.size();
	}

	/**
	 * Preguntas sin responder.
	 *
	 * @return cantidad
	 */
	public int getSinResponder() {
		int n = 0;
		for (Pregunta p : todas) {
			if (!respondida(p)) {
				n++;
			}
		}
		return n;
	}

	/**
	 * Preguntas respondidas.
	 *
	 * @return cantidad
	 */
	public int getRespondidas() {
		return getTotal() - getSinResponder();
	}

	/**
	 * Nombre y inicial del apellido de quien pregunta; nunca el email.
	 *
	 * @param cliente quien pregunta
	 * @return nombre público
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
	 * Prepara el diálogo de respuesta.
	 *
	 * @param p pregunta elegida
	 */
	public void prepararRespuesta(Pregunta p) {
		this.seleccionada = p;
		this.respuesta = null;
	}

	/** Publica la respuesta; el parámetro de retorno {@code exito} indica si se puede cerrar el diálogo. */
	public void responder() {
		boolean exito = false;
		try {
			if (seleccionada == null) {
				Mensajes.error("No se pudo responder", "Selecciona una pregunta.");
			} else {
				preguntaEJB.responderPregunta(seleccionada.getCodigo_pregunta(), respuesta,
						seguridadBean.getCliente().getEmail());
				Mensajes.info("Respuesta publicada", "Tu respuesta ya es visible para los interesados.");
				seleccionada = null;
				respuesta = null;
				recargar();
				exito = true;
			}
		} catch (ElementoNoEncontradoException | InformacionInvalidaException e) {
			Mensajes.error("No se pudo responder", e.getMessage());
		}
		PrimeFaces.current().ajax().addCallbackParam("exito", exito);
	}

	/**
	 * Filtro actual.
	 *
	 * @return SIN, RESP o TODAS
	 */
	public String getFiltro() {
		return filtro;
	}

	/**
	 * Establece el filtro.
	 *
	 * @param filtro SIN, RESP o TODAS
	 */
	public void setFiltro(String filtro) {
		this.filtro = filtro;
	}

	/**
	 * Pregunta elegida para responder.
	 *
	 * @return la pregunta o {@code null}
	 */
	public Pregunta getSeleccionada() {
		return seleccionada;
	}

	/**
	 * Texto de la respuesta.
	 *
	 * @return la respuesta
	 */
	public String getRespuesta() {
		return respuesta;
	}

	/**
	 * Establece el texto de la respuesta.
	 *
	 * @param respuesta texto
	 */
	public void setRespuesta(String respuesta) {
		this.respuesta = respuesta;
	}
}
