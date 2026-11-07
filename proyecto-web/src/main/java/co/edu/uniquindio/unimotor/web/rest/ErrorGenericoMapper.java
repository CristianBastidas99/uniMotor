package co.edu.uniquindio.unimotor.web.rest;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.EJBException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import co.edu.uniquindio.unimotor.excepciones.ElementoNoEncontradoException;
import co.edu.uniquindio.unimotor.excepciones.InformacionInvalidaException;

/**
 * Último recurso: cualquier excepción no tratada produce un 500 JSON genérico, sin traza.
 * Antes desenvuelve {@link EJBException} para delegar en el estado correcto.
 */
@Provider
public class ErrorGenericoMapper implements ExceptionMapper<Throwable> {

	private static final Logger LOG = Logger.getLogger(ErrorGenericoMapper.class.getName());

	@Override
	public Response toResponse(Throwable t) {
		Throwable causa = desenvolver(t);
		if (causa instanceof WebApplicationException) {
			return WebApplicationMapper.responder((WebApplicationException) causa);
		}
		if (causa instanceof ElementoNoEncontradoException) {
			return ErrorJson.respuesta(404, "No encontrado", causa.getMessage());
		}
		if (causa instanceof InformacionInvalidaException) {
			return ErrorJson.respuesta(400, "Solicitud inválida", causa.getMessage());
		}
		LOG.log(Level.SEVERE, "Error no controlado en la API REST", t);
		return ErrorJson.respuesta(500, "Error interno", "Ocurrió un error inesperado. Intente más tarde.");
	}

	/** Atraviesa las envolturas EJBException hasta la causa real. */
	private static Throwable desenvolver(Throwable t) {
		Throwable actual = t;
		for (int i = 0; i < 10 && actual instanceof EJBException; i++) {
			Throwable siguiente = ((EJBException) actual).getCausedByException();
			if (siguiente == null) {
				siguiente = actual.getCause();
			}
			if (siguiente == null || siguiente == actual) {
				break;
			}
			actual = siguiente;
		}
		return actual;
	}
}
