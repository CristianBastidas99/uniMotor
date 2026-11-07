package co.edu.uniquindio.unimotor.web.rest;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Traduce {@link WebApplicationException} (rutas inexistentes, parámetros mal formados, etc.)
 * a JSON conservando su estado HTTP.
 */
@Provider
public class WebApplicationMapper implements ExceptionMapper<WebApplicationException> {

	@Override
	public Response toResponse(WebApplicationException e) {
		return responder(e);
	}

	static Response responder(WebApplicationException e) {
		int estado = e.getResponse().getStatus();
		if (Response.Status.Family.familyOf(estado) == Response.Status.Family.REDIRECTION) {
			return e.getResponse();
		}
		String texto = textoEstado(estado);
		String mensaje = e.getMessage();
		// Los mensajes por defecto de JAX-RS ("HTTP 404 Not Found") están en inglés: se reemplazan.
		if (mensaje == null || mensaje.isEmpty() || mensaje.startsWith("HTTP " + estado)) {
			mensaje = mensajePorDefecto(estado);
		}
		return ErrorJson.respuesta(estado, texto, mensaje);
	}

	/**
	 * Nombre en español del estado HTTP.
	 *
	 * @param estado código HTTP
	 * @return el nombre del estado
	 */
	static String textoEstado(int estado) {
		switch (estado) {
		case 400: return "Solicitud inválida";
		case 404: return "No encontrado";
		case 405: return "Método no permitido";
		case 406: return "Formato no aceptable";
		case 415: return "Tipo de contenido no soportado";
		default:
			Response.Status s = Response.Status.fromStatusCode(estado);
			return s == null ? "Error" : s.getReasonPhrase();
		}
	}

	private static String mensajePorDefecto(int estado) {
		switch (estado) {
		case 404: return "La ruta solicitada no existe en la API.";
		case 405: return "La API es de solo lectura: use GET.";
		case 406: return "La API solo responde JSON.";
		default: return textoEstado(estado) + ".";
		}
	}
}
