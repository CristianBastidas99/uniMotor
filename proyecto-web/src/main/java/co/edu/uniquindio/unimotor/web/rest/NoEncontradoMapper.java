package co.edu.uniquindio.unimotor.web.rest;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import co.edu.uniquindio.unimotor.excepciones.ElementoNoEncontradoException;

/** Traduce {@link ElementoNoEncontradoException} a una respuesta JSON 404. */
@Provider
public class NoEncontradoMapper implements ExceptionMapper<ElementoNoEncontradoException> {

	@Override
	public Response toResponse(ElementoNoEncontradoException e) {
		return ErrorJson.respuesta(404, "No encontrado", e.getMessage());
	}
}
