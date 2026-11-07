package co.edu.uniquindio.unimotor.web.util;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

/**
 * Utilidades para añadir mensajes globales de JSF (se muestran en el growl de
 * la plantilla).
 */
public final class Mensajes {

	private Mensajes() {
	}

	/**
	 * Añade un mensaje global informativo.
	 *
	 * @param resumen titulo del mensaje
	 * @param detalle detalle del mensaje
	 */
	public static void info(String resumen, String detalle) {
		agregar(FacesMessage.SEVERITY_INFO, resumen, detalle);
	}

	/**
	 * Añade un mensaje global de advertencia.
	 *
	 * @param resumen titulo del mensaje
	 * @param detalle detalle del mensaje
	 */
	public static void advertencia(String resumen, String detalle) {
		agregar(FacesMessage.SEVERITY_WARN, resumen, detalle);
	}

	/**
	 * Añade un mensaje global de error.
	 *
	 * @param resumen titulo del mensaje
	 * @param detalle detalle del mensaje
	 */
	public static void error(String resumen, String detalle) {
		agregar(FacesMessage.SEVERITY_ERROR, resumen, detalle);
	}

	/**
	 * Conserva los mensajes en el flash para que sobrevivan a una redirección.
	 */
	public static void conservarTrasRedireccion() {
		FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
	}

	private static void agregar(FacesMessage.Severity severidad, String resumen, String detalle) {
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severidad, resumen, detalle));
	}
}
